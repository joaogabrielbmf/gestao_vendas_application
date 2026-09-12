package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "tipo_produto_customizado",
    uniqueConstraints = @UniqueConstraint(columnNames = {"codigo_usuario", "nome_normalizado"})
)
public class TipoProdutoCustomizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_tipo_produto")
    private Integer codigoTipoProduto;

    @Column(nullable = false)
    private String nome;

    @Column(name = "nome_normalizado", nullable = false)
    private String nomeNormalizado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "codigo_usuario", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
