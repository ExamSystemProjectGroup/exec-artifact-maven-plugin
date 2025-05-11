package club.summerain0.plugin.maven.exec.artifact.config;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * MySQL链接配置
 */
@Data
public class MySQLConfig {
    /**
     * 数据库地址
     */
    @Parameter
    private String host;

    /**
     * 端口
     */
    @Parameter
    private String port;

    /**
     * 数据库名
     */
    @Parameter
    private String database;

    /**
     * 用户名
     */
    @Parameter
    private String username;

    /**
     * 密码
     */
    @Parameter
    private String password;

    /**
     * 获取链接地址
     *
     * @return 链接地址
     */
    public String getUrl() {
        return String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                host,
                port,
                database
        );
    }
}
