# Documentação

Esse projeto é um laboratório, onde são testadas novas tecnologias e padrões de arquitetura para o desenvolvimento de sistemas distribuídos. 

Estou utilizando o projeto para aprender, exercitar e aplicar conceitos de arquitetura de software, design patterns, boas práticas de desenvolvimento, integração contínua, entrega contínua e DevOps.

Não estarei utilizando IA para gerar códigos(no máximo o autocomplete), mas sim para me auxiliar na documentação, organização e revisão do projeto. A ideia é codar como os antigos faziam, escrevendo código de qualidade, testável e sustentável. Surgindo dúvas, irei pesquisar, estudar e aplicar o que for necessário para resolver os problemas e desafios que surgirem. 

Podem achar algumas configurações exageradas, mas como não estarei utilizando de IA para gerar códigos, preciso de ferramentas que me ajudem a manter a qualidade do código, como o Checkstyle, Spotless, Error Prone e SpotBugs. Aqui será uma metamorfose de ideias, conceitos e códigos.

A Hydrogrow, apesar de ser um projeto de laboratório, é um projeto real, que está sendo utilizado para gerenciar a produção de uma fazenda hidropônica.


## Estrutura da documentação das ferramentas utilizadas

- [README principal](../README.md) — instruções gerais de execução e configuração do projeto.
- [Arquitetura do banco](schema-db/schema.db.sql) — modelo de dados em SQL.
- [Diagrama do banco](schema-db/schema.dbml) — visão visual do schema.
- [Git Hooks](./git-hooks.md) — Validações antes do commit e push

## Plugins utilizados para manter a qualidade do código:
Caso queira saber mais sobre cada ferramenta, clique nos links abaixo para acessar a documentação detalhada de cada uma delas:
- [Error Prone](./plugins/error-prone.md) — Error Prone é uma ferramenta de análise estática para Java que detecta erros comuns de programação em tempo de compilação.
- [JaCoCo](./plugins/JaCoCo.md) — JaCoCo é uma ferramenta de cobertura de código para Java.
- [Checkstyle](./plugins/checkstyle.md) — Checkstyle é uma ferramenta de análise estática para Java que verifica se o código segue as regras de estilo definidas.
- [Spotless](./plugins/Spotless.md) — Spotless é uma ferramenta de formatação de código que garante um padrão de estilo consistente em todo o projeto.
- [SpotBugs](./plugins/SpotBugs.md) — SpotBugs é uma ferramenta de análise estática para Java que detecta bugs e problemas de qualidade no código.



