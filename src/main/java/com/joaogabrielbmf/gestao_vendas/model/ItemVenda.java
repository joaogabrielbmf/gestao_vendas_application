package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @Entity
public class ItemVenda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_item_venda")
    private Integer codigoItemVenda;

    @ManyToOne(optional = false) @JoinColumn(name = "codigo_venda")
    private Venda venda;

    @ManyToOne(optional = false) @JoinColumn(name = "codigo_produto")
    private Produto produto;

    private int quantidade;

    @Column(name = "valor_unitario", precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "custo_unitario", precision = 12, scale = 2)
    private BigDecimal custoUnitario;

    @Column(name="taxa_shopee_unitaria", precision=12, scale=2)
    private BigDecimal taxaShopeeUnitaria;
}
