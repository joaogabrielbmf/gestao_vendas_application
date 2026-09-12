package com.joaogabrielbmf.gestao_vendas.repository;
import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.*;
public interface RegraShopeeRegressivaRepository extends JpaRepository<RegraShopeeRegressiva,Integer>{
    Optional<RegraShopeeRegressiva> findByUsuarioAndValorItem(Usuario usuario, BigDecimal valorItem);
    List<RegraShopeeRegressiva> findByUsuarioOrderByValorItemAsc(Usuario usuario);
    Optional<RegraShopeeRegressiva> findByCodigoRegraShopeeAndUsuario(Integer codigoRegraShopee, Usuario usuario);
}
