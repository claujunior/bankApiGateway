
# Controle dos processos

Cada servico possui um servidor HTTP de controle que permanece ativo na porta `8008`. Ele permite que o API Gateway pare e reinicie os servidores HTTP, UDP e gRPC do servico sem encerrar o controlador.

Endpoints publicados pelo Gateway:

```text
POST /contaCorrente/start
POST /contaCorrente/stop
GET  /contaCorrente/status

POST /investimento/start
POST /investimento/stop
GET  /investimento/status
```


