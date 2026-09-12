package com.joaogabrielbmf.gestao_vendas.repository;
import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ItemOrcamentoRepository extends JpaRepository<ItemOrcamento, Integer> {
    List<ItemOrcamento> findByOrcamento(Orcamento orcamento);
}
