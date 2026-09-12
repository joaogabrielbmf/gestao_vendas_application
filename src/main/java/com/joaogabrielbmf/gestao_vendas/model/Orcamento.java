package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Orcamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_orcamento")
    private Integer codigoOrcamento;

    @ManyToOne
    @JoinColumn(name = "codigo_cliente")
    private Cliente cliente;

    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_orcamento")
    private StatusOrcamento statusOrcamento = StatusOrcamento.EM_ABERTO;

    @OneToOne
    @JoinColumn(name = "codigo_venda_convertida")
    private Venda vendaConvertida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
