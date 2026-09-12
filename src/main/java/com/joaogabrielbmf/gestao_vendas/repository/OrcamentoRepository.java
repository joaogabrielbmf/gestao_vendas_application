package com.joaogabrielbmf.gestao_vendas.repository;
import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OrcamentoRepository extends JpaRepository<Orcamento, Integer> {
    List<Orcamento> findByUsuario(Usuario usuario);
    Optional<Orcamento> findByCodigoOrcamentoAndUsuario(Integer codigoOrcamento, Usuario usuario);
}
