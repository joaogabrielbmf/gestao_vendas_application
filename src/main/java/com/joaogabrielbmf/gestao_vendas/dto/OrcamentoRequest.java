package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrcamentoRequest {
    private Integer codigoCliente;
    private List<ItemOrcamentoRequest> itens;
}
