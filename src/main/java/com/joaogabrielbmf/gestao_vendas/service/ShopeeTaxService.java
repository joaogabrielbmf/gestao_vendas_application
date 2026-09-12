package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.RegraShopeeRegressivaRepository;
import org.springframework.stereotype.Service;
import java.math.*;

@Service
public class ShopeeTaxService {
    private final RegraShopeeRegressivaRepository regraRepository;
    private final UsuarioAtualService usuarioAtualService;

    public ShopeeTaxService(RegraShopeeRegressivaRepository regraRepository, UsuarioAtualService usuarioAtualService) {
        this.regraRepository = regraRepository;
        this.usuarioAtualService = usuarioAtualService;
    }

    public BigDecimal calcularTaxaTotal(BigDecimal valorTotalVenda, ConfiguracaoVenda config) {
        if (valorTotalVenda == null || valorTotalVenda.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = valorTotalVenda.setScale(2, RoundingMode.HALF_UP);
        BigDecimal oito = new BigDecimal("8.00");
        BigDecimal doze = new BigDecimal("12.00");

        if (total.compareTo(oito) < 0) {
            return total.multiply(new BigDecimal("0.50"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        boolean cpfAlto = config.getTipoVendedorShopee() == TipoVendedorShopee.CPF
                && config.isCpfAcima450Pedidos();

        if (cpfAlto && total.compareTo(oito) >= 0 && total.compareTo(doze) < 0) {
            return regraRepository.findByUsuarioAndValorItem(usuarioAtualService.get(), total)
                    .orElseThrow(() -> new IllegalStateException(
                            "Cadastre a taxa regressiva exata da Shopee para venda de " + total + "."))
                    .getTaxaTotal();
        }

        if (total.compareTo(new BigDecimal("80.00")) < 0) {
            return total.multiply(new BigDecimal("0.20"))
                    .add(new BigDecimal("4.00"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (total.compareTo(new BigDecimal("100.00")) < 0) {
            return total.multiply(new BigDecimal("0.14"))
                    .add(new BigDecimal("16.00"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (total.compareTo(new BigDecimal("200.00")) < 0) {
            return total.multiply(new BigDecimal("0.14"))
                    .add(new BigDecimal("20.00"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return total.multiply(new BigDecimal("0.14"))
                .add(new BigDecimal("26.00"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
