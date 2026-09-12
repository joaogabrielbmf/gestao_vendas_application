package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class CompraProdutoService {
    private final CompraProdutoRepository repository;
    private final UsuarioAtualService usuarioAtualService;
    private final TextoService textoService;

    public CompraProdutoService(CompraProdutoRepository repository,
                                UsuarioAtualService usuarioAtualService,
                                TextoService textoService) {
        this.repository = repository;
        this.usuarioAtualService = usuarioAtualService;
        this.textoService = textoService;
    }

    public CompraProduto cadastrar(CompraProduto compra) {
        validar(compra);
        compra.setCodigoCompra(null);
        compra.setData(compra.getData() == null ? LocalDate.now() : compra.getData());
        compra.setDescricao(textoService.titulo(compra.getDescricao()));
        compra.setObservacao(compra.getObservacao() == null ? null : compra.getObservacao().trim());
        compra.setUsuario(usuarioAtualService.get());
        return repository.save(compra);
    }

    public List<CompraProduto> listar() {
        return repository.findByUsuarioOrderByDataDescCodigoCompraDesc(usuarioAtualService.get());
    }

    public CompraProduto editar(int codigo, CompraProduto novo) {
        CompraProduto atual = repository.findByCodigoCompraAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Compra não encontrada."));
        validar(novo);
        atual.setData(novo.getData() == null ? atual.getData() : novo.getData());
        atual.setDescricao(textoService.titulo(novo.getDescricao()));
        atual.setValorTotal(novo.getValorTotal());
        atual.setObservacao(novo.getObservacao() == null ? null : novo.getObservacao().trim());
        return repository.save(atual);
    }

    public void deletar(int codigo) {
        CompraProduto atual = repository.findByCodigoCompraAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Compra não encontrada."));
        repository.delete(atual);
    }

    public BigDecimal totalDoUsuario() {
        return totalDoUsuario(null, null);
    }

    public BigDecimal totalDoUsuario(LocalDate inicio, LocalDate fim) {
        return listar().stream()
                .filter(c -> inicio == null || !c.getData().isBefore(inicio))
                .filter(c -> fim == null || !c.getData().isAfter(fim))
                .map(CompraProduto::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validar(CompraProduto compra) {
        if (compra.getDescricao() == null || compra.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (compra.getValorTotal() == null || compra.getValorTotal().signum() < 0) {
            throw new IllegalArgumentException("Valor total inválido.");
        }
    }
}
