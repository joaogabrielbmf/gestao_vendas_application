package com.joaogabrielbmf.gestao_vendas.controller;
import com.joaogabrielbmf.gestao_vendas.model.RegraShopeeRegressiva;
import com.joaogabrielbmf.gestao_vendas.service.RegraShopeeRegressivaService;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/regras-shopee")
public class RegraShopeeRegressivaController{
    private final RegraShopeeRegressivaService s;
    public RegraShopeeRegressivaController(RegraShopeeRegressivaService s){this.s=s;}
    @PostMapping public RegraShopeeRegressiva cadastrar(@RequestBody RegraShopeeRegressiva r){return s.cadastrar(r);}
    @GetMapping public List<RegraShopeeRegressiva> listar(){return s.listar();}
    @DeleteMapping("/{id}") public ResponseEntity<Void> deletar(@PathVariable("id") int id){s.deletar(id);return ResponseEntity.noContent().build();}
}
