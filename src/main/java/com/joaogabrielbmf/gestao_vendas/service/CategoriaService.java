package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CategoriaService {
    private final CategoriaRepository repository;
    private final TextoService textoService;
    private final UsuarioAtualService usuarioAtualService;

    public CategoriaService(CategoriaRepository repository, TextoService textoService, UsuarioAtualService usuarioAtualService) {
        this.repository = repository;
        this.textoService = textoService;
        this.usuarioAtualService = usuarioAtualService;
    }

    public Categoria cadastrar(Categoria categoria) {
        Usuario usuario = usuarioAtualService.get();
        String nome = normalizar(categoria.getNomeCategoria());
        if (repository.existsByUsuarioAndNomeCategoriaIgnoreCase(usuario, nome)) throw new IllegalStateException("Categoria já cadastrada.");
        categoria.setCodigoCategoria(null);
        categoria.setUsuario(usuario);
        categoria.setNomeCategoria(nome);
        return repository.save(categoria);
    }

    public List<Categoria> mostrarTodas() { return repository.findByUsuarioOrderByNomeCategoriaAsc(usuarioAtualService.get()); }
    public Optional<Categoria> buscarPorID(int codigo) { return repository.findByCodigoCategoriaAndUsuario(codigo, usuarioAtualService.get()); }

    public Categoria editar(int codigo, Categoria atualizado) {
        Usuario usuario = usuarioAtualService.get();
        Categoria atual = repository.findByCodigoCategoriaAndUsuario(codigo, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
        String nome = normalizar(atualizado.getNomeCategoria());
        if (repository.existsByUsuarioAndNomeCategoriaIgnoreCaseAndCodigoCategoriaNot(usuario, nome, codigo)) throw new IllegalStateException("Categoria já cadastrada.");
        atual.setNomeCategoria(nome);
        return repository.save(atual);
    }

    public void deletar(int codigo) {
        Categoria atual = repository.findByCodigoCategoriaAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
        repository.delete(atual);
    }

    private String normalizar(String nome) {
        String normalizado = textoService.titulo(nome);
        if (normalizado == null || normalizado.isBlank()) throw new IllegalArgumentException("Nome da categoria é obrigatório.");
        return normalizado;
    }
}
