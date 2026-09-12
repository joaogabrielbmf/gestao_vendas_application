package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_cliente")
    private Integer codigoCliente;

    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    @Column(name = "email_cliente")
    private String emailCliente;

    @Column(name = "telefone_cliente")
    private String telefoneCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
    // Métodos explícitos para não depender do processamento de anotações do Lombok
    // na compilação/IDE para esta entidade.
    public Integer getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(Integer codigoCliente) { this.codigoCliente = codigoCliente; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

    public String getTelefoneCliente() { return telefoneCliente; }
    public void setTelefoneCliente(String telefoneCliente) { this.telefoneCliente = telefoneCliente; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

}

