package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.dto.*;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.service.OrcamentoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/orcamentos")
public class OrcamentoController {
    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @PostMapping
    public Orcamento cadastrar(@RequestBody OrcamentoRequest request) {
        return service.cadastrar(request);
    }

    @GetMapping
    public List<Orcamento> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscar(@PathVariable("id") int id) {
        return service.buscar(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/itens")
    public List<ItemOrcamento> itens(@PathVariable("id") int id) {
        return service.itens(id);
    }

    @GetMapping("/{id}/total")
    public BigDecimal total(@PathVariable("id") int id) {
        return service.total(id);
    }

    @PostMapping("/{id}/converter")
    public Venda converter(@PathVariable("id") int id,
                           @RequestBody ConverterOrcamentoRequest request) {
        return service.converterEmVenda(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") int id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
