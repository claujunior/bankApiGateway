# Bank API - projetos Maven

O pom.xml da raiz centraliza dependencias, versoes, Java 21 e plugins. Cada modulo gera seu proprio JAR e nao depende do codigo dos demais.

```text
bankApiGateway/
  pom.xml                       # parent e agregador; nao gera JAR
  gateway/pom.xml
  gateway/src/
  conta-corrente-service/pom.xml
  conta-corrente-service/src/
  investimento-service/pom.xml
  investimento-service/src/
```

## IntelliJ

Abra ou recarregue apenas o pom.xml da raiz como projeto Maven. Os tres modulos serao importados pela lista modules. Se uma configuracao de execucao antiga apontar para grpcprojeto, selecione o modulo gateway.

## Compilar

Requer JDK 21 ou superior e Maven. A partir da raiz:

    mvn clean package

Para compilar apenas um modulo:

    mvn -pl gateway -am package
    mvn -pl conta-corrente-service -am package
    mvn -pl investimento-service -am package

Saidas:

- gateway/target/gateway-1.0-SNAPSHOT.jar
- conta-corrente-service/target/conta-corrente-service-1.0-SNAPSHOT.jar
- investimento-service/target/investimento-service-1.0-SNAPSHOT.jar

Os JARs usam dependencias externas e nao estao configurados como JARs executaveis com java -jar.

## Independencia

Os modulos compartilham a configuracao Maven pelo parent, mas mantem fontes, protobuf e target separados. Nao existe dependencia Maven entre os tres modulos. Eles podem rodar como processos separados; na compilacao, precisam do POM pai (../pom.xml, ou publicado/instalado no repositorio Maven).

Alteracoes nas dependencias ou plugins do POM principal sao herdadas pelos tres modulos. Dependencias exclusivas de um modulo podem ser declaradas no POM dele.

## Bancos de dados

Os dois servicos possuem inicializacao e persistencia independente via JDBC/SQLite. Consulte [BANCOS.md](BANCOS.md) para executar, configurar e testar.
