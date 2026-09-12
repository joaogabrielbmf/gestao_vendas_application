package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class VendaRequest {
    private Integer codigoCliente;
    private Integer codigoConfiguracao;
    private boolean fretePagoVendedor;
    private BigDecimal frete;
    private List<ItemVendaRequest> itens;
}
