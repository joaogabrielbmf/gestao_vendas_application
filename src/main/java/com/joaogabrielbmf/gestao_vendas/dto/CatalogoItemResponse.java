package com.joaogabrielbmf.gestao_vendas.dto;

public record CatalogoItemResponse(
        String codigo,
        String grupoNome,
        String prefixo,
        Integer numero,
        Integer codigoProduto,
        Integer codigoCategoria,
        String categoria
) {}
