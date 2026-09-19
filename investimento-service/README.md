# investimento-service

Projeto Maven independente, Java 21+. Abra o pom.xml no IntelliJ e execute org.claujunior.investimento.App.

## Executar localmente

    mvn package
    java -jar target/investimento-service-1.0-SNAPSHOT.jar 127.0.0.3 9090

Sem argumentos, usa 127.0.0.3:9090. UDP_BIND_HOST tambem configura o IP; o primeiro argumento tem prioridade.
O primeiro argumento e o IP local para escutar; o segundo e a porta.

Mapa local:
- Gateway: 127.0.0.1:9090 (endereco proposto para teste; o gateway atual ainda escuta em todas as interfaces e nao foi alterado).
- Conta corrente: 127.0.0.2:9090.
- Investimento: 127.0.0.3:9090.

Cada processo precisa escutar somente no seu IP para compartilhar a porta. Nao execute o gateway em 0.0.0.0:9090 nesse teste.

## Na AWS

Em instancias EC2 separadas, execute com o IP privado local da instancia e porta 9090, ou 0.0.0.0 para escutar em todas as interfaces daquela instancia.
O gateway deve enviar para os IPs privados reais dos servicos. 127.x.x.x serve apenas para testes no mesmo computador; 0.0.0.0 e endereco de escuta, nao de destino.
Permita o trafego UDP 9090 entre as instancias nas regras de rede.

## Protocolo inicial e limites

Envie contaCorrente;health ao servico de conta ou investimento;health ao de investimento.
A resposta e 200, quebra de linha e o nome do servico seguido por ;UP.
A resposta volta ao IP e porta de origem. Operacoes bancarias retornam 501 porque ainda nao foram implementadas. Nao ha banco de dados.

O gateway ainda precisa inicializar ClientUDP, registrar os IPs corretos e receber/repassar as respostas ao cliente original. Estes projetos nao enviam heartbeat automatico. Quando implementado, o heartbeat deve sair do IP do respectivo servico para que packet.getAddress() identifique a instancia correta.

Cada pasta possui pom.xml e processo proprios. O Maven do gateway nao compila nem inicia estes projetos automaticamente. Eles compartilham o repositorio Git do gateway.
Para executar o gateway junto usando a mesma porta, sera necessario futuramente configurar o bind dele para 127.0.0.1 ou usar ambientes de rede isolados. Apenas estes dois projetos novos foram adicionados.
