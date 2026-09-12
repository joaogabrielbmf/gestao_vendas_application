package com.joaogabrielbmf.gestao_vendas.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PrecoCategoriaAlbumRequest {
    private Integer codigoAlbum;
    private Integer codigoCategoria;
    private BigDecimal valor;
}
