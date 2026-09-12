package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @Entity
public class Venda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_venda")
    private Integer codigoVenda;

    @ManyToOne @JoinColumn(name = "codigo_cliente")
    private Cliente cliente;

    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_venda")
    private StatusVenda statusVenda = StatusVenda.EM_ABERTO;

    @ManyToOne @JoinColumn(name = "codigo_configuracao")
    private ConfiguracaoVenda configuracaoVenda;

    @Column(precision = 12, scale = 2)
    private BigDecimal embalagem;

    @Column(name = "taxa_percentual", precision = 8, scale = 4)
    private BigDecimal taxaPercentual;

    @Column(name = "taxa_fixa", precision = 12, scale = 2)
    private BigDecimal taxaFixa;

    @Column(name="frete_pago_vendedor")
    private boolean fretePagoVendedor;

    @Column(precision = 12, scale = 2)
    private BigDecimal frete;

    @Column(name="taxa_shopee_total", precision=12, scale=2)
    private BigDecimal taxaShopeeTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
