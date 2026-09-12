package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.dto.PrecoCategoriaAlbumRequest;
import com.joaogabrielbmf.gestao_vendas.model.PrecoCategoriaAlbum;
import com.joaogabrielbmf.gestao_vendas.service.PrecoCategoriaAlbumService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/precos-categoria-album")
public class PrecoCategoriaAlbumController {
    private final PrecoCategoriaAlbumService service;
    public PrecoCategoriaAlbumController(PrecoCategoriaAlbumService service) { this.service = service; }

    @PostMapping public PrecoCategoriaAlbum cadastrar(@RequestBody PrecoCategoriaAlbumRequest r) { return service.cadastrar(r); }
    @GetMapping public List<PrecoCategoriaAlbum> listar() { return service.mostrarTodos(); }
    @GetMapping("/{id}") public ResponseEntity<PrecoCategoriaAlbum> buscar(@PathVariable("id") int id) { return service.buscarPorID(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }
    @PutMapping("/{id}") public PrecoCategoriaAlbum editar(@PathVariable("id") int id, @RequestBody PrecoCategoriaAlbumRequest r) { return service.editar(id, r.getValor()); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id) { service.deletar(id); return ResponseEntity.noContent().build(); }
}
