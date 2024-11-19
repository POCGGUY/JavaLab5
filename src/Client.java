import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8030);
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
             PrintStream serverOutput = new PrintStream(socket.getOutputStream(), true, "UTF-8")) {

            Thread serverListener = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverInput.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Соединение с сервером разорвано.");
                }
            });
            serverListener.start();

            String clientMessage;
            while ((clientMessage = userInput.readLine()) != null) {
                if (clientMessage.equalsIgnoreCase("выход")) {
                    serverOutput.println("выход");
                    break;
                }
                serverOutput.println(clientMessage);
            }

            serverListener.interrupt();
        } catch (IOException e) {
            System.out.println("Ошибка клиента: " + e.getMessage());
        }
    }
}
