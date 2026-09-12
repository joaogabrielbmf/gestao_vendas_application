package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.ConfiguracaoVenda;
import com.joaogabrielbmf.gestao_vendas.service.ConfiguracaoVendaService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/configuracoes-venda")
public class ConfiguracaoVendaController {
    private final ConfiguracaoVendaService service;
    public ConfiguracaoVendaController(ConfiguracaoVendaService service) { this.service = service; }

    @PostMapping public ConfiguracaoVenda cadastrar(@RequestBody ConfiguracaoVenda c) { return service.cadastrar(c); }
    @GetMapping public List<ConfiguracaoVenda> listar() { return service.listar(); }
    @GetMapping("/{id}") public ResponseEntity<ConfiguracaoVenda> buscar(@PathVariable("id") int id) { return service.buscar(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public ConfiguracaoVenda editar(@PathVariable("id") int id, @RequestBody ConfiguracaoVenda c) { return service.editar(id, c); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id) { service.deletar(id); return ResponseEntity.noContent().build(); }
}
