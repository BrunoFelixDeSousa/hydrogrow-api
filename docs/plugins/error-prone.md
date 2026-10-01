# Error Prone

> Error Prone é uma ferramenta de análise estática para Java que detecta erros comuns de programação em tempo de compilação. Ele é desenvolvido pelo Google e é usado em muitos projetos Java, incluindo o próprio código do Google.

## Instalação neste projeto

add o plugin ao `pom.xml`:

```xml
<properties>
    <error-prone.version>2.50.0</error-prone.version>
</properties>


<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${compiler-plugin.version}</version>
    <configuration>
        <parameters>true</parameters>
        <fork>true</fork>
        <compilerArgs>
            <arg>-XDcompilePolicy=simple</arg>
            <arg>--should-stop=ifError=FLOW</arg>
            <arg>-Xplugin:ErrorProne</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
            <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
        </compilerArgs>
        <annotationProcessorPaths>
            <path>
                <groupId>com.google.errorprone</groupId>
                <artifactId>error_prone_core</artifactId>
                <version>${error-prone.version}</version>
            </path>
        </annotationProcessorPaths>

    </configuration>
</plugin>
```

Execute o comando abaixo na raiz. Código de saída diferente de zero indica falha.

```shell
./mvnw -B clean test-compile
```

## Referência

- [Documentação oficial](https://errorprone.info/docs/installation)
- [Repositório oficial](https://github.com/google/error-prone)
