package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.dto.DashboardResponse;
import com.joaogabrielbmf.gestao_vendas.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse resumo(
            @RequestParam(value = "inicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.resumo(inicio, fim);
    }
}
