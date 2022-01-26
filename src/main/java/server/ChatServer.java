package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {

    private final AuthService authService;
    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        /*в конструкторе по умолчанию создаём экземпляр класса SimpleAuthService
        для того чтобы через него сравнивать логин и пароль которые вводятся в ClientHandler с аналогичными
        полями в аррэйлисте UserData
        а также создаём хэшмап с клиентами которых будем добавлять и убирать через методы subscribe и unsubscribe
        */
        this.authService = new SimpleAuthService();
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(2013)) {             //открываем серверсокет
            while (true) {
                System.out.println("Wait client connection...");
                final Socket socket = serverSocket.accept();                       //подключение клиента
                new ClientHandler(socket, this);               //создаём нового клиента в который передаём сокет и экземпляр этого сервера
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public synchronized void subscribe(ClientHandler client) {                               //добавляем клиента в HashMap
        clients.put(client.getNick(), client);
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler client) {                             //удаляем
        clients.remove(client.getNick());
        broadcastClientsList();
    }

      public void sendMessageToClient(ClientHandler from, String nickTo, String msg) {
        final ClientHandler client = clients.get(nickTo);           /*экземпляр получателя равен объекту в хэшмапе по нику
        переданному в параметрах метода*/
        if (client != null) {                                       //если такой клиент уже есть
            client.sendMessage("от " + from.getNick() + ": " + msg);        //то вызываем конкретно его метод sendMessage
            from.sendMessage("участнику " + nickTo + ": " + msg);       //точно также с отправителем
            return;
        }
        from.sendMessage("Участника с ником " + nickTo + " нет в чат-комнате");     //если не существует такого экземпляра в хэшмапе
    }

    public void broadcastClientsList() {                                 // информации о клиентах
        StringBuilder clientsCommand = new StringBuilder("/clients ");   //к стрингбилдеру добавляем ники и ставим пробелы
        for (ClientHandler client : clients.values()) {
            clientsCommand.append(client.getNick()).append(" ");
        }
        broadcast(clientsCommand.toString());
    }

    public synchronized void broadcast(String msg) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(msg);                                //sendMessage для каждого клиента
        }
    }
}
