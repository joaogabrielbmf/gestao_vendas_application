package com.joaogabrielbmf.gestao_vendas.dto;

import java.util.List;

public record AplicarCategoriaCatalogoRequest(
        Integer codigoCategoria,
        List<Integer> codigosProdutos
) {}
