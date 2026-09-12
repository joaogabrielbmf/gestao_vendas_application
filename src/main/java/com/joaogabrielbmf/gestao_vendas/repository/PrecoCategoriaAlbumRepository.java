package com.joaogabrielbmf.gestao_vendas.repository;
import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PrecoCategoriaAlbumRepository extends JpaRepository<PrecoCategoriaAlbum, Integer> {
    Optional<PrecoCategoriaAlbum> findByUsuarioAndAlbumAndCategoria(Usuario usuario, Album album, Categoria categoria);
    List<PrecoCategoriaAlbum> findByUsuario(Usuario usuario);
    Optional<PrecoCategoriaAlbum> findByCodigoPrecoAndUsuario(Integer codigoPreco, Usuario usuario);
}
