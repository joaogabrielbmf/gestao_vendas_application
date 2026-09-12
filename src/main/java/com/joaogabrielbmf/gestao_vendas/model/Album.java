package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_album")
    private Integer codigoAlbum;

    private Integer ano;

    @Column(name = "nome_album", nullable = false)
    private String nomeAlbum;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_album")
    private TipoAlbum tipoAlbum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
