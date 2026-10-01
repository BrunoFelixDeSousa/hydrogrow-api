# JaCoCo

O JaCoCo (Java Code Coverage) é uma ferramenta utilizada para medir a cobertura de testes em aplicações Java.

Ele identifica quais partes do código foram executadas durante os testes e gera relatórios que auxiliam na avaliação da qualidade dos testes automatizados.

O JaCoCo pode gerar relatórios em diferentes formatos, sendo o **XML** o utilizado pelo SonarQube para importar a cobertura de código.

---

## Como funciona

O JaCoCo utiliza um **Java Agent** para instrumentar as classes durante a execução dos testes.

O fluxo é o seguinte:

1. O Maven inicia a fase de testes.
2. O JaCoCo instrumenta as classes da aplicação.
3. Os testes são executados.
4. O JaCoCo registra quais instruções foram executadas.
5. É gerado o relatório de cobertura.
6. O SonarQube utiliza o relatório XML para calcular a cobertura do projeto.

```mermaid
flowchart LR
    A[Maven Test / Verify]
    B[JaCoCo Agent]
    C[Testes]
    D[Coleta de Cobertura]
    E[jacoco.xml]
    F[SonarQube]

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
```

---

## Principais arquivos gerados

Após a execução dos testes, o JaCoCo gera alguns arquivos importantes.

| Arquivo | Descrição |
|----------|-----------|
| `target/jacoco.exec` | Dados brutos da cobertura coletada durante os testes. |
| `target/site/jacoco/index.html` | Relatório HTML navegável. |
| `target/site/jacoco/jacoco.xml` | Relatório XML utilizado pelo SonarQube. |
| `target/site/jacoco/jacoco.csv` | Relatório CSV contendo métricas da cobertura. |

---

## Comandos Maven

```bash
# Executa o ciclo completo do Maven.
# Compila, executa os testes, gera o relatório do JaCoCo
# e executa as validações configuradas.
mvn clean verify

# Gera apenas o relatório HTML/XML.
mvn jacoco:report

# Executa apenas a validação das regras de cobertura.
mvn jacoco:check

# Consolida a cobertura de projetos multimódulo.
mvn jacoco:report-aggregate

# Mescla múltiplos arquivos .exec em um único relatório.
mvn jacoco:merge
```

---

## Configuração do plugin

Adicione o plugin ao `pom.xml`.

```xml
<!-- Plugin responsável por instrumentar o código,
     coletar métricas de cobertura e gerar relatórios -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.15</version>

    <executions>

        <!--
            Adiciona o Java Agent do JaCoCo durante a execução dos testes.

            Sem esta etapa nenhuma cobertura será coletada.
        -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>

        <!--
            Gera os relatórios de cobertura.

            Arquivos gerados:

            target/site/jacoco/index.html
            target/site/jacoco/jacoco.xml
            target/site/jacoco/jacoco.csv
        -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>

            <configuration>

                <!--
                    Classes excluídas do cálculo da cobertura.

                    Normalmente não possuem regra de negócio.
                -->
                <excludes>
                    <exclude>**/dto/**</exclude>
                    <exclude>**/config/**</exclude>
                    <exclude>**/*Mapper.*</exclude>
                    <exclude>**/*Config.*</exclude>
                    <exclude>**/generated/**</exclude>
                </excludes>

            </configuration>
        </execution>

        <!--
            Valida se a cobertura mínima foi atingida.

            Caso alguma regra não seja satisfeita,
            o build será interrompido.
        -->
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>

                    <!--
                        Aplica a validação ao projeto inteiro.

                        Também é possível validar por:

                        - PACKAGE
                        - CLASS
                        - METHOD
                    -->
                    <rule>
                        <element>BUNDLE</element>
                        <limits>

                            <!--
                                Exige no mínimo 80% das linhas cobertas.

                                Outros contadores disponíveis:

                                - INSTRUCTION
                                - BRANCH
                                - METHOD
                                - CLASS
                                - COMPLEXITY

                                Outros tipos de validação:

                                - COVEREDRATIO
                                - MISSEDRATIO
                                - COVEREDCOUNT
                                - MISSEDCOUNT
                                - TOTALCOUNT
                            -->
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

## Executando

Execute:

```bash
mvn clean verify
```

Após a execução deverão existir os arquivos:

```text
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
target/site/jacoco/jacoco.csv
```

Abra no navegador:

```text
target/site/jacoco/index.html
```

Será exibido um relatório semelhante a:

- Cobertura por pacote
- Cobertura por classe
- Cobertura por método
- Cobertura por linha
- Complexidade ciclomática

---

## Métricas de cobertura

O JaCoCo disponibiliza diversas métricas para avaliar a qualidade dos testes.

| Métrica | Descrição |
|----------|-----------|
| Instructions | Percentual de instruções executadas. |
| Branches | Cobertura de decisões (`if`, `switch`, etc.). |
| Lines | Cobertura de linhas de código. |
| Methods | Métodos executados pelos testes. |
| Classes | Classes exercitadas pelos testes. |
| Complexity | Complexidade ciclomática coberta pelos testes. |

---

## Problemas comuns

### Coverage = 0%

Verifique se existe:

```text
target/site/jacoco/jacoco.xml
```

Caso não exista, confirme se o plugin está configurado corretamente e execute:

```bash
mvn clean verify
```

---

### Relatório HTML não foi gerado

Verifique se existe a execução:

```xml
<goal>report</goal>
```

---

### Build falhando por cobertura

O plugin possui uma regra semelhante a:

```xml
<minimum>0.80</minimum>
```

Caso a cobertura seja inferior a **80%**, o Maven interromperá o build.

---

### SonarQube mostra cobertura 0%

Verifique se o SonarQube está apontando para:

```properties
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

---

## Referências

- https://www.jacoco.org/jacoco/
- https://www.jacoco.org/jacoco/trunk/doc/maven.html
- https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/test-coverage/java-test-coverage/