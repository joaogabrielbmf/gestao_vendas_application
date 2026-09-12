package com.joaogabrielbmf.gestao_vendas.service;
import com.joaogabrielbmf.gestao_vendas.model.Usuario;
import com.joaogabrielbmf.gestao_vendas.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service public class UsuarioAtualService{private final UsuarioRepository repo; public UsuarioAtualService(UsuarioRepository r){repo=r;} public Usuario get(){String email=SecurityContextHolder.getContext().getAuthentication().getName();return repo.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalStateException("Usuário autenticado não encontrado."));} public boolean pertence(Usuario u){return u!=null&&u.getCodigoUsuario().equals(get().getCodigoUsuario());}}
