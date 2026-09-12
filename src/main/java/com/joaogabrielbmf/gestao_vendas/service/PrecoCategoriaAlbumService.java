package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.dto.PrecoCategoriaAlbumRequest;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class PrecoCategoriaAlbumService {
    private final PrecoCategoriaAlbumRepository repository;
    private final AlbumRepository albumRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioAtualService usuarioAtualService;

    public PrecoCategoriaAlbumService(PrecoCategoriaAlbumRepository repository, AlbumRepository albumRepository,
                                      CategoriaRepository categoriaRepository, UsuarioAtualService usuarioAtualService) {
        this.repository = repository;
        this.albumRepository = albumRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioAtualService = usuarioAtualService;
    }

    public PrecoCategoriaAlbum cadastrar(PrecoCategoriaAlbumRequest request) {
        Usuario usuario = usuarioAtualService.get();
        Album album = albumRepository.findByCodigoAlbumAndUsuario(request.getCodigoAlbum(), usuario).orElseThrow(() -> new IllegalArgumentException("Álbum não encontrado."));
        Categoria categoria = categoriaRepository.findByCodigoCategoriaAndUsuario(request.getCodigoCategoria(), usuario).orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
        if (repository.findByUsuarioAndAlbumAndCategoria(usuario, album, categoria).isPresent()) throw new IllegalStateException("Já existe preço para esta combinação de álbum e categoria.");
        PrecoCategoriaAlbum p = new PrecoCategoriaAlbum();
        p.setUsuario(usuario);
        p.setAlbum(album);
        p.setCategoria(categoria);
        p.setValor(request.getValor());
        return repository.save(p);
    }

    public List<PrecoCategoriaAlbum> mostrarTodos() { return repository.findByUsuario(usuarioAtualService.get()); }
    public Optional<PrecoCategoriaAlbum> buscarPorID(int codigo) { return repository.findByCodigoPrecoAndUsuario(codigo, usuarioAtualService.get()); }

    public PrecoCategoriaAlbum editar(int codigo, BigDecimal valor) {
        PrecoCategoriaAlbum p = repository.findByCodigoPrecoAndUsuario(codigo, usuarioAtualService.get()).orElseThrow(() -> new IllegalArgumentException("Preço não encontrado."));
        p.setValor(valor);
        return repository.save(p);
    }

    public void deletar(int codigo) {
        PrecoCategoriaAlbum p = repository.findByCodigoPrecoAndUsuario(codigo, usuarioAtualService.get()).orElseThrow(() -> new IllegalArgumentException("Preço não encontrado."));
        repository.delete(p);
    }
}
