package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.AlbumRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AlbumService {
    private final AlbumRepository repository;
    private final TextoService textoService;
    private final UsuarioAtualService usuarioAtualService;

    public AlbumService(AlbumRepository repository, TextoService textoService, UsuarioAtualService usuarioAtualService) {
        this.repository = repository;
        this.textoService = textoService;
        this.usuarioAtualService = usuarioAtualService;
    }

    public Album cadastrar(Album album) {
        Usuario usuario = usuarioAtualService.get();
        String nome = normalizarNomeAlbum(album.getNomeAlbum(), album.getAno());
        if (repository.existsByUsuarioAndNomeAlbumIgnoreCaseAndAno(usuario, nome, album.getAno())) {
            throw new IllegalStateException("Álbum já cadastrado para este ano.");
        }
        album.setCodigoAlbum(null);
        album.setUsuario(usuario);
        album.setNomeAlbum(nome);
        return repository.save(album);
    }

    public List<Album> mostrarTodos() { return repository.findByUsuarioOrderByNomeAlbumAscAnoAsc(usuarioAtualService.get()); }
    public Optional<Album> buscarPorID(int codigo) { return repository.findByCodigoAlbumAndUsuario(codigo, usuarioAtualService.get()); }

    public Album editar(int codigo, Album atualizado) {
        Usuario usuario = usuarioAtualService.get();
        Album atual = repository.findByCodigoAlbumAndUsuario(codigo, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Álbum não encontrado."));
        String nome = normalizarNomeAlbum(atualizado.getNomeAlbum(), atualizado.getAno());
        if (repository.existsByUsuarioAndNomeAlbumIgnoreCaseAndAnoAndCodigoAlbumNot(usuario, nome, atualizado.getAno(), codigo)) {
            throw new IllegalStateException("Álbum já cadastrado para este ano.");
        }
        atual.setNomeAlbum(nome);
        atual.setAno(atualizado.getAno());
        atual.setTipoAlbum(atualizado.getTipoAlbum());
        return repository.save(atual);
    }

    public void deletar(int codigo) {
        Album atual = repository.findByCodigoAlbumAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Álbum não encontrado."));
        repository.delete(atual);
    }

    private String normalizarNomeAlbum(String nome, Integer ano) {
        String normalizado = textoService.titulo(nome);
        if (normalizado == null || normalizado.isBlank()) throw new IllegalArgumentException("Nome do álbum é obrigatório.");
        return normalizado;
    }
}
