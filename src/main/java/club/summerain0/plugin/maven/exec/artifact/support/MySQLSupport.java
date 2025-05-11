package club.summerain0.plugin.maven.exec.artifact.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MySQL支持类
 *
 * @author summerain0
 */
public class MySQLSupport {
    private final Connection connection;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private MySQLSupport(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }

    /**
     * 获取连接
     *
     * @param url      url
     * @param username 用户名
     * @param password 密码
     * @return MySQL支持类对象
     */
    public static MySQLSupport getConnection(String url, String username, String password) throws SQLException {
        return new MySQLSupport(url, username, password);
    }

    /**
     * 执行SQL
     *
     * @param sql SQL语句
     */
    public void execute(String sql) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.execute();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
