# export-service

Microsserviço de exportação assíncrona de pedidos em PDF do HortiControl.

---

## Como rodar

Pré-requisitos: **JDK 21**, **Docker** em execução e o **MySQL** que a API
principal usa.

```bash
# 1. API principal
cd HortiControl_BackEnd && ./mvnw spring-boot:run        # :8080

# 2. este microsserviço 
cd ../export-service && ./mvnw spring-boot:run           # :8081

# 3. frontend
cd ../HortiControl_FrontEnd/frontend && npm run dev      # :5173
```

Não é preciso rodar `docker compose up`. Este repositório traz um
`compose.yaml` com o RabbitMQ, e o `spring-boot-docker-compose` faz o Boot
subir o container ao iniciar em `dev`, esperar o healthcheck e injetar as
credenciais na aplicação.

O container só é derrubado quando você quiser
(`docker compose down` dentro desta pasta); reiniciar a aplicação não recria
o broker nem apaga as filas.

O perfil `dev` já traz valores para o segredo do JWT e para a chave interna,
iguais aos da API principal. Em produção, ambos vêm de variável de ambiente.

| Endereço | O quê |
|---|---|
| http://localhost:8081/swagger-ui.html | Documentação da API |
| http://localhost:15672 | Painel do RabbitMQ (`guest`/`guest`) |
| http://localhost:8081/actuator/health | Health check |

---

## Testes

```bash
./mvnw test
```

---

## A API

### Solicitar — `POST /exportacoes`

```bash
curl -i -X POST http://localhost:8081/exportacoes \
  -H 'Content-Type: application/json' \
  -H "X-XSRF-TOKEN-EXPORTACAO: $TOKEN" \
  --cookie "HORTCONTROL_AUTH=$JWT" \
  -d '{"escopo":"ATIVOS"}'
```

Responde `202 Accepted` — o PDF ainda não existe:

```json
{ "id": "3f1b8a4c-...", "status": "PENDENTE", "recorte": "Pedidos ativos" }
```

Campos aceitos, todos opcionais: `escopo` (`ATIVOS` | `HISTORICO` | `TODOS`),
`mercadoId`, `dataInicio`, `dataFim`, `pedidoIds`.

### Acompanhar — `GET /exportacoes/{id}`

`status` percorre `PENDENTE → PROCESSANDO → CONCLUIDA` (ou `FALHOU`, com
`motivoDaFalha` preenchido).

### Baixar — `GET /exportacoes/{id}/arquivo`

Devolve `application/pdf`. Antes de concluir, responde `409`.

---

## Estrutura

```
domain/          regras e invariantes — nenhum import de framework
application/     casos de uso (usecase) e o que eles precisam (port/out)
infrastructure/  web · messaging · persistence · client · pdf · storage · security
```

---

## Variáveis de ambiente (produção)

| Variável | Para quê |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Banco do microsserviço |
| `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` | Broker |
| `HORTICONTROL_API_URL` | Base URL da API principal |
| `HORTICONTROL_INTERNAL_API_KEY` | Chave serviço-a-serviço; **igual** à da API principal |
| `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE` | **Iguais** aos da API principal |
| `EXPORT_PRAZO_RETIRADA` | Prazo de espera pela retirada do PDF |
| `EXPORT_MAXIMO_AGUARDANDO` | Documentos simultâneos em memória |
| `CORS_ORIGENS` | Origens do frontend |
| `COOKIE_SAME_SITE` | `None` quando API e microsserviço estão em domínios distintos |

### Os relatórios não são armazenados

O serviço **não guarda os PDFs**. Não há diretório de arquivos, nem BLOB no
banco, nem volume no container. O relatório existe no servidor apenas entre a
geração e o download, em memória, e é descartado no instante em que o cliente
o retira, o destino do arquivo é a máquina de quem pediu, e só.

