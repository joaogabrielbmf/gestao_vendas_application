package com.joaogabrielbmf.gestao_vendas.repository;

import com.joaogabrielbmf.gestao_vendas.model.ConfiguracaoVenda;
import com.joaogabrielbmf.gestao_vendas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfiguracaoVendaRepository extends JpaRepository<ConfiguracaoVenda, Integer> {
    List<ConfiguracaoVenda> findByUsuarioOrderByNomeAsc(Usuario usuario);
    Optional<ConfiguracaoVenda> findByCodigoConfiguracaoAndUsuario(Integer codigoConfiguracao, Usuario usuario);
}
