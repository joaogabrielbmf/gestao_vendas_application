package com.joaogabrielbmf.gestao_vendas.dto;

import com.joaogabrielbmf.gestao_vendas.model.Produto;
import lombok.*;

@Getter
@AllArgsConstructor
public class ProdutoVendidoResponse {
    private Produto produto;
    private long quantidadeVendida;
}
