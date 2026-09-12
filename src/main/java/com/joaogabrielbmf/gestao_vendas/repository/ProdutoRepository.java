package com.joaogabrielbmf.gestao_vendas.repository;
import com.joaogabrielbmf.gestao_vendas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.*;
public interface ProdutoRepository extends JpaRepository<Produto, Integer>, JpaSpecificationExecutor<Produto> {
    boolean existsByAlbumAndNumeroFigurinha(Album album, Integer numeroFigurinha);
    boolean existsByAlbumAndSelecaoFigurinhaAndNumeroFigurinha(Album album, String selecaoFigurinha, Integer numeroFigurinha);
    boolean existsByAlbumAndNumeroFigurinhaAndCodigoProdutoNot(Album album, Integer numeroFigurinha, Integer codigoProduto);
    boolean existsByAlbumAndSelecaoFigurinhaAndNumeroFigurinhaAndCodigoProdutoNot(Album album, String selecaoFigurinha, Integer numeroFigurinha, Integer codigoProduto);
    boolean existsByTipoProdutoCustomizado(TipoProdutoCustomizado tipoProdutoCustomizado);
    List<Produto> findByUsuario(Usuario usuario);
    Optional<Produto> findByCodigoProdutoAndUsuario(Integer codigoProduto, Usuario usuario);
    List<Produto> findByUsuarioIsNull();
    List<Produto> findByUsuarioAndAlbum(Usuario usuario, Album album);
    long countByUsuarioAndAlbum(Usuario usuario, Album album);
}
