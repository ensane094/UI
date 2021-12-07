package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private final List<UserData> users;

    public SimpleAuthService() {                        //когда создаётся экземпляр этого класса то конструктором создаём в нём
        users = new ArrayList<>();                      //ArrayList пользователей типа UserData у которых есть поля ник, логин и пароль
        for (int i = 0; i < 5; i++) {
            users.add(new UserData("login" + i, "pass" + i, "nick" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {        //переопределяем метод интерфейса AuthService
        for (UserData user : users) {                                               //пробегаем по ArrayList с пользователями
            if (user.login.equals(login) && user.password.equals(password)) {       //и если логин пользователя вызывающего этот метод и его пароль
                return user.nick;                                                   //совпадают с тем пользователем что есть в листе, то возвращаем ник
            }
        }
        return null;
    }

    private static class UserData {                 //внутренний класс с полями данных о клиенте
        private final String login;
        private final String password;
        private final String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

}
