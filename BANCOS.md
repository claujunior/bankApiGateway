# Bancos dos servicos (Java 21 + JDBC)

Cada servico usa seu proprio arquivo SQLite. Nao ha Spring, Hibernate, JPA ou ORM.
A unica dependencia nova de execucao e o driver JDBC org.xerial:sqlite-jdbc.
Os arquivos e a tabela contas sao criados na primeira inicializacao, sem instalar um servidor de banco.

| Servico | Arquivo padrao | Variavel de ambiente | Propriedade Java |
| --- | --- | --- | --- |
| Conta corrente | data/conta-corrente.db | CONTA_CORRENTE_DB_PATH | contacorrente.db.path |
| Investimento | data/investimento.db | INVESTIMENTO_DB_PATH | investimento.db.path |

Caminhos relativos usam a pasta de trabalho do processo. Para um local fixo, configure um caminho absoluto.
Nao configure os dois servicos com o mesmo arquivo. O banco do investimento nao consulta o banco da conta corrente.

## Estrutura

- database/Database.java: caminho do banco, conexoes JDBC e criacao da tabela.
- repository/ContaRepository.java: SQL com PreparedStatement e fechamento automatico das conexoes.
- service/Service.java: validacoes e operacoes utilizadas pelos atendimentos HTTP, UDP e gRPC.
- App.java: inicializa o banco e os tres servidores.

Tabela contas: cpf (chave primaria), nome e saldo_centavos (inteiro).
Os valores sao convertidos com BigDecimal, sem arredondamento silencioso.
CPF aceita 11 digitos, com ou sem pontos e hifen; nao valida os digitos verificadores.
O saldo maximo e 999999999999.99. Nao se permite saldo negativo, CPF duplicado ou exclusao de conta com saldo.
Cada alteracao de saldo e um UPDATE atomico com verificacao de limite e saldo na mesma instrucao.

## Compilar e executar

Requer Java 21+ e Maven no PATH. Na raiz do projeto:

```powershell
mvn -pl conta-corrente-service,investimento-service -am package
mvn -f conta-corrente-service/pom.xml dependency:copy-dependencies
mvn -f investimento-service/pom.xml dependency:copy-dependencies
```

Em dois terminais PowerShell, a partir da raiz:

```powershell
java -cp "conta-corrente-service/target/classes;conta-corrente-service/target/dependency/*" org.claujunior.contacorrente.App
java -cp "investimento-service/target/classes;investimento-service/target/dependency/*" org.claujunior.investimento.App
```

No Linux/macOS, troque o separador de classpath ; por :.

| Servico | HTTP | UDP | gRPC |
| --- | --- | --- | --- |
| Conta corrente | 8081 | 9091 | 50051 |
| Investimento | 8082 | 9092 | 50052 |

As portas sao configuraveis com -Dhttp.port, -Dudp.port e -Dgrpc.port, antes do nome da classe.
Exemplo de banco fixo: java "-Dcontacorrente.db.path=C:/dados/conta.db" -cp "..." org.claujunior.contacorrente.App.

## HTTP direto nos servicos

Cadastro por JSON (aceita saldo inicial, padrao zero):

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8081/contaCorrente/criar -ContentType application/json -Body '{"nome":"Maria","CPF":"12345678901","saldo":100.50}'
Invoke-RestMethod -Uri http://localhost:8081/contaCorrente/saldo/12345678901
Invoke-RestMethod -Method Put -Uri http://localhost:8081/contaCorrente/attsaldo/12345678901/25.00
Invoke-RestMethod -Method Put -Uri http://localhost:8081/contaCorrente/attsaldo/12345678901/-10.00
Invoke-RestMethod -Method Post -Uri http://localhost:8082/investimento/criar -ContentType application/json -Body '{"nome":"Maria","CPF":"12345678901"}'
Invoke-RestMethod -Method Put -Uri http://localhost:8082/investimento/guardar/12345678901/50.00
Invoke-RestMethod -Method Put -Uri http://localhost:8082/investimento/resgatar/12345678901/20.00
Invoke-RestMethod -Uri http://localhost:8082/investimento/saldo/12345678901
```

- POST /{servico}/criar/{nome}/{cpf} tambem cria com saldo zero; codifique o nome como segmento de URL.
- GET /{servico}/health verifica a conexao.
- DELETE /{servico}/deletar/{cpf} remove somente contas com saldo zero.
- attsaldo soma um valor assinado ao saldo: positivo credita, negativo debita.
- guardar e resgatar recebem valores positivos e operam apenas no saldo do investimento.
- GET /{servico}/saldo/{cpf} retorna JSON com nome e saldo, por exemplo: {"nome":"Maria","saldo":100.50}.
  Content-Type: application/json; charset=UTF-8. O saldo e numerico e mantem duas casas decimais no JSON.
- As demais respostas continuam em texto UTF-8: 200 sucesso, 400 dados invalidos, 404 inexistente, 405 operacao/metodo incorreto, 409 conflito e 500 falha interna.
- Requisicoes com corpo devem ter Content-Length; transferencia chunked nao e suportada.

## UDP

UTF-8; a resposta mantem o formato codigo + quebra de linha + mensagem.
Exemplos, enviados a porta do servico correspondente:

```text
contaCorrente;criar;Maria;12345678901
contaCorrente;attsaldo;12345678901;100.50
contaCorrente;saldo;12345678901
investimento;criar;Maria;12345678901
investimento;guardar;12345678901;50.00
investimento;resgatar;12345678901;10.00
investimento;saldo;12345678901
investimento;deletar;12345678901
investimento;health
```

As operacoes financeiras agora exigem o valor, ausente no esqueleto anterior.
UDP nao tem deduplicacao: reenviar um credito/debito pode aplica-lo novamente.

## gRPC

O RPC existente CadastrarCliente salva nome, CPF e saldo inicial no banco do servico.
CadastroResponse.sucesso so e true depois da gravacao. Idade continua no contrato protobuf, mas nao faz parte da conta persistida.
O contrato protobuf foi preservado; ele ainda nao oferece RPC de consulta ou movimentacao.

## Testes sem framework

Depois de compilar e copiar as dependencias com os comandos acima:

```powershell
java -cp "conta-corrente-service/target/test-classes;conta-corrente-service/target/classes;conta-corrente-service/target/dependency/*" org.claujunior.contacorrente.PersistenceTest
java -cp "investimento-service/target/test-classes;investimento-service/target/classes;investimento-service/target/dependency/*" org.claujunior.investimento.PersistenceTest
```

Para validar a consulta HTTP por CPF e a resposta JSON:

```powershell
java -cp "conta-corrente-service/target/test-classes;conta-corrente-service/target/classes;conta-corrente-service/target/dependency/*" org.claujunior.contacorrente.ConsultaHttpTest
java -cp "investimento-service/target/test-classes;investimento-service/target/classes;investimento-service/target/dependency/*" org.claujunior.investimento.ConsultaHttpTest
```

Sao programas Java com verificacoes explicitas; Maven compila esses testes, mas nao os executa automaticamente.
Eles usam bancos temporarios e verificam persistencia, bancos distintos, concorrencia, duplicidade, limites e validacao.

## Integracao com o gateway existente

Os bancos estao ligados aos dois servicos. O gateway ainda possui encaminhamento com portas fixas,
respostas HTTP fixas e coordenacao 2PC incompleta. Para validar esta integracao, use as portas diretas acima.
Em implantacoes separadas, as portas dos servicos podem ser ajustadas para as esperadas pelo gateway.
Guardar/resgatar nao transfere valores entre os dois bancos. Transferencia distribuida, replicacao,
registro de replicas/heartbeat automatico e recuperacao 2PC nao foram implementados nesta alteracao.
