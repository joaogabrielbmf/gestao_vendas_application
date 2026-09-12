package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.dto.*;
import com.joaogabrielbmf.gestao_vendas.service.CatalogoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/catalogos")
public class CatalogoController {
    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CatalogoResumoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{chave}/itens")
    public List<CatalogoItemResponse> itens(@PathVariable("chave") String chave) {
        return service.itens(chave);
    }

    @PostMapping("/{chave}/importar")
    public Map<String, Object> importar(@PathVariable("chave") String chave) {
        return service.importar(chave);
    }

    @PutMapping("/{chave}/categorias")
    public Map<String, Object> aplicarCategoria(@PathVariable("chave") String chave,
                                                 @RequestBody AplicarCategoriaCatalogoRequest request) {
        return service.aplicarCategoria(chave, request);
    }
}
