package com.joaogabrielbmf.gestao_vendas.dto;

public record CatalogoResumoResponse(
        String chave,
        String nome,
        String tipoAlbum,
        Integer ano,
        int quantidadeItens,
        boolean importado
) {}
