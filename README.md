<h1 style="font-size: 2em; font-weight: bold; text-align: center;">HydroGrow API</h1>

## Visão geral

O HydroGrow-API é a camada de backend do sistema HydroGrow.

## Objetivo do projeto

Centralizar e padronizar as regras de negócio da plataforma HydroGrow, incluindo:

- autenticação e autorização por usuário e tenant;
- gestão de usuários, perfis, roles e permissões;
- controle de planos e acesso comercial;
- cadastro de produtos, categorias e estoque;
- integração com clientes, fornecedores e dados operacionais;
- suporte a múltiplas empresas/tenants com isolamento funcional.

## Stack tecnológica

- Java 25
- Quarkus 3.x
- Maven
- PostgreSQL


## README com detalhes do projeto

[Aqui](docs/README.md) eu detalharei o projeto, explicando as decisões de arquitetura, padrões de projeto, boas práticas e tecnologias utilizadas.

## Como executar

```bash
./mvnw quarkus:dev
```

A aplicação ficará disponível em modo de desenvolvimento normalmente em:

- http://localhost:8080