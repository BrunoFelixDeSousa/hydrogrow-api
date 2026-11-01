# Decisões de arquitetura

## Modelo de domínio

O modelo de domínio não replica o modelo relacional. Tabelas, chaves substitutas e campos de
auditoria podem existir apenas no adapter de persistência quando não participam de regras de
negócio.

### Identidade

- Aggregate roots usam `UUID` diretamente.
- IDs são criados pela aplicação por meio de `IdGenerator`.
- A implementação padrão usa UUID v7 com `java-uuid-generator`.
- Value objects específicos de ID só devem ser criados quando o identificador possuir formato,
  validação ou comportamento de negócio próprio.
- `Profile` é identificado pelo usuário ao qual pertence.
- `Membership` é identificada no domínio pelo par `tenantId` e `userId`; o `id` substituto da
  tabela `tenant_memberships` é detalhe de persistência.

### Entidades e value objects

- `User` e `Tenant` são os aggregate roots atuais.
- Entidades usam classes e expõem operações de negócio, não setters genéricos.
- Records são reservados a value objects imutáveis.
- Um value object deve normalizar, validar, proteger ou oferecer comportamento relevante. Objetos
  usados apenas para agrupar parâmetros devem ser evitados.

### Criação e restauração

- `register` ou `create` aplica os estados padrão de uma instância nova.
- `restore` reconstitui o estado carregado pela persistência sem repetir o fluxo de criação.
- IDs, relógio e outras dependências externas são fornecidos pela camada de aplicação.
- Builders não são usados quando uma factory com poucos campos obrigatórios representa o caso de
  uso com clareza.

### Datas de auditoria

`created_at` e `updated_at` são dados de auditoria mantidos pela persistência e pelos triggers do
banco. Datas que afetam comportamento, como `last_login_at`, permanecem explícitas no domínio.

### Testes

- Testar comportamentos, transições e invariantes dos aggregates.
- Testar normalização e rejeições dos value objects.
- Testar o gerador UUID v7 uma vez.
- Não duplicar testes para wrappers de tipos da biblioteca padrão.
