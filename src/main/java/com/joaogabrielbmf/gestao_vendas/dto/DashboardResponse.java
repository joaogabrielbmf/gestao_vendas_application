package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class DashboardResponse {
    private BigDecimal faturamento;
    private BigDecimal custoProdutos;
    private BigDecimal comprasProdutos;
    private BigDecimal taxasDespesas;
    private BigDecimal lucroLiquido;
    private BigDecimal ticketMedio;
    private long vendasFinalizadas;
    private List<VolumeTipoProduto> volumePorTipo;
    private List<ProdutoRanking> maisVendidos;
    private List<EstoqueBaixo> estoqueBaixo;
    private long vendasEmAberto;
    private long vendasCanceladas;

    @Getter @Setter @AllArgsConstructor
    public static class VolumeTipoProduto { private String tipo; private long quantidade; private BigDecimal faturamento; }
    @Getter @Setter @AllArgsConstructor
    public static class ProdutoRanking { private String produto; private long quantidade; }
    @Getter @Setter @AllArgsConstructor
    public static class EstoqueBaixo { private String produto; private int estoque; }
}
