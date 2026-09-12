package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.dto.ProdutoRequest;
import com.joaogabrielbmf.gestao_vendas.model.Produto;
import com.joaogabrielbmf.gestao_vendas.service.ProdutoService;
import org.springframework.http.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoService service;
    public ProdutoController(ProdutoService service) { this.service = service; }

    @PostMapping public Produto cadastrar(@RequestBody ProdutoRequest r) { return service.cadastrar(r); }
    @GetMapping public List<Produto> listar() { return service.mostrarTodos(); }

    @GetMapping("/pagina")
    public Page<Produto> listarPaginado(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "album", required = false) String album,
            @RequestParam(name = "ano", required = false) Integer ano,
            @RequestParam(name = "selecao", required = false) String selecao,
            @RequestParam(name = "categoria", required = false) Integer categoria,
            @RequestParam(name = "estoque", required = false) String estoque,
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "ordenarPor", required = false) String ordenarPor,
            @RequestParam(name = "direcao", defaultValue = "asc") String direcao) {

        return service.buscarPaginado(
                page, size, tipo, album, ano, selecao, categoria,
                estoque, busca, ordenarPor, direcao
        );
    }
    @GetMapping("/{id}") public ResponseEntity<Produto> buscar(@PathVariable("id") int id) { return service.buscarPorID(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); }
    @GetMapping("/{id}/preco") public BigDecimal preco(@PathVariable("id") int id) { return service.buscarPrecoAtual(id); }
    @PutMapping("/{id}") public Produto editar(@PathVariable("id") int id, @RequestBody ProdutoRequest r) { return service.editar(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id) { service.deletar(id); return ResponseEntity.noContent().build(); }

    @PatchMapping("/{id}/estoque")
    public Produto atualizarEstoque(@PathVariable("id") int id, @RequestBody Map<String, Integer> body) {
        Integer estoque = body.get("estoque");
        if (estoque == null) throw new IllegalArgumentException("Estoque é obrigatório.");
        return service.atualizarEstoque(id, estoque);
    }
}
