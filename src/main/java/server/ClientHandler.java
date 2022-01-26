package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static final String COMMAND_PREFIX = "/";
    private static final String SEND_MESSAGE_TO_CLIENT_COMMAND = COMMAND_PREFIX + "w";
    private static final String END_COMMAND = COMMAND_PREFIX + "end";
    private final Socket socket;
    private final ChatServer server;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;

    public ClientHandler(Socket socket, ChatServer server) {                //конструктор в котором
        try {
            this.nick = "";                                                 //задаём первоначальный ник
            this.socket = socket;                                           //передаём сокет из параметров
            this.server = server;                                           /*передаём экземпляр класса ChatServer чтобы вызывать методы
            такие как subscribe, broadcast, unsubscribe, isNickBusy и sendMessageToClient*/
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {                                      //создаём поток в котором происходит аутентификация и чтение сообщений
                try {
                    authenticate();
                    readMessages();
                } finally {                                     //который когда эти два метода выполнятся закроется
                    closeConnection();
                }
            }).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {                               //сначала заходим сюда
        long startTime = System.currentTimeMillis();            //задаём время старта
        /**
         * создаём поток в котором будем сверять время старта с актуальным временем
         * и если разница будет равна 120 секундам то выходим из системы посредством closeConnection
         */
        new Thread(() -> {
            try {
                while (!server.isNickBusy(nick)) {
                    if (System.currentTimeMillis() - startTime == 120000 && !server.isNickBusy(nick)) {
                        System.out.println("System exit");
                        closeConnection();                      /*и казалось бы... Но если другой клиент не успеет авторизоваться
                        то в сервере начинается дичь: миллиард эксепшенов но чат продолжает работу. Два клиента
                        могут вполне себе обмениваться сообщениями*/
                    }
                }
            }finally {
                System.out.println("Timer is done");
            }
        }).start();
        do {
            try {
                final String str = in.readUTF();                //и слушаем входящие сообщения
                if (str.startsWith("/auth")) {                  //если сообщение начинается с /auth
                    final String[] split = str.split(" ");      //разделяем его дщна auth логин и пароль
                    final String login = split[1];
                    final String password = split[2];
                    final String nick = server.getAuthService().getNickByLoginAndPassword(login, password);
                    //берём ник из SimpleAuthService
                    if (nick != null) {                       //проверка ника занят он или нет
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;                       //если занят- всё по новой
                        }
                        sendMessage("/authok " + nick);     //если не занят, говорим что авторизация прошла успешно
                        this.nick = nick;           //приравниваем этот ник к значению ника в ClientHandler'e
                        System.out.println("wow");
                        server.broadcast("Пользователь " + nick + " зашел в чат");
                        server.subscribe(this);   //ложим в хэшмап в сервере экземпляр этого класса
                        break;
                    } else {
                        sendMessage("Неверные логин и пароль");     //если логин и пароль не совпадают с полянми в UserData
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (System.currentTimeMillis() - startTime != 10000);
    }

    public void sendMessage(String message) {                   //метод отсылающие сообщение через out.WriteUTF()
        try {
            System.out.println("SERVER: Send message to " + nick);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {                               //читаем сообщения
        try {
            while (true) {
                final String msg = in.readUTF();                //считываем входяший поток
                System.out.println("Receive message: " + msg);
                if (msg.startsWith(COMMAND_PREFIX)) {           //проверка на команду
                    if (END_COMMAND.equals(msg)) {
                        break;
                    }
                    if (msg.startsWith(SEND_MESSAGE_TO_CLIENT_COMMAND)) { //если сообщение начинается через /w то разделяем его
                        final String[] token = msg.split(" ");
                        final String nick = token[1];       //выдираем из него ник
                        server.sendMessageToClient(this, nick, msg.substring(SEND_MESSAGE_TO_CLIENT_COMMAND.length() + 2 + nick.length()));
                        //сабстрингом выдираем сообщение и отправляем лично клиенту
                    }
                    continue;
                }
                server.broadcast(nick + ": " + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

}
