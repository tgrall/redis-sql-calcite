package io.redis.demo.sql;

import org.apache.calcite.jdbc.CalciteConnection;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


public class App {





    public static void main(String[] args) throws URISyntaxException {

        System.out.println("== Start ==");

        JedisPool pool = new JedisPool("redis://localhost:6379");

        LoadDataInRedis redisLoader =  new LoadDataInRedis(pool);

        // Create a HASH where each field is a JSON Record
        // HSET "employees" "row_1"  "{\"emp_no\":10, \"first_name\":\"John\", \"last_name\":\"Doe\", \"salary\": 5500.5, \"bonus\" : 100, \"dept_no\" : 10}"
        redisLoader.loadJsonInHash();


        redisLoader.loadCsvInHas();


        // Load schema definition
        URL res = App.class.getClassLoader().getResource("model.json");
        File file = Paths.get(res.toURI()).toFile();
        String absolutePath = file.getAbsolutePath();

        try {

            Class.forName("org.apache.calcite.jdbc.Driver");
            Properties info = new Properties();

            Connection connection =
                    DriverManager.getConnection("jdbc:calcite:model="+absolutePath);


            CalciteConnection calciteConnection = (CalciteConnection)connection;


            // QUERY HASH & JSON ======
            System.out.println("\n ====== CALL EMPLOYEES : JSON IN HASH");
            Statement statement = calciteConnection.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select * from \"employees\"");

            ResultSetMetaData rsmd = rs.getMetaData();
            List<String> columns = new ArrayList<String>(rsmd.getColumnCount());
            for(int i = 1; i <= rsmd.getColumnCount(); i++){
                columns.add(rsmd.getColumnName(i));
            }

            while(rs.next()){
                System.out.println("EMP_NO : "+ rs.getString("emp_no"));
                for(String col : columns) {
                    System.out.println("\t"+ col +" : "+ rs.getString(col));
                }
                System.out.println("\t--- --- ---");
            }

            rs.close();
            statement.close();


            // QUERY HASH & CSV ======
            System.out.println("\n ====== CALL DEPARTMENTS : CSV IN HASH");
            statement = calciteConnection.createStatement();
            rs = statement.executeQuery(
                    "select * from \"departments\"");

            rsmd = rs.getMetaData();
            columns = new ArrayList<String>(rsmd.getColumnCount());
            for(int i = 1; i <= rsmd.getColumnCount(); i++){
                columns.add(rsmd.getColumnName(i));
            }

            while(rs.next()){
                System.out.println("DEPT_NO : "+ rs.getString("dept_no"));
                for(String col : columns) {
                    System.out.println("\t"+ col +" : "+ rs.getString(col));
                }
                System.out.println("\t--- --- ---");
            }

            rs.close();
            statement.close();




            // JOIN HASH & CSV ======
            System.out.println("\n ====== JOIN 2 Tables & where clause");
            statement = calciteConnection.createStatement();
            rs = statement.executeQuery(
                    "select e.FIRST_NAME, e.LAST_NAME, d.NAME from  \"employees\" e, \"departments\" d  "+
                            "where e.DEPT_NO = d.DEPT_NO AND e.DEPT_NO = 20"
                    );

            rsmd = rs.getMetaData();
            columns = new ArrayList<String>(rsmd.getColumnCount());
            for(int i = 1; i <= rsmd.getColumnCount(); i++){
                columns.add(rsmd.getColumnName(i));
            }
            while(rs.next()){
                for(String col : columns) {
                    System.out.println("\t"+ col +" : "+ rs.getString(col));
                }
                System.out.println("\t--- --- ---");
            }

            rs.close();
            statement.close();








            connection.close();


        } catch(Exception e){
            e.printStackTrace();
        }


        System.out.println("== Stop ==");

    }

}
