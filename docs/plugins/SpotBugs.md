# SpotBugs

O programa compilado contém um padrão perigoso: analisa bytecode Java de produção.

## Instalação neste projeto

add o plugin ao `pom.xml`:

```xml
<properties>
    <spotbugs-plugin.version>4.10.4.1</spotbugs-plugin.version>
</properties>

<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>${spotbugs-plugin.version}</version>
    <configuration>
        <effort>Max</effort>
        <threshold>High</threshold>
        <xmlOutput>true</xmlOutput>
        <failOnError>true</failOnError>
    </configuration>
    <executions>
        <execution>
            <id>check-bytecode</id>
            <phase>verify</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Execute o comando abaixo na raiz. Código de saída diferente de zero indica falha.

```shell
./mvnw -B compile spotbugs:check
```

## Configuração e utilização

Executa em `verify`, com esforço Max e limiar High (alta confiança, não sinônimo de severidade).
O plugin falha quando encontra bugs nesse limiar; não há exclusões globais.
Analisa `target/classes`; testes não entram na análise de bytecode.
Resultado: `target/spotbugsXml.xml` e console. Para investigar achados de confiança menor:
`./mvnw -B compile spotbugs:spotbugs -Dspotbugs.threshold=Low`.
Corrija a causa e execute novamente o gate. Evite executar apenas `spotbugs:check` após alterar
fontes sem recompilar. Versões antigas podem não ler bytecode Java 25; mantenha a versão do POM.

## Gate completo

`./mvnw -B clean verify` executa os gates locais, testes, cobertura e Javadoc.
Os testes Quarkus exigem Docker disponível para PostgreSQL Dev Services.

## Referência

[Documentação oficial](https://spotbugs.github.io/spotbugs-maven-plugin/).
