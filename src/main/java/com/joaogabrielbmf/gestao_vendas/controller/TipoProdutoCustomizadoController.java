package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.TipoProdutoCustomizado;
import com.joaogabrielbmf.gestao_vendas.service.TipoProdutoCustomizadoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/tipos-produto")
public class TipoProdutoCustomizadoController {
    private final TipoProdutoCustomizadoService service;

    public TipoProdutoCustomizadoController(TipoProdutoCustomizadoService service) {
        this.service = service;
    }

    @PostMapping
    public TipoProdutoCustomizado cadastrar(@RequestBody TipoProdutoCustomizado tipo) {
        return service.cadastrar(tipo);
    }

    @GetMapping
    public List<TipoProdutoCustomizado> listar() {
        return service.listar();
    }

    @PutMapping("/{id}")
    public TipoProdutoCustomizado editar(@PathVariable("id") int id, @RequestBody TipoProdutoCustomizado tipo) {
        return service.editar(id, tipo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") int id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
