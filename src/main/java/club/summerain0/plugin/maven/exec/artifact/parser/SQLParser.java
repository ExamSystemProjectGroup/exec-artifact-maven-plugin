package club.summerain0.plugin.maven.exec.artifact.parser;

import club.summerain0.plugin.maven.exec.artifact.bean.SQLToken;
import club.summerain0.plugin.maven.exec.artifact.constants.SQLParserState;
import club.summerain0.plugin.maven.exec.artifact.constants.SQLTokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL解析器
 */
public class SQLParser {
    /**
     * 解析SQL语句
     *
     * @param sqlContent SQL语句
     * @return 语句列表
     */
    public static List<SQLToken> parseSQL(String sqlContent) {
        List<SQLToken> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        SQLParserState state = SQLParserState.INIT;

        for (int i = 0; i < sqlContent.length(); i++) {
            char currentChar = sqlContent.charAt(i);
            char nextChar = i + 1 < sqlContent.length() ? sqlContent.charAt(i + 1) : '\0';

            switch (state) {
                case INIT: // 初始状态
                    if (!Character.isWhitespace(currentChar)) {
                        if (currentChar == '-' && nextChar == '-') { // 行级注释
                            state = SQLParserState.IN_LINE_COMMENT;
                            buffer.append(currentChar).append(nextChar);
                            i++;
                        } else if (currentChar == '/' && nextChar == '*') { // 块级注释头部
                            state = SQLParserState.IN_BLOCK_COMMENT;
                            buffer.append(currentChar).append(nextChar);
                            i++;
                        } else { // 其余情况，进入语句解析
                            state = SQLParserState.IN_STATEMENT;
                            buffer.append(currentChar);
                        }
                    }
                    break;

                case IN_STATEMENT: // 正在解析SQL语句
                    if (currentChar == '\'') { // 字符串字面量
                        buffer.append(currentChar);
                        state = SQLParserState.IN_STRING_LITERAL;
                    } else if (currentChar == ';') { // SQL语句结束
                        buffer.append(currentChar);
                        tokens.add(new SQLToken(SQLTokenType.SQL, buffer.toString()));
                        buffer.setLength(0);
                        state = SQLParserState.INIT;
                    } else { // 否则，继续添加字符
                        buffer.append(currentChar);
                    }
                    break;

                case IN_STRING_LITERAL: // 正在解析字符串字面量
                    buffer.append(currentChar);
                    if (currentChar == '\'' && sqlContent.charAt(i - 1) != '\\') { // 字符串结束
                        state = SQLParserState.IN_STATEMENT;
                    }
                    break;

                case IN_LINE_COMMENT: // 正在解析行级注释
                    if (currentChar == '\n') { // 注释结束
                        tokens.add(new SQLToken(SQLTokenType.COMMENT, buffer.toString()));
                        buffer.setLength(0);
                        state = SQLParserState.INIT;
                    } else {
                        buffer.append(currentChar);
                    }
                    break;

                case IN_BLOCK_COMMENT: // 正在解析块级注释
                    buffer.append(currentChar);
                    if (currentChar == '*' && nextChar == '/') { // 注释结束
                        buffer.append('/');
                        i++;
                        tokens.add(new SQLToken(SQLTokenType.COMMENT, buffer.toString()));
                        buffer.setLength(0);
                        state = SQLParserState.INIT;
                    }
                    break;
            }
        }

        // 处理文件结束时的残余内容
        if (!buffer.isEmpty()) {
            if (state == SQLParserState.IN_STATEMENT || state == SQLParserState.IN_STRING_LITERAL) { // 如果是正在解析SQL语句或字符串字面量，则将残余内容视为SQL语句
                tokens.add(new SQLToken(SQLTokenType.SQL, buffer.toString()));
            } else if (state == SQLParserState.IN_LINE_COMMENT || state == SQLParserState.IN_BLOCK_COMMENT) { // 如果是正在解析行级或块级注释，则将残余内容视为注释
                tokens.add(new SQLToken(SQLTokenType.COMMENT, buffer.toString()));
            }
        }

        return tokens;
    }
}
