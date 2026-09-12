package com.joaogabrielbmf.gestao_vendas.dto;

import com.joaogabrielbmf.gestao_vendas.model.TipoProduto;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProdutoRequest {
    private TipoProduto tipoProduto;
    private Integer codigoTipoCustomizado;
    private Integer codigoAlbum;
    private Integer codigoCategoria;
    private String selecaoFigurinha;
    private Integer numeroFigurinha;
    private String nomeProduto;
    private BigDecimal precoEspecifico;
    private Integer estoque;

    private BigDecimal custoEspecifico;
    private BigDecimal custoMedio;
}
