package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.dto.ProdutoRequest;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProdutoService {
    private static final Pattern SELECAO_3_LETRAS = Pattern.compile("^[A-Z]{3}$");

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlbumRepository albumRepository;
    private final PrecoCategoriaAlbumRepository precoCategoriaAlbumRepository;
    private final TipoProdutoCustomizadoRepository tipoCustomizadoRepository;
    private final UsuarioAtualService usuarioAtualService;
    private final TextoService textoService;

    public ProdutoService(ProdutoRepository produtoRepository,
                          CategoriaRepository categoriaRepository,
                          AlbumRepository albumRepository,
                          PrecoCategoriaAlbumRepository precoCategoriaAlbumRepository,
                          TipoProdutoCustomizadoRepository tipoCustomizadoRepository,
                          UsuarioAtualService usuarioAtualService,
                          TextoService textoService) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.albumRepository = albumRepository;
        this.precoCategoriaAlbumRepository = precoCategoriaAlbumRepository;
        this.tipoCustomizadoRepository = tipoCustomizadoRepository;
        this.usuarioAtualService = usuarioAtualService;
        this.textoService = textoService;
    }

    public Produto cadastrar(ProdutoRequest r) {
        validarEstoque(r.getEstoque());

        boolean tipoSistema = r.getTipoProduto() != null;
        boolean tipoCustomizado = r.getCodigoTipoCustomizado() != null;
        if (tipoSistema == tipoCustomizado) {
            throw new IllegalArgumentException("Selecione exatamente um tipo de produto.");
        }

        Produto p = new Produto();
        p.setUsuario(usuarioAtualService.get());
        p.setEstoque(r.getEstoque());
        p.setNomeProduto(textoService.titulo(r.getNomeProduto()));
        p.setPrecoEspecifico(r.getPrecoEspecifico());

        if (tipoCustomizado) {
            TipoProdutoCustomizado custom = buscarTipoCustomizadoDoUsuario(r.getCodigoTipoCustomizado());
            if (p.getNomeProduto() == null || p.getNomeProduto().isBlank()) {
                throw new IllegalArgumentException("Nome é obrigatório para este tipo de produto.");
            }
            if (p.getPrecoEspecifico() == null) {
                throw new IllegalArgumentException("Preço específico é obrigatório para este tipo de produto.");
            }
            p.setTipoProduto(TipoProduto.CUSTOMIZADO);
            p.setTipoProdutoCustomizado(custom);
            limparCamposDeFigurinha(p);
            p.setAlbum(null);
            return produtoRepository.save(p);
        }

        p.setTipoProduto(r.getTipoProduto());
        p.setTipoProdutoCustomizado(null);

        if (r.getTipoProduto() == TipoProduto.FIGURINHA) {
            if (r.getCodigoAlbum() == null) throw new IllegalArgumentException("Álbum é obrigatório para figurinha.");
            if (r.getCodigoCategoria() == null) throw new IllegalArgumentException("Categoria é obrigatória para figurinha.");
            if (r.getNumeroFigurinha() == null) throw new IllegalArgumentException("Número é obrigatório para figurinha.");

            Album album = buscarAlbum(r.getCodigoAlbum());
            Categoria categoria = categoriaRepository.findByCodigoCategoriaAndUsuario(r.getCodigoCategoria(), usuarioAtualService.get())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
            boolean copaComSelecao = exigeSelecao(album);
            String selecao = null;

            if (copaComSelecao) {
                selecao = normalizarSelecao(r.getSelecaoFigurinha());
                if (produtoRepository.existsByAlbumAndSelecaoFigurinhaAndNumeroFigurinha(album, selecao, r.getNumeroFigurinha())) {
                    throw new IllegalStateException("Figurinha já cadastrada.");
                }
            } else if (produtoRepository.existsByAlbumAndNumeroFigurinha(album, r.getNumeroFigurinha())) {
                throw new IllegalStateException("Figurinha já cadastrada.");
            }

            p.setAlbum(album);
            p.setCategoria(categoria);
            p.setNumeroFigurinha(r.getNumeroFigurinha());
            p.setSelecaoFigurinha(selecao);
            p.setCustoMedio(null);
            p.setCustoEspecifico(null);
            return produtoRepository.save(p);
        }

        if (r.getTipoProduto() == TipoProduto.ALBUM_COMPLETO || r.getTipoProduto() == TipoProduto.ALBUM_INCOMPLETO) {
            if (r.getCodigoAlbum() == null) throw new IllegalArgumentException("Álbum é obrigatório.");
            if (p.getNomeProduto() == null || p.getNomeProduto().isBlank()) {
                throw new IllegalArgumentException("Nome é obrigatório para álbum.");
            }
            if (p.getPrecoEspecifico() == null) {
                throw new IllegalArgumentException("Preço específico é obrigatório para álbum.");
            }
            p.setAlbum(buscarAlbum(r.getCodigoAlbum()));
            limparCamposDeFigurinha(p);
            p.setCustoMedio(null);
            p.setCustoEspecifico(null);
            return produtoRepository.save(p);
        }

        throw new IllegalArgumentException("Tipo de produto inválido.");
    }

    public List<Produto> mostrarTodos() {
        return produtoRepository.findByUsuario(usuarioAtualService.get());
    }

    public Page<Produto> buscarPaginado(int page, int size,
                                        String tipo, String album, Integer ano,
                                        String selecao, Integer codigoCategoria,
                                        String estoque, String busca,
                                        String ordenarPor, String direcao) {

        Usuario usuario = usuarioAtualService.get();
        Specification<Produto> spec = (root, query, cb) -> cb.equal(root.get("usuario"), usuario);

        if (tipo != null && !tipo.isBlank()) {
            if (tipo.startsWith("CUSTOM:")) {
                Integer codigo;
                try {
                    codigo = Integer.valueOf(tipo.substring("CUSTOM:".length()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Tipo de produto inválido.");
                }
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.join("tipoProdutoCustomizado").get("codigoTipoProduto"), codigo));
            } else {
                TipoProduto tipoProduto;
                try {
                    tipoProduto = TipoProduto.valueOf(tipo);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Tipo de produto inválido.");
                }
                spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoProduto"), tipoProduto));
            }
        }

        if (album != null && !album.isBlank()) {
            String albumNormalizado = album.trim().toLowerCase(Locale.ROOT);
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.join("album").get("nomeAlbum")), albumNormalizado));
        }

        if (ano != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("album").get("ano"), ano));
        }

        if (selecao != null && !selecao.isBlank()) {
            String selecaoNormalizada = selecao.trim().toUpperCase(Locale.ROOT);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("selecaoFigurinha"), selecaoNormalizada));
        }

        if (codigoCategoria != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("categoria").get("codigoCategoria"), codigoCategoria));
        }

        if ("com".equalsIgnoreCase(estoque)) {
            spec = spec.and((root, query, cb) -> cb.gt(root.get("estoque"), 0));
        } else if ("sem".equalsIgnoreCase(estoque)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estoque"), 0));
        }

        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim().toLowerCase(Locale.ROOT);
            Integer numero = null;
            try { numero = Integer.valueOf(termo); } catch (NumberFormatException ignored) {}
            Integer numeroFinal = numero;
            spec = spec.and((root, query, cb) -> {
                var porNome = cb.like(cb.lower(root.get("nomeProduto")), "%" + termo + "%");
                var porSelecao = cb.like(cb.lower(root.get("selecaoFigurinha")), "%" + termo + "%");
                if (numeroFinal != null) {
                    return cb.or(porNome, porSelecao, cb.equal(root.get("numeroFigurinha"), numeroFinal));
                }
                return cb.or(porNome, porSelecao);
            });
        }

        String propriedadeOrdenacao = switch (ordenarPor == null ? "" : ordenarPor) {
            case "numero" -> "numeroFigurinha";
            case "selecao" -> "selecaoFigurinha";
            case "produto" -> "nomeProduto";
            case "tipo" -> "tipoProduto";
            case "album" -> "album.nomeAlbum";
            case "ano" -> "album.ano";
            case "categoria" -> "categoria.nomeCategoria";
            case "preco" -> "precoEspecifico";
            case "estoque" -> "estoque";
            default -> "codigoProduto";
        };

        Sort.Direction direction = "desc".equalsIgnoreCase(direcao)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort sort = "selecao".equals(ordenarPor)
                ? Sort.by(direction, "selecaoFigurinha").and(Sort.by(direction, "numeroFigurinha"))
                : Sort.by(direction, propriedadeOrdenacao);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                sort
        );

        return produtoRepository.findAll(spec, pageable);
    }

    public Optional<Produto> buscarPorID(int codigo) {
        return produtoRepository.findByCodigoProdutoAndUsuario(codigo, usuarioAtualService.get());
    }

    public Produto editar(int codigo, ProdutoRequest r) {
        Produto p = produtoRepository.findByCodigoProdutoAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        validarEstoque(r.getEstoque());
        p.setEstoque(r.getEstoque());
        p.setNomeProduto(textoService.titulo(r.getNomeProduto()));
        p.setPrecoEspecifico(r.getPrecoEspecifico());

        if (p.getTipoProdutoCustomizado() != null) {
            if (p.getNomeProduto() == null || p.getNomeProduto().isBlank()) {
                throw new IllegalArgumentException("Nome é obrigatório para este tipo de produto.");
            }
            if (p.getPrecoEspecifico() == null) {
                throw new IllegalArgumentException("Preço específico é obrigatório para este tipo de produto.");
            }
            limparCamposDeFigurinha(p);
            p.setAlbum(null);
            return produtoRepository.save(p);
        }

        if (p.getTipoProduto() == TipoProduto.FIGURINHA) {
            if (r.getCodigoAlbum() == null || r.getCodigoCategoria() == null || r.getNumeroFigurinha() == null) {
                throw new IllegalArgumentException("Álbum, categoria e número são obrigatórios para figurinha.");
            }
            Album album = buscarAlbum(r.getCodigoAlbum());
            Categoria categoria = categoriaRepository.findByCodigoCategoriaAndUsuario(r.getCodigoCategoria(), usuarioAtualService.get())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
            boolean copaComSelecao = exigeSelecao(album);
            String selecao = null;

            if (copaComSelecao) {
                selecao = normalizarSelecao(r.getSelecaoFigurinha());
                if (produtoRepository.existsByAlbumAndSelecaoFigurinhaAndNumeroFigurinhaAndCodigoProdutoNot(
                        album, selecao, r.getNumeroFigurinha(), codigo)) {
                    throw new IllegalStateException("Já existe outra figurinha com esses dados.");
                }
            } else if (produtoRepository.existsByAlbumAndNumeroFigurinhaAndCodigoProdutoNot(
                    album, r.getNumeroFigurinha(), codigo)) {
                throw new IllegalStateException("Já existe outra figurinha com esse número.");
            }

            p.setAlbum(album);
            p.setCategoria(categoria);
            p.setNumeroFigurinha(r.getNumeroFigurinha());
            p.setSelecaoFigurinha(selecao);
            p.setCustoMedio(null);
            p.setCustoEspecifico(null);
            return produtoRepository.save(p);
        }

        if (p.getTipoProduto() == TipoProduto.ALBUM_COMPLETO || p.getTipoProduto() == TipoProduto.ALBUM_INCOMPLETO) {
            if (r.getCodigoAlbum() == null) throw new IllegalArgumentException("Álbum é obrigatório.");
            if (p.getNomeProduto() == null || p.getNomeProduto().isBlank()) {
                throw new IllegalArgumentException("Nome é obrigatório para álbum.");
            }
            if (p.getPrecoEspecifico() == null) {
                throw new IllegalArgumentException("Preço específico é obrigatório para álbum.");
            }
            p.setAlbum(buscarAlbum(r.getCodigoAlbum()));
            limparCamposDeFigurinha(p);
            p.setCustoMedio(null);
            p.setCustoEspecifico(null);
            return produtoRepository.save(p);
        }

        return produtoRepository.save(p);
    }

    public Produto atualizarEstoque(int codigo, int estoque) {
        validarEstoque(estoque);
        Produto p = produtoRepository.findByCodigoProdutoAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (p.getUsuario() == null || !Objects.equals(p.getUsuario().getCodigoUsuario(), usuarioAtualService.get().getCodigoUsuario())) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        p.setEstoque(estoque);
        return produtoRepository.save(p);
    }

    public void deletar(int codigo) {
        Produto p = produtoRepository.findByCodigoProdutoAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        produtoRepository.delete(p);
    }

    public BigDecimal buscarPrecoAtual(int codigoProduto) {
        Usuario usuario = usuarioAtualService.get();
        Produto produto = produtoRepository.findByCodigoProdutoAndUsuario(codigoProduto, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));

        if (produto.getPrecoEspecifico() != null) {
            return produto.getPrecoEspecifico();
        }

        if (produto.getTipoProduto() != TipoProduto.FIGURINHA ||
                produto.getAlbum() == null || produto.getCategoria() == null) {
            throw new IllegalStateException("Preço específico não cadastrado para este produto.");
        }

        return precoCategoriaAlbumRepository
                .findByUsuarioAndAlbumAndCategoria(usuario, produto.getAlbum(), produto.getCategoria())
                .orElseThrow(() -> new IllegalStateException(
                        "Preço não cadastrado para esta categoria neste álbum."))
                .getValor();
    }

    public BigDecimal buscarCustoAtual(int codigoProduto) {
        if (produtoRepository.findByCodigoProdutoAndUsuario(codigoProduto, usuarioAtualService.get()).isEmpty()) {
            throw new IllegalArgumentException("Produto não encontrado.");
        }
        return BigDecimal.ZERO;
    }

    private Album buscarAlbum(Integer codigo) {
        return albumRepository.findByCodigoAlbumAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Álbum não encontrado."));
    }

    private TipoProdutoCustomizado buscarTipoCustomizadoDoUsuario(Integer codigo) {
        return tipoCustomizadoRepository.findByCodigoTipoProdutoAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de produto não encontrado."));
    }

    private void validarEstoque(Integer estoque) {
        if (estoque == null || estoque < 0) {
            throw new IllegalArgumentException("Estoque inválido.");
        }
    }

    private boolean exigeSelecao(Album album) {
        return album.getTipoAlbum() == TipoAlbum.COPA_DO_MUNDO &&
                album.getAno() != null &&
                album.getAno() >= 2022;
    }

    private String normalizarSelecao(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Seleção é obrigatória para Copa do Mundo 2022 ou posterior.");
        }
        String selecao = valor.trim().toUpperCase(Locale.ROOT);
        if (!SELECAO_3_LETRAS.matcher(selecao).matches()) {
            throw new IllegalArgumentException("Seleção deve possuir exatamente 3 letras. Ex.: BRA.");
        }
        return selecao;
    }

    private void limparCamposDeFigurinha(Produto p) {
        p.setCategoria(null);
        p.setSelecaoFigurinha(null);
        p.setNumeroFigurinha(null);
    }
}
