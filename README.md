# 🖥️ RemoteClient

Cliente Java/Spring Boot que se conecta a um servidor via **WebSocket** e executa comandos remotos de gerenciamento de energia (desligar, reiniciar, suspender e bloquear) na máquina local, com suporte multiplataforma (Windows/Linux).

---

## 📖 Descrição

O **RemoteClient** é uma aplicação cliente desenvolvida em Java com Spring Boot que estabelece uma conexão WebSocket persistente com um servidor de controle remoto. Ao receber comandos em formato JSON, o cliente interpreta o tipo de comando e executa a ação correspondente no sistema operacional hospedeiro, utilizando `ProcessBuilder` para invocar comandos nativos do SO.

A aplicação foi pensada para cenários de **administração remota de máquinas**, permitindo que um servidor centralizado dispare ações de energia (shutdown, restart, sleep, lock) em estações de trabalho conectadas, com reconexão automática em caso de queda de link.

---

## ✅ Funcionalidades

- ✅ Conexão persistente via WebSocket com servidor remoto
- ✅ Reconexão automática após 5 segundos em caso de desconexão
- ✅ Interpretação de comandos recebidos em formato JSON
- ✅ Execução de comandos de energia: `SHUTDOWN`, `RESTART`, `SLEEP`, `LOCK`
- ✅ Suporte multiplataforma (Windows e Linux)
- ✅ Logs de status no console (conexão, comando recebido, execução, erros)

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| **Java** | Linguagem principal |
| **Spring Boot** | Bootstrap e ciclo de vida da aplicação |
| **Java-WebSocket** (`org.java_websocket`) | Cliente WebSocket |
| **Jackson** (`com.fasterxml.jackson`) | Parsing de mensagens JSON |

---

## 📂 Estrutura do Projeto

```
br/com/joavlr03/remoteclient/
├── RemoteclientApplication.java   # Classe principal Spring Boot
└── RemoteControllerClient.java    # Cliente WebSocket e execução de comandos
```

---

## 🏗️ Arquitetura

Projeto simples de camada única (cliente), organizado em:

- **`RemoteclientApplication`** — ponto de entrada Spring Boot, responsável pelo bootstrap do contexto da aplicação.
- **`RemoteControllerClient`** — estende `WebSocketClient` e concentra toda a lógica de conexão, recepção de mensagens, reconexão e execução de comandos do sistema operacional.

---

## ▶️ Como Executar

### Pré-requisitos
- JDK 17+
- Maven ou Gradle (conforme o build configurado no projeto)
- Um servidor WebSocket disponível em `ws://localhost:9000/ws/commands` (endpoint esperado pelo cliente)

### Passos

```bash
# Clonar o repositório
git clone <url-do-repositorio>
cd remoteclient

# Executar com Maven
./mvnw spring-boot:run

# ou com Gradle
./gradlew bootRun
```

Ao iniciar, o cliente tentará se conectar automaticamente ao endereço configurado:

```
ws://localhost:9000/ws/commands
```

---

## 🔌 Comunicação (WebSocket)

O cliente escuta mensagens JSON no seguinte formato:

```json
{
  "type": "SHUTDOWN"
}
```

### Comandos suportados

| Comando | Ação (Windows) | Ação (Linux) |
|---|---|---|
| `SHUTDOWN` | `shutdown /s /t 60` | `shutdown -h now` |
| `RESTART` | `shutdown /r /t 60` | `shutdown -r now` |
| `SLEEP` | `rundll32.exe powrprof.dll,SetSuspendState 0,1,0` | `systemctl suspend` |
| `LOCK` | `rundll32.exe user32.dll,LockWorkStation` | `loginctl lock-session` |

Comandos não reconhecidos são ignorados e registrados no console.

---

## 🔄 Fluxo da Aplicação

```
Aplicação inicia
       ↓
Conecta ao servidor WebSocket
       ↓
Aguarda mensagens JSON
       ↓
Identifica o campo "type"
       ↓
Executa o comando de sistema correspondente
       ↓
Em caso de desconexão → aguarda 5s → reconecta
```

---

## 📁 Organização do Código

- **`RemoteclientApplication.java`** — inicializa o contexto Spring Boot da aplicação.
- **`RemoteControllerClient.java`** — gerencia o ciclo de vida da conexão WebSocket (`onOpen`, `onMessage`, `onClose`, `onError`) e traduz comandos JSON em chamadas de sistema operacional via `ProcessBuilder`.

---

## 🚀 Possíveis Melhorias

- 🔐 Adicionar autenticação/autorização na conexão WebSocket
- ⚙️ Externalizar a URL do servidor em arquivo de configuração (`application.properties`/`application.yml`)
- 🧪 Adicionar testes automatizados
- 🛡️ Validar a origem das mensagens recebidas antes de executar comandos
- 📝 Melhorar tratamento e padronização de logs (uso de SLF4J/Logback)
- 🔁 Implementar backoff exponencial na reconexão

---
