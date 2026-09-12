package com.joaogabrielbmf.gestao_vendas.repository;

import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CompraProdutoRepository extends JpaRepository<CompraProduto, Integer> {
    List<CompraProduto> findByUsuarioOrderByDataDescCodigoCompraDesc(Usuario usuario);
    Optional<CompraProduto> findByCodigoCompraAndUsuario(Integer codigoCompra, Usuario usuario);
}
