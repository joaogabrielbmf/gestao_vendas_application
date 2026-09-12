package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "compra_produto")
public class CompraProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_compra")
    private Integer codigoCompra;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @Column(length = 1000)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_usuario", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
