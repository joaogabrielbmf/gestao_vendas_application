package com.joaogabrielbmf.gestao_vendas.repository;

import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findByUsuarioOrderByNomeCategoriaAsc(Usuario usuario);
    Optional<Categoria> findByCodigoCategoriaAndUsuario(Integer codigoCategoria, Usuario usuario);
    boolean existsByUsuarioAndNomeCategoriaIgnoreCase(Usuario usuario, String nomeCategoria);
    boolean existsByUsuarioAndNomeCategoriaIgnoreCaseAndCodigoCategoriaNot(Usuario usuario, String nomeCategoria, Integer codigoCategoria);
    Optional<Categoria> findByUsuarioAndNomeCategoriaIgnoreCase(Usuario usuario, String nomeCategoria);
}
