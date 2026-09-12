package com.joaogabrielbmf.gestao_vendas.service;

import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final TextoService textoService;
    private final UsuarioAtualService usuarioAtualService;

    public ClienteService(ClienteRepository repository, TextoService textoService, UsuarioAtualService usuarioAtualService) {
        this.repository = repository;
        this.textoService = textoService;
        this.usuarioAtualService = usuarioAtualService;
    }

    public Cliente cadastrar(Cliente c) {
        normalizar(c);
        c.setCodigoCliente(null);
        c.setUsuario(usuarioAtualService.get());
        return repository.save(c);
    }

    public List<Cliente> listar() { return repository.findByUsuarioOrderByNomeClienteAsc(usuarioAtualService.get()); }
    public Optional<Cliente> buscar(int id) { return repository.findByCodigoClienteAndUsuario(id, usuarioAtualService.get()); }

    public Cliente editar(int id, Cliente novo) {
        Cliente atual = repository.findByCodigoClienteAndUsuario(id, usuarioAtualService.get()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        atual.setNomeCliente(textoService.titulo(novo.getNomeCliente()));
        atual.setEmailCliente(textoService.email(novo.getEmailCliente()));
        atual.setTelefoneCliente(novo.getTelefoneCliente() == null ? null : novo.getTelefoneCliente().trim());
        return repository.save(atual);
    }

    public void deletar(int id) {
        Cliente atual = repository.findByCodigoClienteAndUsuario(id, usuarioAtualService.get()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        repository.delete(atual);
    }

    private void normalizar(Cliente c) {
        c.setNomeCliente(textoService.titulo(c.getNomeCliente()));
        c.setEmailCliente(textoService.email(c.getEmailCliente()));
        if (c.getTelefoneCliente() != null) c.setTelefoneCliente(c.getTelefoneCliente().trim());
    }
}
