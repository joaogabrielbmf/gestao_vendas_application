package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ItemVendaRequest {
    private Integer codigoProduto;
    private Integer quantidade;
}
