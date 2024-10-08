package main.java.org;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MyJDBC extends ListenerAdapter {

    private static final String DB_URL = Variable.DB_URL;

    static {
        checkAndCreateStoryTable();
    }

    private static void checkAndCreateStoryTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS OneWordStorys (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "story TEXT, " +
                    "lastUserId VARCHAR(255)" +
                    ")";
            statement.execute(createTableQuery);
            // Ensure there's always one row to update
            statement.execute("INSERT IGNORE INTO OneWordStorys (id, story, lastUserId) VALUES (1, '', '')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentStory() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT story FROM OneWordStorys WHERE id = 1");
            if (resultSet.next()) {
                return resultSet.getString("story");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void saveCurrentStory(String story) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE OneWordStorys SET story = ? WHERE id = 1")) {
            preparedStatement.setString(1, story);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getLastUserId() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT lastUserId FROM OneWordStorys WHERE id = 1");
            if (resultSet.next()) {
                return resultSet.getString("lastUserId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void saveLastUserId(String userId) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE OneWordStorys SET lastUserId = ? WHERE id = 1")) {
            preparedStatement.setString(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean birthdayExists(String userId) {
        String query = "SELECT 1 FROM BirthdaySystem WHERE userId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {
            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }
            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static Map<String, String> getAllBirthdays() {
        Map<String, String> birthdays = new HashMap<>();
        String query = "SELECT * FROM BirthdaySystem";
        try {Connection connection = DriverManager.getConnection(Variable.DB_URL);
            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String userId = resultSet.getString("userId");
                int day = resultSet.getInt("day");
                int month = resultSet.getInt("month");
                int year = resultSet.getInt("year");

                String birthday;

                birthday = day + " " + month + " " + year;

                birthdays.put(userId, birthday);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return birthdays;
    }

    public  static String getBirthdayUser1(String userId) {
        String query = "SELECT day, month, year FROM BirthdaySystem WHERE userId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }

            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int day = resultSet.getInt("day");
                int month = resultSet.getInt("month");
                int year = resultSet.getInt("year");

                    return day + " " + month + " " + year;

            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

        public static String getBirthday(String userId) {
        String query = "SELECT day, month, year FROM BirthdaySystem WHERE userId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }

            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int day = resultSet.getInt("day");
                int month = resultSet.getInt("month");
                int year = resultSet.getInt("year");

                if (year != 0) {
                    return day + "." + month + "." + year;
                } else {
                    return day + "." + month;
                }

            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void removeBirthday(String userId) {

        String query = "DELETE FROM BirthdaySystem WHERE userId = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }

            statement.setString(1, userId);
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addBirthday(String userId, int day, int month, Integer year) {
        String query = "INSERT INTO BirthdaySystem (userId, day, month, year) VALUES (?, ?, ?, ?) ";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }

            String date = year + "-" + month + "-" + day;
            String[] dateSplit = date.split("-");
            if (day < 10) {
                dateSplit[2] = "0" + dateSplit[2];
            }
            if (month < 10) {
                dateSplit[1] = "0" + dateSplit[1];
            }

            int month2 = Integer.parseInt(dateSplit[1]);
            int day2 = Integer.parseInt(dateSplit[2]);


            statement.setString(1, userId);
            statement.setInt(2, day);
            statement.setInt(3, month);
            if (year != null) {
                statement.setInt(4, year);
            } else {
                statement.setNull(4, Types.INTEGER);
            }
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<String> getBirthdaysForDate(int day, int month) {
        List<String> userIds = new ArrayList<>();
        String query = "SELECT userId FROM BirthdaySystem WHERE day = ? AND month = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (!MyJDBC.isBirthdaySystemTableExists()) {
                MyJDBC.createBirthdaySystemTable();
            }

            statement.setInt(1, day);
            statement.setInt(2, month);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                userIds.add(resultSet.getString("userId"));
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
        return userIds;

    }

    public static boolean isBirthdaySystemTableExists() {
        try {
            Connection connection = DriverManager.getConnection(DB_URL/*,DB_USERNAME,DB_PASSWORD*/);

            // Check if the CounterSystem table exists
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "BirthdaySystem", null);
            connection.close();
            return resultSet.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createBirthdaySystemTable() {
        try {
            Connection connection = DriverManager.getConnection(DB_URL/*,DB_USERNAME,DB_PASSWORD*/);

            // SQL query to create CounterSystem table
            String createTableQuery = "CREATE TABLE BirthdaySystem (" +
                    "userId VARCHAR(20)," +
                    "day INT," +
                    "month INT," +
                    "year INT" +
                    ");";

            // Create the CounterSystem table
            Statement statement = connection.createStatement();
            statement.execute(createTableQuery);

            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static String[] getCountNumber() {
        try {
            Connection connection = DriverManager.getConnection(Variable.DB_URL/*,DB_USERNAME,DB_PASSWORD*/);

            // Überprüfe, ob die Tabelle CounterSystem vorhanden ist
            if (!MyJDBC.isCounterSystemTableExists()) {
                MyJDBC.createCounterSystemTable();
            }

            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT * FROM CounterSystem"
            );

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String userId = resultSet.getString("userId");
                int nextNum = resultSet.getInt("nextNum");
                return new String[]{userId, String.valueOf(nextNum)};
            }

            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public static void setCountNumber(String userId, int nextNumber) {
        try {
            Connection connection = DriverManager.getConnection(Variable.DB_URL/*,DB_USERNAME,DB_PASSWORD*/);

            // Überprüfe, ob die Tabelle CounterSystem vorhanden ist
            if (!MyJDBC.isCounterSystemTableExists()) {
                System.out.println("[Counter-System-DB] Tabelle existiert nicht");
                // Tabelle existiert nicht, also erstelle sie
                MyJDBC.createCounterSystemTable();
                System.out.println("[Counter-System-DB] Tabelle wurde erstellt");
            } else {
                System.out.println("[Counter-System-DB] Tabelle existiert bereits");
            }

            PreparedStatement insertStatement = connection.prepareStatement(
                    "UPDATE CounterSystem SET userId = ?, nextNum = ?"
            );

            insertStatement.setString(1, userId);
            insertStatement.setInt(2, nextNumber);
            insertStatement.execute();

            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isCounterSystemTableExists() {
        try {
            Connection connection = DriverManager.getConnection(Variable.DB_URL/*,DB_USERNAME,DB_PASSWORD*/);

            // Check if the CounterSystem table exists
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "CounterSystem", null);
            connection.close();
            return resultSet.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createCounterSystemTable() {
        try {
            Connection connection = DriverManager.getConnection(Variable.DB_URL/*,DB_USERNAME,DB_PASSWORD*/);

            // SQL query to create CounterSystem table
            String createTableQuery = "CREATE TABLE CounterSystem (" +
                    "nextNum INT, " +
                    "userId VARCHAR(20)" +
                    ");";

            // Create the CounterSystem table
            Statement statement = connection.createStatement();
            statement.execute(createTableQuery);

            // Insert initial values into the CounterSystem table
            String insertInitialValuesQuery = "INSERT INTO CounterSystem (nextNum, userId) VALUES (1, 1);";
            statement.execute(insertInitialValuesQuery);

            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
