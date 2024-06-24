/**
 * С помощью JDBC, выполнить следующие пункты:
 * 1. Создать таблицу Person (скопировать код с семниара)
 * 2. Создать таблицу Department (id bigint primary key, name varchar(128) not null)
 * 3. Добавить в таблицу Person поле department_id типа bigint (внешний ключ)
 * 4. Написать метод, который загружает имя department по  person
 * 5. * Написать метод, который загружает Map<String, String>, в которой маппинг person.name -> department.name
 *   Пример: [{"person #1", "department #1"}, {"person #2", "department #3}]
 * 6. ** Написать метод, который загружает Map<String, List<String>>, в которой маппинг department.name -> <person.name>
 *   Пример:
 *   [
 *     {"department #1", ["person #1", "person #2"]},
 *     {"department #2", ["person #3", "person #4"]}
 *   ]
 *
 *  7. *** Создать классы-обертки над таблицами, и в пунктах 4, 5, 6 возвращать объекты.
 */

package org.example;

import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args){

        String url = "jdbc:mysql://localhost:3306/test";
        String user = "root";
        String password = "996633";

        try (Connection conn = DriverManager.getConnection(url, user, password)){
            createTable(conn);
            insertTable(conn);
            selectTable(conn);
            updateTable(conn);
            System.out.println(getPersonDepartmentName(conn, 1));

            // работа с базой данных
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    static void createTable(Connection conn){
        try(Statement statement = conn.createStatement()) {
            statement.execute("drop table person;");
            statement.execute("drop table department;");
            statement.execute("""
        create table person (
          id bigint primary key,
          department_id bigint,
          name varchar(256),
          age integer,
          active boolean
        );
        """);
            statement.execute("""
        create table department (
          id bigint primary key,
          name varchar(128) not null
        );
        """);
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    static void insertTable(Connection conn){
        try (Statement statement = conn.createStatement()) {
            StringBuilder insertQuery = new StringBuilder("insert into person(id, department_id, name, age, active) values\n");
            for (int i = 1; i <= 10; i++) {
                int age = ThreadLocalRandom.current().nextInt(20, 60);
                boolean active = ThreadLocalRandom.current().nextBoolean();
                long department = ThreadLocalRandom.current().nextLong(1, 4);
                insertQuery.append(String.format("(%s, %s, '%s', %s, %s)", i, department, "Person #" + i, age, active));

                if (i != 10) {
                    insertQuery.append(",\n");
                }
            }

            int insertCount = statement.executeUpdate(insertQuery.toString());
            System.out.println("Вставлено строк: " + insertCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Statement statement = conn.createStatement()) {
            StringBuilder insertQuery = new StringBuilder("insert into department(id, name) values\n");
            for (int i = 1; i <= 3; i++) {
                insertQuery.append(String.format("(%s, '%s')", i, "Department #" + i));

                if (i != 3) {
                    insertQuery.append(",\n");
                }
            }

            int insertCount = statement.executeUpdate(insertQuery.toString());
            System.out.println("Вставлено строк: " + insertCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void selectTable(Connection conn){
        try(Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select name from person");
            while (resultSet.next()){
                String name = resultSet.getString("name");
                System.out.println(name);
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    private static void updateTable(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            int updateCount = statement.executeUpdate("update person set active = true where id > 5");
            System.out.println("Обновлено строк: " + updateCount);
        }

//        try (PreparedStatement statement =
//                     conn.prepareStatement("select name from person where age = ?")) {
//            statement.setInt(1, Integer.parseInt(age));
//            ResultSet resultSet = statement.executeQuery();
//
//            List<String> names = new ArrayList<>();
//            while (resultSet.next()) {
//                names.add(resultSet.getString("name"));
//            }
//            return names;
//        }
    }

    private static String getPersonDepartmentName(Connection conn, long personId) {
        long departmentId = 0;
        String departmentName = "";
        try(Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select department_id from person where id = " + personId);

            while (resultSet.next()){
                departmentId = resultSet.getLong("department_id");
            }

            resultSet = statement.executeQuery("select name from department where id = " + departmentId);
            while (resultSet.next()){
                departmentName =  resultSet.getString("name");
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return departmentName;
    }
}