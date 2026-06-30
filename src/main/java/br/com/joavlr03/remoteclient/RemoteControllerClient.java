package br.com.joavlr03.remoteclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class RemoteControllerClient extends WebSocketClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    public RemoteControllerClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Conectado à API. Aguardando comandos...");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Comando recebido: " + message);
        try {
            JsonNode json = mapper.readTree(message);
            String type = json.get("type").asText();
            executeCommand(type);
        } catch (Exception e) {
            System.err.println("Erro ao processar comando: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Desconectado: " + reason);
        System.out.println("Tentando reconectar em 5 segundos...");
        try {
            Thread.sleep(5000);
            this.reconnect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Erro WebSocket: " + ex.getMessage());
    }

    private void executeCommand(String type) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            switch (type) {
                case "SHUTDOWN":
                    pb = os.contains("win")
                            ? new ProcessBuilder("cmd.exe", "/c", "shutdown /s /t 60")
                            : new ProcessBuilder("shutdown", "-h", "now");
                    break;
                case "RESTART":
                    pb = os.contains("win")
                            ? new ProcessBuilder("cmd.exe", "/c", "shutdown /r /t 60")
                            : new ProcessBuilder("shutdown", "-r", "now");
                    break;
                case "SLEEP":
                    pb = os.contains("win")
                            ? new ProcessBuilder("cmd.exe", "/c", "rundll32.exe powrprof.dll,SetSuspendState 0,1,0")
                            : new ProcessBuilder("systemctl", "suspend");
                    break;
                case "LOCK":
                    pb = os.contains("win")
                            ? new ProcessBuilder("cmd.exe", "/c", "rundll32.exe user32.dll,LockWorkStation")
                            : new ProcessBuilder("loginctl", "lock-session");
                    break;
                default:
                    System.out.println("Comando desconhecido: " + type);
                    return;
            }

            System.out.println("Executando: " + type);
            pb.inheritIO().start();

        } catch (Exception e) {
            System.err.println("Erro ao executar comando: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String apiUrl = "ws://localhost:9000/ws/commands";
        RemoteControllerClient client = new RemoteControllerClient(new URI(apiUrl));
        client.connect();
        System.out.println("Cliente iniciado. Conectando em " + apiUrl);

        // Mantém o programa rodando
        Thread.currentThread().join();
    }
}
