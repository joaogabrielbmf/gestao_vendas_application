package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_produto")
    private Integer codigoProduto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto", nullable = false)
    private TipoProduto tipoProduto;

    @ManyToOne
    @JoinColumn(name = "codigo_tipo_produto_customizado")
    private TipoProdutoCustomizado tipoProdutoCustomizado;

    @Column(name = "numero_figurinha")
    private Integer numeroFigurinha;

    @Column(name = "selecao_figurinha")
    private String selecaoFigurinha;

    @Column(name = "nome_produto")
    private String nomeProduto;

    @Column(name = "preco_especifico", precision = 12, scale = 2)
    private BigDecimal precoEspecifico;

    @Column(name = "custo_medio", precision = 12, scale = 2)
    private BigDecimal custoMedio;

    @Column(name = "custo_especifico", precision = 12, scale = 2)
    private BigDecimal custoEspecifico;

    @Column(nullable = false)
    private int estoque;

    @ManyToOne
    @JoinColumn(name = "codigo_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "codigo_album")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
