package com.joaogabrielbmf.gestao_vendas.repository;

import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TipoProdutoCustomizadoRepository extends JpaRepository<TipoProdutoCustomizado, Integer> {
    List<TipoProdutoCustomizado> findByUsuarioOrderByNomeAsc(Usuario usuario);
    Optional<TipoProdutoCustomizado> findByCodigoTipoProdutoAndUsuario(Integer codigoTipoProduto, Usuario usuario);
    boolean existsByUsuarioAndNomeNormalizado(Usuario usuario, String nomeNormalizado);
    boolean existsByUsuarioAndNomeNormalizadoAndCodigoTipoProdutoNot(Usuario usuario, String nomeNormalizado, Integer codigoTipoProduto);
}
