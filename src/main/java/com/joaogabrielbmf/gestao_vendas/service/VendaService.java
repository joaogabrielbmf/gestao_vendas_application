package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.dto.*;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class VendaService {
    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final ConfiguracaoVendaRepository configuracaoRepository;
    private final ProdutoService produtoService;
    private final ShopeeTaxService shopeeTaxService;
    private final UsuarioAtualService usuarioAtualService;

    public VendaService(VendaRepository vendaRepository, ItemVendaRepository itemRepository,
                        ProdutoRepository produtoRepository, ClienteRepository clienteRepository,
                        ConfiguracaoVendaRepository configuracaoRepository, ProdutoService produtoService,
                        ShopeeTaxService shopeeTaxService, UsuarioAtualService usuarioAtualService) {
        this.vendaRepository = vendaRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.produtoService = produtoService;
        this.shopeeTaxService = shopeeTaxService;
        this.usuarioAtualService = usuarioAtualService;
    }

    @Transactional
    public Venda criar(VendaRequest request) {
        ConfiguracaoVenda conf = buscarConfiguracao(request.getCodigoConfiguracao());
        Venda venda = prepararVenda(
                request.getCodigoCliente(),
                conf,
                request.isFretePagoVendedor(),
                request.getFrete()
        );

        BigDecimal faturamento = BigDecimal.ZERO;

        if (request.getItens() != null) {
            for (ItemVendaRequest itemReq : request.getItens()) {
                Produto produto = buscarProdutoValidandoQuantidade(itemReq.getCodigoProduto(), itemReq.getQuantidade());
                BigDecimal valorUnitario = produtoService.buscarPrecoAtual(produto.getCodigoProduto());
                salvarItem(venda, produto, itemReq.getQuantidade(), valorUnitario);
                faturamento = faturamento.add(
                        valorUnitario.multiply(BigDecimal.valueOf(itemReq.getQuantidade()))
                );
            }
        }

        atualizarTaxaShopeeDaVenda(venda, conf, faturamento);
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda criarAPartirDoOrcamento(Orcamento orcamento,
                                         List<ItemOrcamento> itensOrcamento,
                                         ConverterOrcamentoRequest request) {
        if (orcamento.getStatusOrcamento() == StatusOrcamento.CONVERTIDO) {
            throw new IllegalStateException("Orçamento já convertido em venda.");
        }

        ConfiguracaoVenda conf = buscarConfiguracao(request.getCodigoConfiguracao());
        Venda venda = prepararVenda(
                orcamento.getCliente() == null ? null : orcamento.getCliente().getCodigoCliente(),
                conf,
                request.isFretePagoVendedor(),
                request.getFrete()
        );

        BigDecimal faturamento = BigDecimal.ZERO;

        for (ItemOrcamento itemOrcamento : itensOrcamento) {
            Produto produto = buscarProdutoValidandoQuantidade(
                    itemOrcamento.getProduto().getCodigoProduto(),
                    itemOrcamento.getQuantidade()
            );

            BigDecimal valorUnitario = itemOrcamento.getValorUnitario();
            salvarItem(venda, produto, itemOrcamento.getQuantidade(), valorUnitario);
            faturamento = faturamento.add(
                    valorUnitario.multiply(BigDecimal.valueOf(itemOrcamento.getQuantidade()))
            );
        }

        atualizarTaxaShopeeDaVenda(venda, conf, faturamento);
        return vendaRepository.save(venda);
    }

    private Venda prepararVenda(Integer codigoCliente,
                                ConfiguracaoVenda conf,
                                boolean fretePagoVendedor,
                                BigDecimal frete) {
        Venda venda = new Venda();
        venda.setData(LocalDate.now());
        venda.setUsuario(usuarioAtualService.get());
        venda.setStatusVenda(StatusVenda.EM_ABERTO);
        venda.setConfiguracaoVenda(conf);

        if (codigoCliente != null) {
            venda.setCliente(clienteRepository.findByCodigoClienteAndUsuario(codigoCliente, usuarioAtualService.get())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado.")));
        }

        venda.setEmbalagem(nvl(conf.getEmbalagem()));
        venda.setTaxaPercentual(conf.isConfiguracaoShopee()
                ? BigDecimal.ZERO
                : nvl(conf.getTaxaPercentual()));
        venda.setTaxaFixa(conf.isConfiguracaoShopee()
                ? BigDecimal.ZERO
                : nvl(conf.getTaxaFixa()));
        venda.setFretePagoVendedor(fretePagoVendedor);
        venda.setFrete(fretePagoVendedor ? nvl(frete) : BigDecimal.ZERO);
        venda.setTaxaShopeeTotal(BigDecimal.ZERO);

        return vendaRepository.save(venda);
    }

    private void salvarItem(Venda venda,
                            Produto produto,
                            int quantidade,
                            BigDecimal valorUnitario) {
        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setValorUnitario(valorUnitario);

        // Mantidos zerados por compatibilidade com o modelo antigo.
        item.setCustoUnitario(BigDecimal.ZERO);
        item.setTaxaShopeeUnitaria(BigDecimal.ZERO);

        itemRepository.save(item);
    }

    private void atualizarTaxaShopeeDaVenda(Venda venda,
                                            ConfiguracaoVenda conf,
                                            BigDecimal faturamento) {
        BigDecimal taxa = conf.isConfiguracaoShopee()
                ? shopeeTaxService.calcularTaxaTotal(faturamento, conf)
                : BigDecimal.ZERO;
        venda.setTaxaShopeeTotal(taxa);
    }

    private ConfiguracaoVenda buscarConfiguracao(Integer codigo) {
        if (codigo == null) {
            throw new IllegalArgumentException("Configuração de venda é obrigatória.");
        }
        return configuracaoRepository.findByCodigoConfiguracaoAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Configuração de venda não encontrada."));
    }

    private Produto buscarProdutoValidandoQuantidade(Integer codigoProduto, Integer quantidade) {
        Produto produto = produtoRepository.findByCodigoProdutoAndUsuario(codigoProduto, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade inválida.");
        }
        if (produto.getEstoque() < quantidade) {
            throw new IllegalStateException("Estoque insuficiente para " + identificacao(produto) + ".");
        }
        return produto;
    }

    public List<Venda> listar() {
        return vendaRepository.findByUsuario(usuarioAtualService.get());
    }

    public Optional<Venda> buscar(int id) {
        return vendaRepository.findByCodigoVendaAndUsuario(id, usuarioAtualService.get());
    }

    public List<ItemVenda> itens(int id) {
        Venda venda = vendaRepository.findByCodigoVendaAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));
        return itemRepository.findByVenda(venda);
    }

    public Page<Venda> buscarHistoricoPaginado(int page, int size,
                                                LocalDate dataInicio, LocalDate dataFim,
                                                String status, Integer cliente,
                                                Integer canal, String tipoProduto,
                                                String ordenarPor, String direcao) {

        Usuario usuario = usuarioAtualService.get();
        Specification<Venda> spec = (root, query, cb) -> cb.equal(root.get("usuario"), usuario);

        if (status != null && !status.isBlank()) {
            StatusVenda statusVenda;
            try {
                statusVenda = StatusVenda.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido.");
            }
            if (statusVenda == StatusVenda.EM_ABERTO) {
                throw new IllegalArgumentException("O histórico contém apenas vendas finalizadas ou canceladas.");
            }
            spec = spec.and((root, query, cb) -> cb.equal(root.get("statusVenda"), statusVenda));
        } else {
            spec = spec.and((root, query, cb) ->
                    cb.notEqual(root.get("statusVenda"), StatusVenda.EM_ABERTO));
        }

        if (dataInicio != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("data"), dataInicio));
        }

        if (dataFim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("data"), dataFim));
        }

        if (cliente != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("cliente").get("codigoCliente"), cliente));
        }

        if (canal != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("configuracaoVenda").get("codigoConfiguracao"), canal));
        }

        if (tipoProduto != null && !tipoProduto.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                var sub = query.subquery(Integer.class);
                var item = sub.from(ItemVenda.class);
                var produto = item.join("produto");

                var mesmaVenda = cb.equal(
                        item.get("venda").get("codigoVenda"),
                        root.get("codigoVenda")
                );

                jakarta.persistence.criteria.Predicate tipoPredicate;
                if (tipoProduto.startsWith("CUSTOM:")) {
                    Integer codigo;
                    try {
                        codigo = Integer.valueOf(tipoProduto.substring("CUSTOM:".length()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Tipo de produto inválido.");
                    }
                    tipoPredicate = cb.equal(
                            produto.join("tipoProdutoCustomizado").get("codigoTipoProduto"),
                            codigo
                    );
                } else {
                    TipoProduto tipo;
                    try {
                        tipo = TipoProduto.valueOf(tipoProduto);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Tipo de produto inválido.");
                    }
                    tipoPredicate = cb.equal(produto.get("tipoProduto"), tipo);
                }

                sub.select(item.get("codigoItemVenda"))
                        .where(cb.and(mesmaVenda, tipoPredicate));
                return cb.exists(sub);
            });
        }

        String campoOrdenacao = switch (ordenarPor == null ? "" : ordenarPor) {
            case "cliente" -> "cliente.nomeCliente";
            case "canal" -> "configuracaoVenda.nome";
            case "status" -> "statusVenda";
            default -> "data";
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(direcao)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 20),
                Sort.by(direction, campoOrdenacao)
        );

        return vendaRepository.findAll(spec, pageable);
    }

    @Transactional
    public Venda finalizar(int id) {
        Venda venda = vendaRepository.findByCodigoVendaAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));

        if (venda.getStatusVenda() != StatusVenda.EM_ABERTO) {
            throw new IllegalStateException("Somente vendas em aberto podem ser finalizadas.");
        }

        List<ItemVenda> itens = itemRepository.findByVenda(venda);
        if (itens.isEmpty()) {
            throw new IllegalStateException("Venda sem itens.");
        }

        for (ItemVenda item : itens) {
            if (item.getProduto().getEstoque() < item.getQuantidade()) {
                throw new IllegalStateException(
                        "Estoque insuficiente para " + identificacao(item.getProduto()) + ".");
            }
        }

        for (ItemVenda item : itens) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() - item.getQuantidade());
            produtoRepository.save(produto);
        }

        venda.setStatusVenda(StatusVenda.FINALIZADA);
        return vendaRepository.save(venda);
    }

    @Transactional
    public Venda cancelar(int id) {
        Venda venda = vendaRepository.findByCodigoVendaAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));

        if (venda.getStatusVenda() == StatusVenda.CANCELADA) {
            throw new IllegalStateException("Venda já está cancelada.");
        }

        if (venda.getStatusVenda() == StatusVenda.FINALIZADA) {
            for (ItemVenda item : itemRepository.findByVenda(venda)) {
                Produto produto = item.getProduto();
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        venda.setStatusVenda(StatusVenda.CANCELADA);
        return vendaRepository.save(venda);
    }

    public ResumoVendaResponse resumo(int id) {
        Venda venda = vendaRepository.findByCodigoVendaAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));

        BigDecimal faturamento = itemRepository.findByVenda(venda).stream()
                .map(item -> item.getValorUnitario()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxaShopee = nvl(venda.getTaxaShopeeTotal());
        BigDecimal taxaPercentual = faturamento.multiply(nvl(venda.getTaxaPercentual()));
        BigDecimal taxaFixa = nvl(venda.getTaxaFixa());
        BigDecimal embalagem = nvl(venda.getEmbalagem());
        BigDecimal frete = venda.isFretePagoVendedor() ? nvl(venda.getFrete()) : BigDecimal.ZERO;

        BigDecimal despesas = taxaShopee
                .add(taxaPercentual)
                .add(taxaFixa)
                .add(embalagem)
                .add(frete);

        // Custo de estoque é controlado pela seção Compras de produtos.
        BigDecimal custoProdutos = BigDecimal.ZERO;
        BigDecimal resultadoAntesEstoque = faturamento.subtract(despesas);

        return new ResumoVendaResponse(
                venda.getCodigoVenda(),
                venda.getStatusVenda(),
                faturamento,
                custoProdutos,
                taxaShopee,
                taxaPercentual,
                taxaFixa,
                embalagem,
                frete,
                despesas,
                resultadoAntesEstoque
        );
    }

    @Transactional
    public void deletar(int id) {
        Venda venda = vendaRepository.findByCodigoVendaAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));
        if (venda.getStatusVenda() != StatusVenda.EM_ABERTO) {
            throw new IllegalStateException("Somente venda em aberto pode ser excluída.");
        }
        itemRepository.deleteAll(itemRepository.findByVenda(venda));
        vendaRepository.delete(venda);
    }

    private String identificacao(Produto produto) {
        if (produto.getTipoProduto() == TipoProduto.FIGURINHA) {
            return (produto.getSelecaoFigurinha() != null
                    ? produto.getSelecaoFigurinha() + " "
                    : "") + produto.getNumeroFigurinha();
        }
        if (produto.getNomeProduto() != null) {
            return produto.getNomeProduto();
        }
        if (produto.getTipoProdutoCustomizado() != null) {
            return produto.getTipoProdutoCustomizado().getNome();
        }
        return "produto";
    }

    private BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
