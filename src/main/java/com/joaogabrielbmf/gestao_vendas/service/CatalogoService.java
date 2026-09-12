package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.dto.*;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CatalogoService {
    private static final String CHAVE_COPA_2026 = "COPA_2026";

    private final ProdutoRepository produtoRepository;
    private final AlbumRepository albumRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioAtualService usuarioAtualService;

    private final Map<String, CatalogoDef> catalogos;

    public CatalogoService(ProdutoRepository produtoRepository,
                           AlbumRepository albumRepository,
                           CategoriaRepository categoriaRepository,
                           UsuarioAtualService usuarioAtualService) {
        this.produtoRepository = produtoRepository;
        this.albumRepository = albumRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioAtualService = usuarioAtualService;
        this.catalogos = criarCatalogos();
    }

    public List<CatalogoResumoResponse> listar() {
        Usuario usuario = usuarioAtualService.get();
        return catalogos.values().stream()
                .map(c -> new CatalogoResumoResponse(
                        c.chave(), c.nome(), c.tipoAlbum().name(), c.ano(), c.itens().size(), catalogoImportado(c, usuario)
                ))
                .toList();
    }

    public List<CatalogoItemResponse> itens(String chave) {
        CatalogoDef catalogo = buscarCatalogo(chave);
        Usuario usuario = usuarioAtualService.get();
        Album album = albumRepository
                .findByUsuarioAndTipoAlbumAndAnoAndNomeAlbumIgnoreCase(usuario, catalogo.tipoAlbum(), catalogo.ano(), catalogo.nomeAlbum())
                .orElse(null);

        Map<String, Produto> produtos = new HashMap<>();
        if (album != null) {
            for (Produto p : produtoRepository.findByUsuarioAndAlbum(usuario, album)) {
                if (p.getTipoProduto() == TipoProduto.FIGURINHA && p.getNumeroFigurinha() != null) {
                    produtos.put(chaveItem(p.getSelecaoFigurinha(), p.getNumeroFigurinha()), p);
                }
            }
        }

        return catalogo.itens().stream().map(item -> {
            Produto p = produtos.get(chaveItem(item.prefixo(), item.numero()));
            return new CatalogoItemResponse(
                    item.codigo(), item.grupoNome(), item.prefixo(), item.numero(),
                    p == null ? null : p.getCodigoProduto(),
                    p == null || p.getCategoria() == null ? null : p.getCategoria().getCodigoCategoria(),
                    p == null || p.getCategoria() == null ? null : p.getCategoria().getNomeCategoria()
            );
        }).toList();
    }

    @Transactional
    public Map<String, Object> importar(String chave) {
        CatalogoDef catalogo = buscarCatalogo(chave);
        Usuario usuario = usuarioAtualService.get();
        Album album = buscarOuCriarAlbum(catalogo, usuario);
        Categoria comum = buscarOuCriarCategoriaComum(usuario);

        Map<String, Produto> existentes = new HashMap<>();
        for (Produto p : produtoRepository.findByUsuarioAndAlbum(usuario, album)) {
            if (p.getNumeroFigurinha() != null) {
                existentes.put(chaveItem(p.getSelecaoFigurinha(), p.getNumeroFigurinha()), p);
            }
        }

        List<Produto> novos = new ArrayList<>();
        for (CatalogoItemDef item : catalogo.itens()) {
            String chaveItem = chaveItem(item.prefixo(), item.numero());
            if (existentes.containsKey(chaveItem)) continue;

            Produto p = new Produto();
            p.setTipoProduto(TipoProduto.FIGURINHA);
            p.setAlbum(album);
            p.setCategoria(comum);
            p.setSelecaoFigurinha(item.prefixo());
            p.setNumeroFigurinha(item.numero());
            p.setEstoque(0);
            p.setUsuario(usuario);
            p.setNomeProduto(null);
            p.setPrecoEspecifico(null);
            p.setCustoMedio(null);
            p.setCustoEspecifico(null);
            novos.add(p);
        }
        produtoRepository.saveAll(novos);

        return Map.of(
                "catalogo", catalogo.nome(),
                "total", catalogo.itens().size(),
                "criados", novos.size(),
                "jaExistentes", catalogo.itens().size() - novos.size()
        );
    }

    @Transactional
    public Map<String, Object> aplicarCategoria(String chave, AplicarCategoriaCatalogoRequest request) {
        CatalogoDef catalogo = buscarCatalogo(chave);
        Usuario usuario = usuarioAtualService.get();
        if (request.codigoCategoria() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }
        if (request.codigosProdutos() == null || request.codigosProdutos().isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos uma figurinha.");
        }

        Categoria categoria = categoriaRepository.findByCodigoCategoriaAndUsuario(request.codigoCategoria(), usuario)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        Album album = albumRepository
                .findByUsuarioAndTipoAlbumAndAnoAndNomeAlbumIgnoreCase(usuario, catalogo.tipoAlbum(), catalogo.ano(), catalogo.nomeAlbum())
                .orElseThrow(() -> new IllegalStateException("Importe o catálogo antes de definir categorias."));

        Set<Integer> permitidos = produtoRepository.findByUsuarioAndAlbum(usuario, album).stream()
                .map(Produto::getCodigoProduto)
                .collect(java.util.stream.Collectors.toSet());

        List<Produto> produtos = produtoRepository.findAllById(request.codigosProdutos()).stream()
                .filter(p -> permitidos.contains(p.getCodigoProduto()))
                .toList();

        if (produtos.size() != new HashSet<>(request.codigosProdutos()).size()) {
            throw new IllegalArgumentException("Há produtos selecionados que não pertencem a este catálogo do usuário.");
        }

        for (Produto p : produtos) p.setCategoria(categoria);
        produtoRepository.saveAll(produtos);
        return Map.of("atualizados", produtos.size(), "categoria", categoria.getNomeCategoria());
    }

    private boolean catalogoImportado(CatalogoDef c, Usuario usuario) {
        return albumRepository
                .findByUsuarioAndTipoAlbumAndAnoAndNomeAlbumIgnoreCase(usuario, c.tipoAlbum(), c.ano(), c.nomeAlbum())
                .map(a -> produtoRepository.countByUsuarioAndAlbum(usuario, a) > 0)
                .orElse(false);
    }

    private Album buscarOuCriarAlbum(CatalogoDef c, Usuario usuario) {
        return albumRepository
                .findByUsuarioAndTipoAlbumAndAnoAndNomeAlbumIgnoreCase(usuario, c.tipoAlbum(), c.ano(), c.nomeAlbum())
                .orElseGet(() -> {
                    Album a = new Album();
                    a.setNomeAlbum(c.nomeAlbum());
                    a.setAno(c.ano());
                    a.setTipoAlbum(c.tipoAlbum());
                    a.setUsuario(usuario);
                    return albumRepository.save(a);
                });
    }

    private Categoria buscarOuCriarCategoriaComum(Usuario usuario) {
        return categoriaRepository.findByUsuarioAndNomeCategoriaIgnoreCase(usuario, "Comum")
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNomeCategoria("Comum");
                    c.setUsuario(usuario);
                    return categoriaRepository.save(c);
                });
    }

    private CatalogoDef buscarCatalogo(String chave) {
        CatalogoDef catalogo = catalogos.get(chave.toUpperCase(Locale.ROOT));
        if (catalogo == null) throw new IllegalArgumentException("Catálogo não encontrado.");
        return catalogo;
    }

    private String chaveItem(String prefixo, Integer numero) {
        return (prefixo == null ? "" : prefixo.toUpperCase(Locale.ROOT)) + "#" + numero;
    }

    private Map<String, CatalogoDef> criarCatalogos() {
        List<CatalogoItemDef> itens = new ArrayList<>();
        itens.add(new CatalogoItemDef("00", "Fifa World Cup History", null, 0));
        adicionarGrupo(itens, "Fifa World Cup History", "FWC", 19);
        adicionarGrupo(itens, "México", "MEX", 20);
        adicionarGrupo(itens, "África do Sul", "RSA", 20);
        adicionarGrupo(itens, "Coreia do Sul", "KOR", 20);
        adicionarGrupo(itens, "Rep. Tcheca", "CZE", 20);
        adicionarGrupo(itens, "Canadá", "CAN", 20);
        adicionarGrupo(itens, "Bósnia", "BIH", 20);
        adicionarGrupo(itens, "Catar", "QAT", 20);
        adicionarGrupo(itens, "Suíça", "SUI", 20);
        adicionarGrupo(itens, "Brasil", "BRA", 20);
        adicionarGrupo(itens, "Marrocos", "MAR", 20);
        adicionarGrupo(itens, "Haiti", "HAI", 20);
        adicionarGrupo(itens, "Escócia", "SCO", 20);
        adicionarGrupo(itens, "Estados Unidos", "USA", 20);
        adicionarGrupo(itens, "Paraguai", "PAR", 20);
        adicionarGrupo(itens, "Austrália", "AUS", 20);
        adicionarGrupo(itens, "Turquia", "TUR", 20);
        adicionarGrupo(itens, "Alemanha", "GER", 20);
        adicionarGrupo(itens, "Curaçao", "CUW", 20);
        adicionarGrupo(itens, "Costa do Marfim", "CIV", 20);
        adicionarGrupo(itens, "Equador", "ECU", 20);
        adicionarGrupo(itens, "Holanda", "NED", 20);
        adicionarGrupo(itens, "Japão", "JPN", 20);
        adicionarGrupo(itens, "Suécia", "SWE", 20);
        adicionarGrupo(itens, "Tunísia", "TUN", 20);
        adicionarGrupo(itens, "Bélgica", "BEL", 20);
        adicionarGrupo(itens, "Egito", "EGY", 20);
        adicionarGrupo(itens, "Irã", "IRN", 20);
        adicionarGrupo(itens, "Nova Zelândia", "NZL", 20);
        adicionarGrupo(itens, "Espanha", "ESP", 20);
        adicionarGrupo(itens, "Cabo Verde", "CPV", 20);
        adicionarGrupo(itens, "Arábia Saudita", "KSA", 20);
        adicionarGrupo(itens, "Uruguai", "URU", 20);
        adicionarGrupo(itens, "França", "FRA", 20);
        adicionarGrupo(itens, "Senegal", "SEN", 20);
        adicionarGrupo(itens, "Iraque", "IRQ", 20);
        adicionarGrupo(itens, "Noruega", "NOR", 20);
        adicionarGrupo(itens, "Argentina", "ARG", 20);
        adicionarGrupo(itens, "Argélia", "ALG", 20);
        adicionarGrupo(itens, "Áustria", "AUT", 20);
        adicionarGrupo(itens, "Jordânia", "JOR", 20);
        adicionarGrupo(itens, "Portugal", "POR", 20);
        adicionarGrupo(itens, "Congo", "COD", 20);
        adicionarGrupo(itens, "Uzbequistão", "UZB", 20);
        adicionarGrupo(itens, "Colômbia", "COL", 20);
        adicionarGrupo(itens, "Inglaterra", "ENG", 20);
        adicionarGrupo(itens, "Croácia", "CRO", 20);
        adicionarGrupo(itens, "Gana", "GHA", 20);
        adicionarGrupo(itens, "Panamá", "PAN", 20);
        adicionarGrupo(itens, "Coca-Cola", "CC", 14);

        CatalogoDef copa2026 = new CatalogoDef(
                CHAVE_COPA_2026,
                "Copa do Mundo 2026",
                "Copa do Mundo",
                TipoAlbum.COPA_DO_MUNDO,
                2026,
                List.copyOf(itens)
        );
        return Map.of(CHAVE_COPA_2026, copa2026);
    }

    private void adicionarGrupo(List<CatalogoItemDef> destino, String grupoNome, String prefixo, int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            destino.add(new CatalogoItemDef(prefixo + i, grupoNome, prefixo, i));
        }
    }

    private record CatalogoDef(String chave, String nome, String nomeAlbum, TipoAlbum tipoAlbum, Integer ano,
                               List<CatalogoItemDef> itens) {}
    private record CatalogoItemDef(String codigo, String grupoNome, String prefixo, Integer numero) {}
}
