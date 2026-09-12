# Gestão de Vendas — Figurinhas, Álbuns e Cards

Projeto Spring Boot + PostgreSQL com frontend em HTML/CSS/JavaScript puro.

## Requisitos
- Java 21
- Maven
- PostgreSQL
- Banco `gestao_vendas`

## Configuração
Edite `src/main/resources/application.properties` e coloque seu usuário/senha do PostgreSQL.

## Rodar
```bash
mvn spring-boot:run
```

Acesse:
- Frontend: http://localhost:8080/
- API: http://localhost:8080/produtos, /albuns, /categorias, /clientes, /vendas etc.

## Regras implementadas
- Produto: figurinha, álbum completo e álbum incompleto.
- Copa do Mundo 2022+: figurinha é identificada por álbum + seleção + número.
- Demais álbuns: figurinha é identificada por álbum + número.
- Preço de figurinha: usa `precoEspecifico` quando existe; senão usa preço por álbum + categoria.
- Venda mantém snapshot de `valorUnitario` e `custoUnitario`.
- Venda finalizada baixa estoque.
- Cancelamento de venda finalizada repõe estoque.
- Venda finalizada/cancelada não pode ser excluída.
- Configuração de venda copia taxas/embalagem para a venda.
- Resumo de venda calcula faturamento, custos, taxas e lucro.

## Observação
Este é um MVP funcional para evolução. Para produção, recomenda-se adicionar autenticação, migrations (Flyway/Liquibase), testes automatizados e DTOs de resposta.


## Frontend com nomes
Os relacionamentos são escolhidos em selects por nomes legíveis. Os IDs permanecem apenas como valores internos enviados à API. Isso vale para produto/álbum/categoria, preço por categoria, cliente, configuração, orçamento e venda.


## Correção: total do orçamento
- A listagem de orçamentos agora mostra o valor total.
- A tela de detalhes mostra valor unitário, subtotal por item e total geral.
- Novo endpoint: `GET /orcamentos/{id}/total`.


## Alterações desta versão
- Login/cadastro com Spring Security e BCrypt.
- Preferência de limite de estoque baixo por usuário.
- Menu lateral retrátil; Dashboard, Orçamentos e Vendas ficam na barra superior.
- Número da figurinha separado do nome na tabela de produtos e ordenação numérica.
- Texto simplificado da configuração Shopee e alinhamento dos selects.
- Estrutura de propriedade `Usuario` adicionada às entidades para evolução do isolamento por vendedor.
- Utilitário de normalização textual incluído para padronização de nomes.

### Banco existente
Como esta versão adiciona autenticação e a coluna `codigo_usuario` a várias tabelas, teste primeiro em uma cópia do banco. `ddl-auto=update` criará as novas estruturas, mas registros antigos não terão proprietário automaticamente.

## Ajustes desta versão
- Detalhe da venda agora separa corretamente `taxaShopee` de `taxaPercentual`; a soma das taxas Shopee exibida no resumo corresponde às taxas unitárias dos itens.
- A tela Vendas foi separada em **Vendas em aberto** e **Histórico de vendas**.
- Histórico com filtros por período, status, cliente, canal/configuração e tipo de produto.
- Histórico exibe faturamento, despesas e lucro e permite abrir os detalhes da venda sem editar registros finalizados/cancelados.
- Corrigida a ordenação numérica da coluna Número em Produtos.

## Atualizações desta versão
- Produtos continuam paginados no backend com `Pageable`, 20 por página.
- Histórico de vendas também usa `Pageable`, com até 20 vendas por página.
- Filtros ficam recolhidos e abrem pelo botão **Filtrar**; filtros categóricos são pesquisáveis e o dropdown exibe aproximadamente 5 opções por vez com scroll.
- Orçamentos agora ficam dentro da área **Vendas** e podem ser convertidos em venda.
- Itens adicionados manualmente ou por lista ficam em tabela compacta com scroll interno.
- Para figurinhas: seleção só é habilitada em Copa do Mundo 2022 ou posterior e usa exatamente 3 letras maiúsculas (ex.: `BRA`).
- Para álbum completo/incompleto: categoria, seleção e número ficam desabilitados.
- Detalhes de venda agrupam figurinhas por álbum/ano; Copa 2022+ usa formato como `BRA17`.
- Configuração CNPJ desabilita a opção de CPF acima de 450 pedidos/90 dias.
- Taxas Shopee são calculadas com base no valor total da venda, uma única vez.
- Custos médio/específico deixaram de ser usados no fluxo; investimentos são registrados em **Compras de produtos**.
- Usuários podem criar tipos de produto personalizados (ex.: Sleeve, Toploader), com normalização e bloqueio de duplicidades.
- A câmera não foi implementada nesta versão, conforme combinado.

### Compatibilidade
Os campos antigos de custo permanecem nas entidades/tabelas apenas para não quebrar bancos já existentes, mas não são usados no novo cálculo financeiro.

## Catálogo pré-carregado de teste
Esta versão inclui o catálogo **Copa do Mundo 2026** baseado no controle de figurinhas fornecido pelo usuário. O catálogo contém 993 itens: FWC1–FWC19, 48 seleções com 20 figurinhas cada e CC1–CC14.

Fluxo:
1. Abra **Catálogos** no menu lateral.
2. Clique em **Adicionar ao meu estoque**. As figurinhas são criadas com estoque 0 e categoria **Comum**.
3. Clique em **Gerenciar categorias** para abrir o grid clicável.
4. Selecione várias figurinhas, escolha a categoria e aplique em massa.

O grid permite filtrar por grupo/seleção, categoria atual e código da figurinha, além de selecionar todos os itens filtrados de uma vez.

## Atualização de testes - 09/09/2026
- Dashboard: paginação visual independente (volume/mais vendidos: 5; estoque baixo: 10).
- Produtos: seleção ordena também pelo número; estoque editável inline com +/-; tipo de preço e preço separados.
- Edição de produto e álbum centralizada; álbum aceita números no nome e mostra Salvar durante edição.
- Vendas: aba independente Vendas em aberto.
- Itens de venda/orçamento e interpretação de listas: 10 por página; preview sem coluna Entrada.
- Corrigido vínculo de produtos manuais ao usuário atual (incluindo migração dos produtos legados sem usuário), o que fazia álbum completo não aparecer.


### Atualização de testes - filtro de período do dashboard
- Dashboard agora aceita filtro opcional por data inicial e final.
- Faturamento, lucro, compras, taxas, ticket, volume, mais vendidos e contadores de vendas respeitam o período.
- Estoque baixo continua mostrando o estoque atual, pois não é uma métrica histórica.
- Alterar estoque na tabela de produtos não recarrega nem reordena automaticamente a listagem; a nova ordenação só aparece ao ordenar, filtrar, trocar de página ou recarregar.
