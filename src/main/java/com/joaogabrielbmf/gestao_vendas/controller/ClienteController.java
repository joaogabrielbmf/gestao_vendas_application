package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.Cliente;
import com.joaogabrielbmf.gestao_vendas.service.ClienteService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service) { this.service = service; }

    @PostMapping public Cliente cadastrar(@RequestBody Cliente c) { return service.cadastrar(c); }
    @GetMapping public List<Cliente> listar() { return service.listar(); }
    @GetMapping("/{id}") public ResponseEntity<Cliente> buscar(@PathVariable("id") int id) { return service.buscar(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public Cliente editar(@PathVariable("id") int id, @RequestBody Cliente c) { return service.editar(id, c); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id) { service.deletar(id); return ResponseEntity.noContent().build(); }
}
