package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ItemOrcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_item_orcamento")
    private Integer codigoItemOrcamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "codigo_orcamento")
    private Orcamento orcamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "codigo_produto")
    private Produto produto;

    private int quantidade;

    @Column(name = "valor_unitario", precision = 12, scale = 2)
    private BigDecimal valorUnitario;
}
