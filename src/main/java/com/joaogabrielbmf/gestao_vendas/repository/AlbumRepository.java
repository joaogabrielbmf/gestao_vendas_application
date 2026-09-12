package com.joaogabrielbmf.gestao_vendas.repository;

import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AlbumRepository extends JpaRepository<Album, Integer> {
    List<Album> findByUsuarioOrderByNomeAlbumAscAnoAsc(Usuario usuario);
    Optional<Album> findByCodigoAlbumAndUsuario(Integer codigoAlbum, Usuario usuario);
    boolean existsByUsuarioAndNomeAlbumIgnoreCaseAndAno(Usuario usuario, String nomeAlbum, Integer ano);
    boolean existsByUsuarioAndNomeAlbumIgnoreCaseAndAnoAndCodigoAlbumNot(Usuario usuario, String nomeAlbum, Integer ano, Integer codigoAlbum);
    Optional<Album> findByUsuarioAndTipoAlbumAndAnoAndNomeAlbumIgnoreCase(Usuario usuario, TipoAlbum tipoAlbum, Integer ano, String nomeAlbum);
}
