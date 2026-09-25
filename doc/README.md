# Documentação

## Visão geral

O HydroGrow API é a camada de backend do sistema HydroGrow.

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


## Estrutura da documentação

- [README principal](../README.md) — instruções gerais de execução e configuração do projeto.
- [Arquitetura do banco](architecture/schema.db.sql) — modelo de dados em SQL.
- [Diagrama do banco](architecture/schema.dbml) — visão visual do schema.
- [Git Hooks](./git-hooks.md) Validações antes do commit e push

## Como executar

```bash
./mvnw quarkus:dev
```

A aplicação ficará disponível em modo de desenvolvimento normalmente em:

- http://localhost:8080

