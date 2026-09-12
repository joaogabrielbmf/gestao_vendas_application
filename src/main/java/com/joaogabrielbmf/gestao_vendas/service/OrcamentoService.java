package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.dto.*;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class OrcamentoService {
    private final OrcamentoRepository orcamentoRepository;
    private final ItemOrcamentoRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final UsuarioAtualService usuarioAtualService;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            ItemOrcamentoRepository itemRepository,
                            ProdutoRepository produtoRepository,
                            ClienteRepository clienteRepository,
                            ProdutoService produtoService,
                            VendaService vendaService,
                            UsuarioAtualService usuarioAtualService) {
        this.orcamentoRepository = orcamentoRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoService = produtoService;
        this.vendaService = vendaService;
        this.usuarioAtualService = usuarioAtualService;
    }

    @Transactional
    public Orcamento cadastrar(OrcamentoRequest request) {
        Orcamento orcamento = new Orcamento();
        orcamento.setData(LocalDate.now());
        Usuario usuario = usuarioAtualService.get();
        orcamento.setUsuario(usuario);
        orcamento.setStatusOrcamento(StatusOrcamento.EM_ABERTO);

        if (request.getCodigoCliente() != null) {
            orcamento.setCliente(
                    clienteRepository.findByCodigoClienteAndUsuario(request.getCodigoCliente(), usuario)
                            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."))
            );
        }

        orcamento = orcamentoRepository.save(orcamento);

        if (request.getItens() != null) {
            for (ItemOrcamentoRequest itemReq : request.getItens()) {
                Produto produto = produtoRepository.findByCodigoProdutoAndUsuario(itemReq.getCodigoProduto(), usuario)
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));

                if (itemReq.getQuantidade() == null || itemReq.getQuantidade() <= 0) {
                    throw new IllegalArgumentException("Quantidade inválida.");
                }

                ItemOrcamento item = new ItemOrcamento();
                item.setOrcamento(orcamento);
                item.setProduto(produto);
                item.setQuantidade(itemReq.getQuantidade());

                // O preço fica congelado no orçamento.
                item.setValorUnitario(
                        produtoService.buscarPrecoAtual(produto.getCodigoProduto())
                );

                itemRepository.save(item);
            }
        }

        return orcamento;
    }

    public List<Orcamento> listar() {
        return orcamentoRepository.findByUsuario(usuarioAtualService.get());
    }

    public Optional<Orcamento> buscar(int id) {
        return orcamentoRepository.findByCodigoOrcamentoAndUsuario(id, usuarioAtualService.get());
    }

    public List<ItemOrcamento> itens(int id) {
        Orcamento orcamento = orcamentoRepository.findByCodigoOrcamentoAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado."));
        return itemRepository.findByOrcamento(orcamento);
    }

    public BigDecimal total(int id) {
        return itens(id).stream()
                .map(item -> item.getValorUnitario()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Venda converterEmVenda(int id, ConverterOrcamentoRequest request) {
        Orcamento orcamento = orcamentoRepository.findByCodigoOrcamentoAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado."));

        StatusOrcamento status = orcamento.getStatusOrcamento();
        if (status == StatusOrcamento.CONVERTIDO || orcamento.getVendaConvertida() != null) {
            throw new IllegalStateException("Orçamento já convertido em venda.");
        }

        List<ItemOrcamento> itens = itemRepository.findByOrcamento(orcamento);
        if (itens.isEmpty()) {
            throw new IllegalStateException("Orçamento sem itens.");
        }

        Venda venda = vendaService.criarAPartirDoOrcamento(orcamento, itens, request);
        orcamento.setStatusOrcamento(StatusOrcamento.CONVERTIDO);
        orcamento.setVendaConvertida(venda);
        orcamentoRepository.save(orcamento);

        return venda;
    }

    @Transactional
    public void deletar(int id) {
        Orcamento orcamento = orcamentoRepository.findByCodigoOrcamentoAndUsuario(id, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Orçamento não encontrado."));

        if (orcamento.getStatusOrcamento() == StatusOrcamento.CONVERTIDO
                || orcamento.getVendaConvertida() != null) {
            throw new IllegalStateException("Orçamento convertido não pode ser excluído.");
        }

        itemRepository.deleteAll(itemRepository.findByOrcamento(orcamento));
        orcamentoRepository.delete(orcamento);
    }
}
