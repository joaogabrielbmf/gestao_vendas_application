package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.Categoria;
import com.joaogabrielbmf.gestao_vendas.service.CategoriaService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {
    private final CategoriaService service;
    public CategoriaController(CategoriaService service) { this.service = service; }

    @PostMapping public Categoria cadastrar(@RequestBody Categoria c) { return service.cadastrar(c); }
    @GetMapping public List<Categoria> listar() { return service.mostrarTodas(); }
    @GetMapping("/{id}") public ResponseEntity<Categoria> buscar(@PathVariable("id") int id) { return service.buscarPorID(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public Categoria editar(@PathVariable("id") int id, @RequestBody Categoria c) { return service.editar(id, c); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id) { service.deletar(id); return ResponseEntity.noContent().build(); }
}
