package com.strangevy.mysql2dictionary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Mysql2dictionaryApplication {

    public static void main(String[] args) throws SQLException, IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("请输入地址（默认：127.0.0.1）:");
        String host = scan.nextLine();
        if (host == null || "".equals(host)) {
            host = "127.0.0.1";
        }
        System.out.print("请输入端口（默认：3306）:");
        String port = scan.nextLine();
        if (port == null || "".equals(port)) {
            port = "3306";
        }
        System.out.print("请输入账号（默认：root）:");
        String user = scan.nextLine();
        if (user == null || "".equals(user)) {
            user = "root";
        }
        System.out.print("请输入密码（默认：root）:");
        String password = scan.nextLine();
        if (password == null || "".equals(password)) {
            password = "root";
        }
        System.out.print("请输入数据库（默认：test）:");
        String schema = scan.nextLine();
        if (schema == null || "".equals(schema)) {
            schema = "test";
        }
        // JDBC config:
        String JDBC_URL = "jdbc:mysql://" + host + ":" + port + "/" + schema;
        System.out.println("-------------------------START WRITE-------------------------");
        Connection conn = DriverManager.getConnection(JDBC_URL, user, password);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("show table status");
        List<String> lines = new ArrayList<>();
        while (rs.next()) {
            // 表名
            String name = rs.getString("Name");
            // 描述
            String comment = rs.getString("Comment");
            System.out.println("WRITING:" + name + "(" + comment + ")");
            lines.add(String.format("#### %s %s", name, comment));
            lines.add("| 字段名称 | 字段类型 | 字段注释 |");
            lines.add("| --- | --- | --- |");
            // 表结构
            String sql = "select COLUMN_NAME,COLUMN_TYPE,COLUMN_DEFAULT,COLUMN_COMMENT from information_schema.COLUMNS where table_schema='%s' and table_name='%s'";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format(sql, schema, name));
            while (resultSet.next()) {
                lines.add(String.format("| %s | %s | %s |",
                        resultSet.getString("COLUMN_NAME"), resultSet.getString("COLUMN_TYPE")
                        , resultSet.getString("COLUMN_COMMENT")));
            }
            resultSet.close();
            statement.close();
        }
        rs.close();
        stmt.close();
        conn.close();
        // 写入文件
        Files.write(Paths.get("DataDictionary.md"), lines);
        System.out.println("-------------------------END WRITE-------------------------");
    }

}
