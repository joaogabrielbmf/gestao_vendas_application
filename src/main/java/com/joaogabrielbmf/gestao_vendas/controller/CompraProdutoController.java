package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.CompraProduto;
import com.joaogabrielbmf.gestao_vendas.service.CompraProdutoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/compras-produtos")
public class CompraProdutoController {
    private final CompraProdutoService service;

    public CompraProdutoController(CompraProdutoService service) {
        this.service = service;
    }

    @PostMapping
    public CompraProduto cadastrar(@RequestBody CompraProduto compra) {
        return service.cadastrar(compra);
    }

    @GetMapping
    public List<CompraProduto> listar() {
        return service.listar();
    }

    @PutMapping("/{id}")
    public CompraProduto editar(@PathVariable("id") int id, @RequestBody CompraProduto compra) {
        return service.editar(id, compra);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") int id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
