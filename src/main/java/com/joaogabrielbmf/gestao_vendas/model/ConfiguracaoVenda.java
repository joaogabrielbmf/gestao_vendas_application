package com.joaogabrielbmf.gestao_vendas.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @Entity
public class ConfiguracaoVenda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_configuracao")
    private Integer codigoConfiguracao;

    @Column(nullable = false)
    private String nome;

    @Column(precision = 12, scale = 2)
    private BigDecimal embalagem;

    @Column(name = "taxa_percentual", precision = 8, scale = 4)
    private BigDecimal taxaPercentual;

    @Column(name = "taxa_fixa", precision = 12, scale = 2)
    private BigDecimal taxaFixa;

    @Column(name="configuracao_shopee")
    private boolean configuracaoShopee;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_vendedor_shopee")
    private TipoVendedorShopee tipoVendedorShopee;

    @Column(name="cpf_acima_450_pedidos")
    private boolean cpfAcima450Pedidos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Usuario usuario;
}
