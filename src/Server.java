import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 8030;
    private static ConcurrentHashMap<String, PrintStream> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                 PrintStream output = new PrintStream(socket.getOutputStream(), true, "UTF-8")) {

                output.println("Введите ваше имя:");
                clientName = input.readLine();
                if (clientName == null || clientName.isEmpty()) {
                    output.println("Имя не может быть пустым. Соединение закрыто.");
                    socket.close();
                    return;
                }

                synchronized (clients) {
                    clients.put(clientName, output);
                }
                output.println("Добро пожаловать, " + clientName + "!");
                System.out.println("Подключён: " + clientName);

                String message;
                while ((message = input.readLine()) != null) {
                    if (message.equalsIgnoreCase("выход")) {
                        break;
                    } else if (message.equalsIgnoreCase("получить список пользователей")) {
                        sendUserList(output);
                    } else {
                        sendMessage(message, output);
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка клиента: " + e.getMessage());
            } finally {
                if (clientName != null) {
                    System.out.println("Отключён: " + clientName);
                    clients.remove(clientName);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Ошибка при закрытии сокета: " + e.getMessage());
                }
            }
        }

        private void sendUserList(PrintStream output) {
            synchronized (clients) {
                if (clients.isEmpty()) {
                    output.println("Список пользователей пуст.");
                } else {
                    output.println("Доступные пользователи:");
                    clients.keySet().forEach(output::println);
                }
            }
        }

        private void sendMessage(String message, PrintStream senderOutput) {
            String[] parts = message.split(":", 2);
            if (parts.length != 2) {
                senderOutput.println("Формат сообщения: Имя_получателя:Сообщение");
                return;
            }

            String recipient = parts[0].trim();
            String msg = parts[1].trim();

            synchronized (clients) {
                PrintStream recipientOutput = clients.get(recipient);
                if (recipientOutput != null) {
                    recipientOutput.println(clientName + " пишет: " + msg);
                } else {
                    senderOutput.println("Пользователь " + recipient + " не найден.");
                }
            }
        }
    }
}

