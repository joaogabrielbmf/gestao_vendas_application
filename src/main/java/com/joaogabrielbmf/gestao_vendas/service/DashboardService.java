package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.dto.DashboardResponse;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;

import java.math.*;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {
    private final VendaRepository vendaRepository;
    private final ItemVendaRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final VendaService vendaService;
    private final UsuarioAtualService usuarioAtualService;
    private final CompraProdutoService compraProdutoService;

    public DashboardService(VendaRepository vendaRepository,
                            ItemVendaRepository itemRepository,
                            ProdutoRepository produtoRepository,
                            VendaService vendaService,
                            UsuarioAtualService usuarioAtualService,
                            CompraProdutoService compraProdutoService) {
        this.vendaRepository = vendaRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.vendaService = vendaService;
        this.usuarioAtualService = usuarioAtualService;
        this.compraProdutoService = compraProdutoService;
    }

    public DashboardResponse resumo() {
        return resumo(null, null);
    }

    public DashboardResponse resumo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }

        Usuario usuario = usuarioAtualService.get();
        List<Venda> finalizadas = vendaRepository.findByUsuarioAndStatusVenda(usuario, StatusVenda.FINALIZADA).stream()
                .filter(v -> dentroDoPeriodo(v.getData(), inicio, fim))
                .toList();

        BigDecimal faturamento = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;

        Map<String, Long> quantidadePorTipo = new LinkedHashMap<>();
        Map<String, BigDecimal> faturamentoPorTipo = new LinkedHashMap<>();
        Map<String, Long> ranking = new HashMap<>();

        for (Venda venda : finalizadas) {
            var resumo = vendaService.resumo(venda.getCodigoVenda());
            faturamento = faturamento.add(resumo.getFaturamento());
            despesas = despesas.add(resumo.getDespesasTotais());

            for (ItemVenda item : itemRepository.findByVenda(venda)) {
                long quantidade = item.getQuantidade();
                String tipo = nomeTipo(item.getProduto());

                quantidadePorTipo.merge(tipo, quantidade, Long::sum);
                faturamentoPorTipo.merge(
                        tipo,
                        item.getValorUnitario().multiply(BigDecimal.valueOf(quantidade)),
                        BigDecimal::add
                );
                ranking.merge(descricao(item.getProduto()), quantidade, Long::sum);
            }
        }

        BigDecimal compras = compraProdutoService.totalDoUsuario(inicio, fim);
        BigDecimal lucroLiquido = faturamento.subtract(despesas).subtract(compras);

        BigDecimal ticket = finalizadas.isEmpty()
                ? BigDecimal.ZERO
                : faturamento.divide(
                        BigDecimal.valueOf(finalizadas.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        List<DashboardResponse.VolumeTipoProduto> volume = quantidadePorTipo.keySet().stream()
                .sorted()
                .map(tipo -> new DashboardResponse.VolumeTipoProduto(
                        tipo,
                        quantidadePorTipo.getOrDefault(tipo, 0L),
                        faturamentoPorTipo.getOrDefault(tipo, BigDecimal.ZERO)
                ))
                .toList();

        List<DashboardResponse.ProdutoRanking> maisVendidos = ranking.entrySet().stream()
                .sorted((a, b) -> {
                    int porQuantidade = Long.compare(b.getValue(), a.getValue());
                    if (porQuantidade != 0) return porQuantidade;
                    return compararDescricaoProduto(a.getKey(), b.getKey());
                })
                .limit(10)
                .map(e -> new DashboardResponse.ProdutoRanking(e.getKey(), e.getValue()))
                .toList();

        int limite = usuario.getLimiteEstoqueBaixo();

        List<DashboardResponse.EstoqueBaixo> estoqueBaixo = produtoRepository.findByUsuario(usuario).stream()
                .filter(p -> p.getEstoque() <= limite)
                .sorted(Comparator.comparingInt(Produto::getEstoque))
                .map(p -> new DashboardResponse.EstoqueBaixo(
                        descricao(p),
                        p.getEstoque()
                ))
                .toList();

        return new DashboardResponse(
                faturamento,
                BigDecimal.ZERO,
                compras,
                despesas,
                lucroLiquido,
                ticket,
                finalizadas.size(),
                volume,
                maisVendidos,
                estoqueBaixo,
                vendaRepository.findByUsuarioAndStatusVenda(usuario, StatusVenda.EM_ABERTO).stream()
                        .filter(v -> dentroDoPeriodo(v.getData(), inicio, fim)).count(),
                vendaRepository.findByUsuarioAndStatusVenda(usuario, StatusVenda.CANCELADA).stream()
                        .filter(v -> dentroDoPeriodo(v.getData(), inicio, fim)).count()
        );
    }

    private boolean dentroDoPeriodo(LocalDate data, LocalDate inicio, LocalDate fim) {
        if (data == null) return inicio == null && fim == null;
        if (inicio != null && data.isBefore(inicio)) return false;
        return fim == null || !data.isAfter(fim);
    }

    private String nomeTipo(Produto p) {
        if (p.getTipoProdutoCustomizado() != null) {
            return p.getTipoProdutoCustomizado().getNome();
        }
        if (p.getTipoProduto() == null) {
            return "Outro";
        }
        return switch (p.getTipoProduto()) {
            case FIGURINHA -> "Figurinha";
            case ALBUM_COMPLETO -> "Álbum completo";
            case ALBUM_INCOMPLETO -> "Álbum incompleto";
            case CUSTOMIZADO -> p.getTipoProdutoCustomizado() != null ? p.getTipoProdutoCustomizado().getNome() : "Personalizado";
        };
    }

    private int compararDescricaoProduto(String a, String b) {
        java.util.regex.Pattern padrao = java.util.regex.Pattern.compile("^([A-Z]{2,3})?\\s*(\\d+)(?:\\s*-.*)?$");
        java.util.regex.Matcher ma = padrao.matcher(a);
        java.util.regex.Matcher mb = padrao.matcher(b);
        if (ma.matches() && mb.matches()) {
            String pa = ma.group(1) == null ? "" : ma.group(1);
            String pb = mb.group(1) == null ? "" : mb.group(1);
            int porPrefixo = pa.compareToIgnoreCase(pb);
            if (porPrefixo != 0) return porPrefixo;
            return Integer.compare(Integer.parseInt(ma.group(2)), Integer.parseInt(mb.group(2)));
        }
        return a.compareToIgnoreCase(b);
    }

    private String descricao(Produto p) {
        if (p.getTipoProduto() == TipoProduto.FIGURINHA) {
            return (p.getSelecaoFigurinha() != null ? p.getSelecaoFigurinha() + " " : "")
                    + (p.getNumeroFigurinha() != null ? p.getNumeroFigurinha() : "")
                    + (p.getAlbum() != null
                        ? " - " + p.getAlbum().getNomeAlbum() + " " + p.getAlbum().getAno()
                        : "");
        }
        if (p.getNomeProduto() != null) return p.getNomeProduto();
        return nomeTipo(p);
    }
}
