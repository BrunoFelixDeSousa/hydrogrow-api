# Checkstyle

Como o código deve ser escrito: verifica convenções e construções Java.

## Instalação neste projeto

add o plugin ao `pom.xml`:

```xml
<properties>
    <checkstyle-plugin.version>3.6.0</checkstyle-plugin.version>
    <checkstyle.version>14.3.0</checkstyle.version>
</properties>


<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>${checkstyle-plugin.version}</version>
    <dependencies>
        <dependency>
            <groupId>com.puppycrawl.tools</groupId>
            <artifactId>checkstyle</artifactId>
            <version>${checkstyle.version}</version>
        </dependency>
    </dependencies>
    <configuration>
        <configLocation>config/quality/checkstyle.xml</configLocation>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
        <includeResources>false</includeResources>
        <includeTestResources>false</includeTestResources>
        <consoleOutput>true</consoleOutput>
        <failOnViolation>true</failOnViolation>
    </configuration>
    <executions>
        <execution>
            <id>check-conventions</id>
            <phase>validate</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Execute o comando abaixo na raiz. Código de saída diferente de zero indica falha.

```shell
// Executa o Checkstyle, mostrando todos os avisos e erros.
./mvnw -B checkstyle:check

// Executa o Checkstyle de forma silenciosa, mostrando apenas erros.
./mvnw -B -q checkstyle:check
```

## Configuração e utilização

Executa em `validate`, analisando produção e testes. As regras estão em
[checkstyle.xml](../../config/quality/checkstyle.xml): nomes de tipos, métodos, constantes, variáveis
e parâmetros; uma instrução por linha; instruções vazias; equals/hashCode e fall-through.
Imports wildcard existentes são permitidos. Formatação permanece com Spotless.
O motor Checkstyle é fixado separadamente do plugin para suportar o Java do projeto.
Resultado: console e `target/checkstyle-result.xml`. Corrija o arquivo/linha indicado e repita.
Checkstyle não aplica correções automaticamente.

## Gate completo

`./mvnw -B clean verify` executa os gates locais, testes, cobertura e Javadoc.
Os testes Quarkus exigem Docker disponível para PostgreSQL Dev Services.

## Referência

[Documentação oficial](https://maven.apache.org/plugins/maven-checkstyle-plugin/).
