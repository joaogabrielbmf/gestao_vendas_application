package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="codigo_usuario") private Integer codigoUsuario;
 @Column(nullable=false) private String nome;
 @Column(nullable=false) private String email;
 @Column(nullable=false) @com.fasterxml.jackson.annotation.JsonIgnore private String senhaHash;
 @Column(name="limite_estoque_baixo",nullable=false) private Integer limiteEstoqueBaixo=3;
}
