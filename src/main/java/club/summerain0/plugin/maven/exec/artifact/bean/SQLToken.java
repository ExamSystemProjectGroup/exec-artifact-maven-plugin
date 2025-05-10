package club.summerain0.plugin.maven.exec.artifact.bean;

import club.summerain0.plugin.maven.exec.artifact.constants.SQLTokenType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class SQLToken {
    /**
     * 语句类型
     */
    private SQLTokenType type;

    /**
     * 语句内容
     */
    private String content;

    /**
     * 执行注释
     */
    private String executeComment;

    public SQLToken(SQLTokenType type, String content) {
        this.type = type;
        this.content = content;
    }
}
