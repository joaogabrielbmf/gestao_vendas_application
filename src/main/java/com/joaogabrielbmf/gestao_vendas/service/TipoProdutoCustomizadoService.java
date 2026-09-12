package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.*;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

@Service
public class TipoProdutoCustomizadoService {
    private static final Set<String> TIPOS_RESERVADOS =
            Set.of("FIGURINHA", "ALBUM COMPLETO", "ALBUM INCOMPLETO");

    private final TipoProdutoCustomizadoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioAtualService usuarioAtualService;
    private final TextoService textoService;

    public TipoProdutoCustomizadoService(TipoProdutoCustomizadoRepository repository,
                                         ProdutoRepository produtoRepository,
                                         UsuarioAtualService usuarioAtualService,
                                         TextoService textoService) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.usuarioAtualService = usuarioAtualService;
        this.textoService = textoService;
    }

    public TipoProdutoCustomizado cadastrar(TipoProdutoCustomizado novo) {
        Usuario usuario = usuarioAtualService.get();
        String nome = normalizarExibicao(novo.getNome());
        String chave = normalizarChave(novo.getNome());
        validarReservado(chave);

        if (repository.existsByUsuarioAndNomeNormalizado(usuario, chave)) {
            throw new IllegalStateException("Tipo de produto já cadastrado.");
        }

        TipoProdutoCustomizado tipo = new TipoProdutoCustomizado();
        tipo.setNome(nome);
        tipo.setNomeNormalizado(chave);
        tipo.setUsuario(usuario);
        return repository.save(tipo);
    }

    public List<TipoProdutoCustomizado> listar() {
        return repository.findByUsuarioOrderByNomeAsc(usuarioAtualService.get());
    }

    public TipoProdutoCustomizado editar(int codigo, TipoProdutoCustomizado novo) {
        Usuario usuario = usuarioAtualService.get();
        TipoProdutoCustomizado atual = repository.findByCodigoTipoProdutoAndUsuario(codigo, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de produto não encontrado."));

        String chave = normalizarChave(novo.getNome());
        validarReservado(chave);

        if (repository.existsByUsuarioAndNomeNormalizadoAndCodigoTipoProdutoNot(usuario, chave, codigo)) {
            throw new IllegalStateException("Tipo de produto já cadastrado.");
        }

        atual.setNome(normalizarExibicao(novo.getNome()));
        atual.setNomeNormalizado(chave);
        return repository.save(atual);
    }

    public void deletar(int codigo) {
        TipoProdutoCustomizado atual = repository.findByCodigoTipoProdutoAndUsuario(codigo, usuarioAtualService.get())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de produto não encontrado."));
        if (produtoRepository.existsByTipoProdutoCustomizado(atual)) {
            throw new IllegalStateException("Não é possível excluir um tipo que está sendo usado por produtos.");
        }
        repository.delete(atual);
    }

    private String normalizarExibicao(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Nome do tipo é obrigatório.");
        }
        return textoService.titulo(valor);
    }

    private String normalizarChave(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Nome do tipo é obrigatório.");
        }
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private void validarReservado(String chave) {
        if (TIPOS_RESERVADOS.contains(chave)) {
            throw new IllegalStateException("Este tipo já existe como tipo padrão do sistema.");
        }
    }
}
