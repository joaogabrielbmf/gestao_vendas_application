package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.ConfiguracaoVendaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracaoVendaService {
    private final ConfiguracaoVendaRepository repository;
    private final TextoService textoService;
    private final UsuarioAtualService usuarioAtualService;

    public ConfiguracaoVendaService(ConfiguracaoVendaRepository repository,
                                    TextoService textoService,
                                    UsuarioAtualService usuarioAtualService) {
        this.repository = repository;
        this.textoService = textoService;
        this.usuarioAtualService = usuarioAtualService;
    }

    public ConfiguracaoVenda cadastrar(ConfiguracaoVenda c) {
        validarENormalizar(c);
        c.setCodigoConfiguracao(null);
        c.setUsuario(usuarioAtualService.get());
        return repository.save(c);
    }

    public List<ConfiguracaoVenda> listar() {
        return repository.findByUsuarioOrderByNomeAsc(usuarioAtualService.get());
    }

    public Optional<ConfiguracaoVenda> buscar(int id) {
        return repository.findByCodigoConfiguracaoAndUsuario(id, usuarioAtualService.get());
    }

    public ConfiguracaoVenda editar(int id, ConfiguracaoVenda novo) {
        ConfiguracaoVenda atual = repository
                .findByCodigoConfiguracaoAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Configuração não encontrada."));

        validarENormalizar(novo);
        atual.setNome(novo.getNome());
        atual.setEmbalagem(novo.getEmbalagem());
        atual.setTaxaPercentual(novo.getTaxaPercentual());
        atual.setTaxaFixa(novo.getTaxaFixa());
        atual.setConfiguracaoShopee(novo.isConfiguracaoShopee());
        atual.setTipoVendedorShopee(novo.getTipoVendedorShopee());
        atual.setCpfAcima450Pedidos(novo.isCpfAcima450Pedidos());
        return repository.save(atual);
    }

    public void deletar(int id) {
        ConfiguracaoVenda atual = repository
                .findByCodigoConfiguracaoAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Configuração não encontrada."));
        repository.delete(atual);
    }

    private void validarENormalizar(ConfiguracaoVenda c) {
        if (c == null || c.getNome() == null || c.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da configuração é obrigatório.");
        }

        c.setNome(textoService.titulo(c.getNome().trim()));
        c.setEmbalagem(valorNaoNegativo(c.getEmbalagem(), "Embalagem"));

        if (!c.isConfiguracaoShopee()) {
            c.setTipoVendedorShopee(null);
            c.setCpfAcima450Pedidos(false);
            c.setTaxaPercentual(valorNaoNegativo(c.getTaxaPercentual(), "Taxa percentual"));
            c.setTaxaFixa(valorNaoNegativo(c.getTaxaFixa(), "Taxa fixa"));
            return;
        }

        if (c.getTipoVendedorShopee() == null) {
            throw new IllegalArgumentException("Tipo de vendedor Shopee é obrigatório.");
        }

        if (c.getTipoVendedorShopee() == TipoVendedorShopee.CNPJ) {
            c.setCpfAcima450Pedidos(false);
        }

        c.setTaxaPercentual(BigDecimal.ZERO);
        c.setTaxaFixa(BigDecimal.ZERO);
    }

    private BigDecimal valorNaoNegativo(BigDecimal valor, String campo) {
        BigDecimal normalizado = valor == null ? BigDecimal.ZERO : valor;
        if (normalizado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(campo + " não pode ser negativo(a).");
        }
        return normalizado;
    }
}
