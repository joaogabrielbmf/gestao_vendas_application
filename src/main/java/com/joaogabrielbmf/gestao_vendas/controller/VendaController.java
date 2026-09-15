package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.dto.*;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.service.VendaService;
import org.springframework.http.*;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/vendas")
public class VendaController {
    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping
    public Venda criar(@RequestBody VendaRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<Venda> listar() {
        return service.listar();
    }

    @GetMapping("/historico")
    public Page<Venda> historico(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(name = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "cliente", required = false) Integer cliente,
            @RequestParam(name = "canal", required = false) Integer canal,
            @RequestParam(name = "tipo", required = false) String tipoProduto,
            @RequestParam(name = "ordenarPor", required = false) String ordenarPor,
            @RequestParam(name = "direcao", defaultValue = "desc") String direcao) {

        return service.buscarHistoricoPaginado(
                page, size, dataInicio, dataFim, status,
                cliente, canal, tipoProduto, ordenarPor, direcao
        );
    }

    @GetMapping("/produtos-vendidos")
    public List<ProdutoVendidoResponse> produtosVendidos(
            @RequestParam(name = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(name = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "album", required = false) Integer album,
            @RequestParam(name = "ano", required = false) Integer ano,
            @RequestParam(name = "selecao", required = false) String selecao,
            @RequestParam(name = "numero", required = false) Integer numero) {
        return service.produtosVendidos(dataInicio, dataFim, tipo, album, ano, selecao, numero);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscar(@PathVariable("id") int id) {
        return service.buscar(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/itens")
    public List<ItemVenda> itens(@PathVariable("id") int id) {
        return service.itens(id);
    }

    @GetMapping("/{id}/resumo")
    public ResumoVendaResponse resumo(@PathVariable("id") int id) {
        return service.resumo(id);
    }

    @PostMapping("/{id}/finalizar")
    public Venda finalizar(@PathVariable("id") int id) {
        return service.finalizar(id);
    }

    @PostMapping("/{id}/cancelar")
    public Venda cancelar(@PathVariable("id") int id) {
        return service.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") int id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
