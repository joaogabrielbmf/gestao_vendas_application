package com.joaogabrielbmf.gestao_vendas.repository;
import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByUsuarioOrderByNomeClienteAsc(Usuario usuario);
    Optional<Cliente> findByCodigoClienteAndUsuario(Integer codigoCliente, Usuario usuario);
}
