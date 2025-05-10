package club.summerain0.plugin.maven.exec.artifact.constants;

import lombok.Getter;

/**
 * 数据库类型枚举
 */
@Getter
public enum DBType {
    SQL("sql", "sql"),
    MONGO("mongo", "mongo"),
    REDIS("redis", "redis");

    /**
     * 数据库类型
     */
    private String type;

    /**
     * 存储目录名称
     */
    private String dirName;

    DBType(String type, String dirName) {
        this.type = type;
        this.dirName = dirName;
    }
}
