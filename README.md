# 📦 Sistema de Gestão de Vendas

Sistema web para gerenciamento de vendas, estoque e resultados financeiros de pequenos vendedores, com foco em operações de figurinhas, álbuns e vendas realizadas através de marketplaces como a Shopee.

## 🌐 Acesse o projeto

A aplicação está publicada e pode ser acessada em:

**https://gestao-vendas-application.onrender.com**

> O projeto está hospedado no plano gratuito do Render. Por isso, após um período sem acessos, o primeiro carregamento pode levar alguns instantes enquanto o servidor é iniciado.

## 💡 Sobre o projeto

O sistema centraliza o controle da operação de um pequeno vendedor, permitindo acompanhar desde a entrada de produtos até a realização das vendas e a análise dos resultados.

Cada usuário possui sua própria conta e seus dados são mantidos separados dos demais usuários.

## ⚙️ Principais funcionalidades

- Cadastro e gerenciamento de produtos
- Controle de estoque
- Cadastro de clientes
- Registro de vendas
- Vendas em aberto, finalizadas e canceladas
- Criação de orçamentos e conversão em vendas
- Registro de compras de produtos
- Cadastro de álbuns e categorias
- Configuração de preços por categoria e álbum
- Configuração de taxas de venda
- Regras específicas para vendas pela Shopee
- Dashboard com indicadores financeiros
- Ranking de produtos mais vendidos
- Controle de produtos com estoque baixo
- Catálogo de figurinhas da Copa do Mundo 2026
- Importação de figurinhas por seleção e número
- Filtros e paginação

## 📊 Dashboard

O Dashboard permite acompanhar informações importantes da operação, como:

- Faturamento
- Compras
- Resultado financeiro
- Vendas por status
- Produtos mais vendidos
- Produtos com estoque baixo

Os resultados também podem ser filtrados por período.

## ⚽ Catálogo de figurinhas

O sistema possui um catálogo da Copa do Mundo 2026 que facilita o cadastro e gerenciamento das figurinhas.

As figurinhas podem ser identificadas por códigos como:

```text
BRA17
ARG5
GER12
FWC1
CC1
```

Também é possível inserir listas de figurinhas e trabalhar com múltiplas unidades do mesmo item.

## 🛠️ Tecnologias utilizadas

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- Maven

### Frontend

- HTML
- CSS
- JavaScript
- Fetch API

### Banco de dados e infraestrutura

- PostgreSQL
- Supabase
- Docker
- Render

## 🏗️ Arquitetura

```text
HTML / CSS / JavaScript
          ↓
      Spring Boot
          ↓
   Services / Repositories
          ↓
   JPA / Hibernate
          ↓
PostgreSQL / Supabase
```

O frontend se comunica com o backend através de requisições HTTP utilizando a Fetch API.

O backend é responsável pelas regras de negócio, autenticação, acesso aos dados e comunicação com o banco PostgreSQL.

## 🔐 Autenticação

A aplicação utiliza Spring Security para autenticação.

Os produtos, clientes, vendas, orçamentos, compras e demais informações comerciais são associados ao usuário autenticado, mantendo os dados de diferentes usuários separados.

## 💰 Vendas e orçamentos

As vendas registram os produtos, quantidades e valores utilizados em cada operação.

Uma venda pode possuir os seguintes status:

```text
EM_ABERTO
FINALIZADA
CANCELADA
```

O sistema também considera informações como estoque, taxas, frete e embalagem.

Os orçamentos permitem preparar uma operação antes da venda e podem ser convertidos posteriormente em vendas, preservando os itens e valores registrados.

## 📦 Produtos e estoque

Os produtos podem ser organizados por álbum, categoria e tipo.

O sistema permite acompanhar e alterar o estoque disponível, além de identificar produtos com estoque baixo.

Para figurinhas, o cadastro pode utilizar informações como álbum, ano, seleção e número.

## 🛒 Compras

O sistema permite registrar compras de produtos realizadas pelo vendedor.

Essas compras representam o investimento feito na aquisição de mercadorias e são consideradas nos indicadores financeiros apresentados no Dashboard.

## 🏷️ Preços e configurações de venda

O sistema permite configurar preços de acordo com categorias e álbuns, além de trabalhar com preços específicos para determinados produtos.

As configurações de venda permitem representar diferentes formas de comercialização, incluindo vendas diretas e operações realizadas pela Shopee.

Taxas percentuais, taxas fixas, embalagem e outras informações da operação podem ser consideradas nos cálculos da venda.

## 🔎 Filtros e paginação

As principais áreas do sistema possuem filtros e paginação para facilitar a utilização mesmo com uma quantidade maior de produtos, vendas e outros registros.

## ☁️ Deploy

A aplicação é executada em um container Docker hospedado no Render.

O banco PostgreSQL está hospedado no Supabase.

```text
Usuário
   ↓
Render
   ↓
Spring Boot
   ↓
Supabase
   ↓
PostgreSQL
```

O código-fonte é versionado com Git e armazenado no GitHub.

## 🔐 Variáveis de ambiente

As credenciais de acesso ao banco de dados são configuradas através de variáveis de ambiente e não ficam armazenadas diretamente no código-fonte.

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=
```

Durante o desenvolvimento local, essas informações podem ser definidas em um arquivo `.env`.

O arquivo `.env` não deve ser enviado ao repositório.

## 💻 Executando localmente

### Requisitos

- Java 21
- Maven
- PostgreSQL ou acesso a uma instância PostgreSQL

Configure as variáveis de ambiente necessárias e execute a aplicação pela IDE ou através do Maven.

Por padrão, a aplicação estará disponível em:

```text
http://localhost:8080
```

## 👨‍💻 Autor

**João Gabriel**

Projeto desenvolvido como aplicação prática de desenvolvimento web utilizando Java, Spring Boot, PostgreSQL, HTML, CSS e JavaScript.