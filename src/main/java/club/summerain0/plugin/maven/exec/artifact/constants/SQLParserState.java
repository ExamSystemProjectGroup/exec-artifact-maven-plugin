package club.summerain0.plugin.maven.exec.artifact.constants;

/**
 * SQL解析状态枚举
 */
public enum SQLParserState {
    /**
     * 初始状态
     */
    INIT,

    /**
     * 正在解析SQL语句
     */
    IN_STATEMENT,

    /**
     * 正在解析字符串字面量
     */
    IN_STRING_LITERAL,

    /**
     * 正在解析行级注释
     */
    IN_LINE_COMMENT,

    /**
     * 正在解析块级注释
     */
    IN_BLOCK_COMMENT
}
