package server;


import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psGetNickname;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNick;

    private static PreparedStatement psAddMassage;
    private static PreparedStatement psGetMassageForNick;

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            prepareAllStatements();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException {
        psGetNickname = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO user(login, password, nickname) VALUES (?, ?, ?);");
        psChangeNick = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");

        psAddMassage = connection.prepareStatement("INSERT INTO messages (sender, receiver, text, data) VALUE (\n" +
                "(SELECT id FROM users WHERE nickname =?),\n" +
                "(SELECT id FROM users WHERE nickname =?),\n" + "?, ?)");

        psGetMassageForNick = connection.prepareStatement("SELECT (SELECT nickname FROM users WHERE id = sender), \n" +
                "text, \n" +
                "date, \n" +
                "FROM messages \n" +
                "WHERE sender = (SELECT id FROM users WHERE nickname = ?)\n" +
                "OR receiver = (SELECT id FROM users WHERE nickname =?)\n" +
                "OR receiver  = (SELECT id FROM users WHERE nickname = 'null')");

    }


    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet rs = psGetNickname.executeQuery();
            if (rs.next()) {
                nick = rs.getString(1);
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return nick;
    }


    public static boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static boolean changeNick(String oldNickname, String newNickname) {
        try {
            psChangeNick.setString(1, newNickname);
            psChangeNick.setString(2, oldNickname);
            psChangeNick.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static boolean addMessage(String sender, String receiver, String text, String date) {
        try {
            psAddMassage.setString(1, sender);
            psAddMassage.setString(2, receiver);
            psAddMassage.setString(3, text);
            psAddMassage.setString(4, date);
            psAddMassage.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public static String getPsGetMassageForNick(String nick) {
        StringBuilder sb = new StringBuilder();

        try {
            psGetMassageForNick.setString(1, nick);
            psGetMassageForNick.setString(2, nick);
            ResultSet rs = psGetMassageForNick.executeQuery();

            while (rs.next()){
                String sender = rs.getString(1);
                String receiver = rs.getString(2);
                String text = rs.getString(3);
                String date = rs.getString(4);

                if (receiver.equals("null")) {
                    sb.append(String.format("[ %s ] : %s\n", sender, text));
                } else {
                    sb.append(String.format("[ %s ] to [ %s ] : %s\n", sender, receiver, text));
                }
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sb.toString();
    }


    public static void disconnect() {
        try {
            psRegistration.close();
            psGetNickname.close();
            psChangeNick.close();
            psAddMassage.close();
            psGetMassageForNick.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
