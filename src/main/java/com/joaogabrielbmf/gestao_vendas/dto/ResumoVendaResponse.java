package com.joaogabrielbmf.gestao_vendas.dto;

import com.joaogabrielbmf.gestao_vendas.model.StatusVenda;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @AllArgsConstructor
public class ResumoVendaResponse {
    private Integer codigoVenda;
    private StatusVenda status;
    private BigDecimal faturamento;
    private BigDecimal custoProdutos;
    private BigDecimal taxaShopee;
    private BigDecimal taxaPercentual;
    private BigDecimal taxaFixa;
    private BigDecimal embalagem;
    private BigDecimal freteVendedor;
    private BigDecimal despesasTotais;
    private BigDecimal lucro;
}
