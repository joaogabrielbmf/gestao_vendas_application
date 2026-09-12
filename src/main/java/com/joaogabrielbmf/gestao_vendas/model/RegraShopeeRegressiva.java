package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"valor_item"}))
public class RegraShopeeRegressiva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="codigo_regra_shopee")
    private Integer codigoRegraShopee;

    @Column(name="valor_item", precision=12, scale=2, nullable=false)
    private BigDecimal valorItem;

    @Column(name="taxa_total", precision=12, scale=2, nullable=false)
    private BigDecimal taxaTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
