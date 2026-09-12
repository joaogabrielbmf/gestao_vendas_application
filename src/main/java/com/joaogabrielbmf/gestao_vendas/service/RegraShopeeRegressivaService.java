package com.joaogabrielbmf.gestao_vendas.service;
import com.joaogabrielbmf.gestao_vendas.model.*;
import com.joaogabrielbmf.gestao_vendas.repository.RegraShopeeRegressivaRepository;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class RegraShopeeRegressivaService{
    private final RegraShopeeRegressivaRepository repo;
    private final UsuarioAtualService usuarioAtualService;
    public RegraShopeeRegressivaService(RegraShopeeRegressivaRepository repo, UsuarioAtualService usuarioAtualService){
        this.repo=repo; this.usuarioAtualService=usuarioAtualService;
    }
    public RegraShopeeRegressiva cadastrar(RegraShopeeRegressiva r){
        r.setCodigoRegraShopee(null);
        r.setUsuario(usuarioAtualService.get());
        return repo.save(r);
    }
    public List<RegraShopeeRegressiva> listar(){return repo.findByUsuarioOrderByValorItemAsc(usuarioAtualService.get());}
    public void deletar(int id){repo.delete(repo.findByCodigoRegraShopeeAndUsuario(id, usuarioAtualService.get()).orElseThrow(()->new IllegalArgumentException("Regra não encontrada.")));}
}
