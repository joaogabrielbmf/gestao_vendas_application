package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ConverterOrcamentoRequest {
    private Integer codigoConfiguracao;
    private boolean fretePagoVendedor;
    private BigDecimal frete;
}
