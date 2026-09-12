package com.joaogabrielbmf.gestao_vendas.controller;

import com.joaogabrielbmf.gestao_vendas.model.Usuario;
import com.joaogabrielbmf.gestao_vendas.repository.UsuarioRepository;
import com.joaogabrielbmf.gestao_vendas.service.TextoService;
import com.joaogabrielbmf.gestao_vendas.service.UsuarioAtualService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioRepository repo;
    private final PasswordEncoder enc;
    private final UsuarioAtualService atual;
    private final TextoService textoService;

    public AuthController(UsuarioRepository repo, PasswordEncoder enc, UsuarioAtualService atual, TextoService textoService) {
        this.repo = repo;
        this.enc = enc;
        this.atual = atual;
        this.textoService = textoService;
    }

    @PostMapping("/cadastro")
    public Map<String,Object> cadastro(@RequestBody Map<String,String> b) {
        String email = textoService.email(b.getOrDefault("email", ""));
        String nome = textoService.titulo(b.getOrDefault("nome", ""));
        String senha = b.getOrDefault("senha", "");
        if (nome == null || nome.isBlank() || email == null || email.isBlank() || senha.length() < 6) {
            throw new IllegalArgumentException("Informe nome, e-mail e senha com pelo menos 6 caracteres.");
        }
        if (repo.existsByEmailIgnoreCase(email)) throw new IllegalStateException("E-mail já cadastrado.");
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(enc.encode(senha));
        u.setLimiteEstoqueBaixo(3);
        repo.save(u);
        return Map.of("message", "Usuário cadastrado");
    }

    @GetMapping("/me")
    public Map<String,Object> me() {
        var u = atual.get();
        return Map.of("codigoUsuario",u.getCodigoUsuario(),"nome",u.getNome(),"email",u.getEmail(),"limiteEstoqueBaixo",u.getLimiteEstoqueBaixo());
    }

    @PutMapping("/preferencias")
    public Map<String,Object> pref(@RequestBody Map<String,Integer> b) {
        var u = atual.get();
        int l = Math.max(0,b.getOrDefault("limiteEstoqueBaixo",3));
        u.setLimiteEstoqueBaixo(l);
        repo.save(u);
        return Map.of("limiteEstoqueBaixo",l);
    }
}
