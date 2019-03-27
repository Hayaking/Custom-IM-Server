package utils;

import java.sql.*;

public class DBUtil {
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://129.204.82.119:3306/imdb?Unicode=true&characterEncoding=utf8&useSSL=false",
                "haya",
                "1024"
        );
        return con;
    }

    public static void close(Connection con, PreparedStatement prst, ResultSet set) {
        try {
            if (set != null) set.close();
            if (prst != null) prst.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
