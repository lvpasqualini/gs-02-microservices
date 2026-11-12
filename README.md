# SaúdePerto — Plataforma de Triagem Clínica e Atendimento Comunitário

**Global Solution 02 — Microservice and Web Engineering & IT Services (3º SIS)**  

---

## 👥 Integrantes da Equipe
| Lucas Pasqualini | RM: 555169 |
| Guilherme Buglioli | RM: 555273 |

---

## 🩺 Contexto do Problema e da Solução

Comunidades ribeirinhas, rurais, indígenas, quilombolas e periféricas frequentemente enfrentam barreiras severas no acesso rápido à triagem clínica e à priorização de atendimentos médicos de urgência. Agentes comunitários de saúde precisam registrar sinais vitais em campo e acionar imediatamente equipes médicas quando um paciente apresenta sinais críticos, sem perder os dados coletados caso haja instabilidade temporária nos sistemas centrais.

O **SaúdePerto** soluciona esse problema por meio de uma **Arquitetura de Microsserviços com Comunicação Síncrona e Resiliência**, composta por:

1. **`eureka-server` (Porta `8761`)**: Servidor de Service Discovery (Netflix Eureka) onde todos os microsserviços e suas múltiplas instâncias se registram dinamicamente.
2. **`api-gateway` (Porta `8082`)**: Ponto único de entrada da arquitetura (Spring Cloud Gateway Server WebFlux) integrado ao Eureka com roteamento dinâmico (`discovery.locator`) e balanceamento de carga (`LoadBalancer`).
3. **`ms-paciente` (Porta aleatória `0`)**: Microsserviço responsável pelo cadastro de pacientes de comunidades vulneráveis e gestão do seu `statusAtendimento` (`CADASTRADO`, `AGUARDANDO_TRIAGEM`, `EM_TRIAGEM`, `ATENDIMENTO_URGENTE`, etc.), com banco de dados MySQL próprio (`db_saudeperto_paciente` na porta `3306`) e versionamento via Flyway.
4. **`ms-triagem` (Porta aleatória `0`)**: Microsserviço responsável pelo registro de triagens clínicas, **Motor Automático de Classificação de Risco Clínico** (`BAIXO`, `MODERADO`, `URGENTE`) com base em pressão arterial, glicemia, temperatura e palavras-chave de sintomas, e **comunicação síncrona via OpenFeign** para atualizar automaticamente o status do paciente no `ms-paciente`. Conta com **Circuit Breaker e Fallback (Resilience4j)** para garantir que, mesmo se o `ms-paciente` estiver indisponível, a triagem seja salva em modo de contingência (`PENDENTE_SINCRONIZACAO`) sem erro 500 não tratado. Possui banco de dados MySQL próprio e independente (`db_saudeperto_triagem` na porta `3307`) com migrations Flyway.

---

## 🛠️ Tecnologias Utilizadas

- **Java 25**
- **Spring Boot 4.1.2** (nos microsserviços `ms-paciente` e `ms-triagem`, atendendo ao requisito >= 4.0.9)
- **Spring Cloud** (Netflix Eureka Server/Client, Spring Cloud Gateway WebFlux, Spring Cloud LoadBalancer, Spring Cloud OpenFeign, Resilience4j Circuit Breaker)
- **Spring Data JPA / Hibernate** + **Bean Validation (`jakarta.validation`)**
- **MySQL 8.4.6** (2 instâncias independentes via Docker Compose)
- **Flyway Migrations** (criação de tabelas `V1` e carga inicial de dados `V2` em ambos os microsserviços)
- **SpringDoc OpenAPI / Swagger UI** (documentação interativa e executável)
- **Lombok** & **Maven Wrapper (`./mvnw`)**

---

## 📂 Estrutura do Repositório (HUB / Monorepo)

```text
saude-perto/
├── compose/
│   └── compose.yaml         # Sobe mysql-paciente (3306) e mysql-triagem (3307)
├── eureka-server/           # Service Discovery (porta 8761)
├── api-gateway/             # API Gateway + Load Balancer (porta 8082)
├── ms-paciente/             # Microsserviço de Pacientes (Java 25, Spring Boot 4.1.2, porta aleatória)
├── ms-triagem/              # Microsserviço de Triagens (Java 25, Spring Boot 4.1.2, porta aleatória)
└── README.md                # Documentação completa de arquitetura, execução e testes
```

---

## 🚀 Passo a Passo para Execução (Ambiente Limpo)

### Pré-requisitos
- **Java JDK 25** instalado e configurado no `PATH` (`java -version`)
- **Docker** e **Docker Compose** em execução (`docker ps`)

---

### Passo 1: Subir os Bancos de Dados MySQL com Docker

A partir da raiz do monorepo (`saude-perto/`), execute:

```bash
cd compose
docker compose up -d
```

*(Ou diretamente da raiz: `docker compose -f compose/compose.yaml up -d`)*

Verifique se os dois containers estão rodando:

```bash
docker ps
```

- **`mysql_paciente_846`**: Porta `3306` | Banco: `db_saudeperto_paciente` | Usuário: `root` | Senha: `root`
- **`mysql_triagem_846`**: Porta `3307` | Banco: `db_saudeperto_triagem` | Usuário: `root` | Senha: `root`

> Os bancos (`createDatabaseIfNotExist=true`), as tabelas (`tb_pacientes` e `tb_triagens`) e os dados iniciais são criados automaticamente pelo **Flyway** (`V1` e `V2`) assim que cada microsserviço inicializa.

---

### Passo 2: Iniciar o `eureka-server` (Porta `8761`)

**Pelo Terminal:**
```bash
cd eureka-server
./mvnw spring-boot:run
```
*(No Windows, utilize `mvnw.cmd spring-boot:run`)*

**Pelo IntelliJ IDEA:**
Execute a classe `com.github.cidarosa.eureka.server.EurekaServerApplication`.

- Painel do Eureka disponível em: **http://localhost:8761**

---

### Passo 3: Iniciar o `api-gateway` (Porta `8082`)

**Pelo Terminal:**
```bash
cd api-gateway
./mvnw spring-boot:run
```

**Pelo IntelliJ IDEA:**
Execute a classe `br.com.fiap.api.gateway.ApiGatewayApplication`.

- O Gateway subirá na porta **`8082`** e se registrará no Eureka.

---

### Passo 4: Iniciar o `ms-paciente` (Múltiplas Instâncias em Porta Aleatória)

Como `server.port=0` e `eureka.instance.instance-id=${spring.application.name}:${random.value}` estão configurados, você pode abrir **2 ou mais terminais** e executar o mesmo comando para subir múltiplas instâncias simultâneas:

**Terminal 1 (Instância 1):**
```bash
cd ms-paciente
./mvnw spring-boot:run
```

**Terminal 2 (Instância 2 - opcional para testar Load Balancer):**
```bash
cd ms-paciente
./mvnw spring-boot:run
```

**Pelo IntelliJ IDEA:**
Execute a classe `com.github.lvpasqualini.ms_paciente.MsPacienteApplication` (para múltiplas instâncias no IntelliJ, habilite *Allow parallel run* em *Run/Debug Configurations*).

---

### Passo 5: Iniciar o `ms-triagem` (Múltiplas Instâncias em Porta Aleatória)

Da mesma forma, abra **1 ou 2 terminais** para iniciar o `ms-triagem`:

**Terminal 1 (Instância 1):**
```bash
cd ms-triagem
./mvnw spring-boot:run
```

**Terminal 2 (Instância 2 - opcional para testar Load Balancer):**
```bash
cd ms-triagem
./mvnw spring-boot:run
```

**Pelo IntelliJ IDEA:**
Execute a classe `com.github.lvpasqualini.ms_triagem.MsTriagemApplication`.

---

## 📖 Documentação Interativa (OpenAPI / Swagger UI)

Com os serviços em execução, a documentação OpenAPI/Swagger pode ser acessada:

- **Via API Gateway (Porta 8082)**:
  - OpenAPI JSON (`ms-paciente`): `http://localhost:8082/ms-paciente/v3/api-docs`
  - OpenAPI JSON (`ms-triagem`): `http://localhost:8082/ms-triagem/v3/api-docs`
- **Diretamente na porta aleatória de cada instância** (consulte a porta atribuída no log de inicialização ou no painel do Eureka em `http://localhost:8761`):
  - Swagger UI (`ms-paciente`): `http://localhost:<PORTA_MS_PACIENTE>/swagger-ui/index.html`
  - Swagger UI (`ms-triagem`): `http://localhost:<PORTA_MS_TRIAGEM>/swagger-ui/index.html`

---

## 🧪 Roteiro Completo de Testes via API Gateway (`http://localhost:8082`)

O `api-gateway` utiliza descoberta dinâmica via Eureka (`spring.cloud.gateway.server.webflux.discovery.locator.enabled=true` e `lower-case-service-id=true`). Assim, todas as chamadas passam pela porta **`8082`** seguindo o padrão:
- **`http://localhost:8082/ms-paciente/api/v1/pacientes`**
- **`http://localhost:8082/ms-triagem/api/v1/triagens`**

---

### 1. Teste de Escala e Load Balancer

Com 2 instâncias de cada microsserviço rodando, execute repetidamente:

```bash
curl http://localhost:8082/ms-paciente/api/v1/pacientes/port
curl http://localhost:8082/ms-paciente/api/v1/pacientes/port
```

```bash
curl http://localhost:8082/ms-triagem/api/v1/triagens/port
curl http://localhost:8082/ms-triagem/api/v1/triagens/port
```

**Resultado esperado:** A cada requisição, o Load Balancer alterna entre as portas aleatórias das instâncias ativas.

---

### 2. Operações CRUD — Microsserviço de Pacientes (`ms-paciente`)

#### Listar todos os pacientes (`GET 200 OK`)
```bash
curl -i http://localhost:8082/ms-paciente/api/v1/pacientes
```

#### Buscar paciente por ID (`GET 200 OK` / `404 Not Found`)
```bash
curl -i http://localhost:8082/ms-paciente/api/v1/pacientes/1
```

#### Buscar paciente por CPF (`GET 200 OK` / `404 Not Found`)
```bash
curl -i http://localhost:8082/ms-paciente/api/v1/pacientes/cpf/12345678901
```

#### Criar novo paciente (`POST 201 Created` + Header `Location`)
```bash
curl -i -X POST http://localhost:8082/ms-paciente/api/v1/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "22233344455",
    "nomeCompleto": "Raimundo Nonato Ferreira",
    "comunidade": "Comunidade Ribeirinha Lago Azul",
    "telefone": "92994445566"
  }'
```

#### Atualizar paciente (`PUT 200 OK`)
```bash
curl -i -X PUT http://localhost:8082/ms-paciente/api/v1/pacientes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "12345678901",
    "nomeCompleto": "Maria da Silva Santos Atualizada",
    "comunidade": "Comunidade Ribeirinha São João",
    "telefone": "92999998888",
    "statusAtendimento": "AGUARDANDO_TRIAGEM"
  }'
```

#### Atualizar status do paciente por CPF (`PATCH 200 OK`)
```bash
curl -i -X PATCH http://localhost:8082/ms-paciente/api/v1/pacientes/cpf/12345678901/status \
  -H "Content-Type: application/json" \
  -d '{
    "statusAtendimento": "EM_TRIAGEM"
  }'
```

#### Excluir paciente (`DELETE 204 No Content`)
```bash
curl -i -X DELETE http://localhost:8082/ms-paciente/api/v1/pacientes/5
```

---

### 3. Operações CRUD e Comunicação Síncrona — Microsserviço de Triagem (`ms-triagem`)

#### Passo A: Consultar status atual do paciente de CPF `11122233344`
```bash
curl -s http://localhost:8082/ms-paciente/api/v1/pacientes/cpf/11122233344
```
*(Observe que o campo `statusAtendimento` está como `"CADASTRADO"`)*

#### Passo B: Registrar uma Triagem Clínica com sinais vitais críticos (`POST 201 Created`)
```bash
curl -i -X POST http://localhost:8082/ms-triagem/api/v1/triagens \
  -H "Content-Type: application/json" \
  -d '{
    "cpfPaciente": "11122233344",
    "pressaoArterial": "190/120",
    "glicemia": 280,
    "temperatura": 39.5,
    "sintomas": "Dor no peito intensa e falta de ar aguda"
  }'
```
**O que acontece internamente:**
1. O `ms-triagem` calcula automaticamente `nivelRisco: "URGENTE"`.
2. O `ms-triagem` chama sincronamente o `ms-paciente` via **OpenFeign** (`PATCH /api/v1/pacientes/cpf/11122233344/status`) sem URL fixa (resolvendo `ms-paciente` pelo Eureka/LoadBalancer).
3. A triagem é salva com `statusTriagem: "SINCRONIZADA_COM_PACIENTE"` e retorna `201 Created` com a URI no header `Location`.

#### Passo C: Confirmar que o status do paciente mudou automaticamente no `ms-paciente`
```bash
curl -s http://localhost:8082/ms-paciente/api/v1/pacientes/cpf/11122233344
```
**Resultado esperado:** O campo `statusAtendimento` do paciente foi alterado automaticamente para `"ATENDIMENTO_URGENTE"`.

#### Outros endpoints CRUD de `ms-triagem`:

- **Listar todas as triagens (`GET 200 OK`)**:
  ```bash
  curl -i http://localhost:8082/ms-triagem/api/v1/triagens
  ```
- **Buscar triagem por ID (`GET 200 OK`)**:
  ```bash
  curl -i http://localhost:8082/ms-triagem/api/v1/triagens/1
  ```
- **Buscar triagens por CPF do paciente (`GET 200 OK`)**:
  ```bash
  curl -i http://localhost:8082/ms-triagem/api/v1/triagens/paciente/11122233344
  ```
- **Atualizar triagem (`PUT 200 OK`)**:
  ```bash
  curl -i -X PUT http://localhost:8082/ms-triagem/api/v1/triagens/1 \
    -H "Content-Type: application/json" \
    -d '{
      "cpfPaciente": "12345678901",
      "pressaoArterial": "155/95",
      "glicemia": 170,
      "temperatura": 38.2,
      "sintomas": "Febre persistente e tontura"
    }'
  ```
- **Excluir triagem (`DELETE 204 No Content`)**:
  ```bash
  curl -i -X DELETE http://localhost:8082/ms-triagem/api/v1/triagens/3
  ```

---

### 4. Teste de Resiliência — Circuit Breaker e Fallback

1. **Derrube todas as instâncias do `ms-paciente`** (`Ctrl + C` nos terminais do `ms-paciente`).
2. **Dispare um novo `POST` de triagem via Gateway**:
   ```bash
   curl -i -X POST http://localhost:8082/ms-triagem/api/v1/triagens \
     -H "Content-Type: application/json" \
     -d '{
       "cpfPaciente": "98765432100",
       "pressaoArterial": "185/115",
       "glicemia": 260,
       "temperatura": 39.2,
       "sintomas": "Desmaio e falta de ar"
     }'
   ```
   **Resultado esperado:**
   - O `@CircuitBreaker` aciona automaticamente o método de fallback `fallbackCriarTriagemPendente`.
   - A requisição **não quebra com erro 500**: a triagem é salva em modo de contingência com `"nivelRisco": "URGENTE"` e `"statusTriagem": "PENDENTE_SINCRONIZACAO"`, retornando `503 Service Unavailable` com o corpo da triagem salva.
3. **Teste do endpoint explícito de sincronização com `@CircuitBreaker`**:
   ```bash
   curl -i -X PATCH http://localhost:8082/ms-triagem/api/v1/triagens/1/sincronizar
   ```
   - Com o `ms-paciente` fora do ar, o método de fallback `fallbackSincronizarPaciente` responde de forma controlada com `503 Service Unavailable` e atualiza o status da triagem para `"PENDENTE_SINCRONIZACAO"`.
   - Assim que o `ms-paciente` voltar a subir, chamar `PATCH /ms-triagem/api/v1/triagens/1/sincronizar` retorna `200 OK` e altera o status da triagem para `"SINCRONIZADA_COM_PACIENTE"`.
