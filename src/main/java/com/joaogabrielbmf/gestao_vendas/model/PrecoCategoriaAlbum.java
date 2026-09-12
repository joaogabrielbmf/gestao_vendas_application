package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"codigo_album", "codigo_categoria"})
})
public class PrecoCategoriaAlbum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_preco")
    private Integer codigoPreco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "codigo_album")
    private Album album;

    @ManyToOne(optional = false)
    @JoinColumn(name = "codigo_categoria")
    private Categoria categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
