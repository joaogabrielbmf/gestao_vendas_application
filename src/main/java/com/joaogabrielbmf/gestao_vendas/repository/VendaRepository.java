package com.joaogabrielbmf.gestao_vendas.repository;

import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.*;

public interface VendaRepository extends JpaRepository<Venda,Integer>, JpaSpecificationExecutor<Venda> {
    List<Venda> findByUsuario(Usuario usuario);
    Optional<Venda> findByCodigoVendaAndUsuario(Integer codigoVenda, Usuario usuario);
    List<Venda> findByUsuarioAndStatusVenda(Usuario usuario, StatusVenda statusVenda);
}
