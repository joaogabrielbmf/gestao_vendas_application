package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_categoria")
    private Integer codigoCategoria;

    @Column(name = "nome_categoria", nullable = false)
    private String nomeCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
