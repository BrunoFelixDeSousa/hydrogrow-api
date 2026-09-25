# Git Hooks

O projeto utiliza Git Hooks para executar validações locais antes de commits e pushes, reduzindo erros e antecipando feedbacks que, de outra forma, ocorreriam apenas no pipeline de CI.

## Configuração

Há duas formas de configurar o Git para utilizar os hooks versionados no diretório `.githooks`.

### Opção 1 — Script de configuração

Após clonar o projeto, execute:

```bash
./scripts/setup-git-hooks.sh
```

O script realiza a configuração necessária para habilitar os hooks do projeto.

### Opção 2 — Configuração manual

Configure o Git para utilizar o diretório `.githooks`:

```bash
git config core.hooksPath .githooks
```

Em Linux/macOS, garanta também que os hooks possuem permissão de execução:

```bash
chmod +x .githooks/pre-commit
chmod +x .githooks/pre-push
```

## Pre-commit

Atualmente, o hook `pre-commit` executa verificações rápidas antes da criação de um commit.

```text
git commit
    ↓
pre-commit
    ↓
testes unitários
    ↓
commit
```

A validação executada é:

```bash
./mvnw -q test
```

Caso os testes falhem, o commit será cancelado.

## Pre-push

O hook `pre-push` executa verificações mais completas antes de enviar alterações para o repositório remoto.

```text
git push
    ↓
pre-push
    ↓
build
testes
validações
    ↓
push
```

A validação executada é:

```bash
./mvnw -q verify
```

Caso alguma validação falhe, o push será cancelado.

## Importante

Os Git Hooks fornecem uma camada de validação local e **não substituem o pipeline de CI**.

Os hooks podem ser ignorados explicitamente utilizando:

```bash
git commit --no-verify
```

ou:

```bash
git push --no-verify
```

Por esse motivo, todas as validações críticas também devem ser executadas no pipeline de CI.

## Estrutura

```text
.githooks/
├── pre-commit
└── pre-push
```

A estratégia adotada é:

```text
pre-commit → validações rápidas
pre-push   → validações mais completas
CI         → validação definitiva
```