package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.Album;
import com.joaogabrielbmf.gestao_vendas.service.AlbumService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/albuns")
public class AlbumController {
    private final AlbumService service;
    public AlbumController(AlbumService service) { this.service = service; }

    @PostMapping public Album cadastrar(@RequestBody Album a) { return service.cadastrar(a); }
    @GetMapping public List<Album> listar() { return service.mostrarTodos(); }
    @GetMapping("/{id}") public ResponseEntity<Album> buscar(@PathVariable("id") int id) { return service.buscarPorID(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public Album editar(@PathVariable("id") int id, @RequestBody Album a) { return service.editar(id, a); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id) { service.deletar(id); return ResponseEntity.noContent().build(); }
}
