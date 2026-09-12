package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ItemOrcamentoRequest {
    private Integer codigoProduto;
    private Integer quantidade;
}
