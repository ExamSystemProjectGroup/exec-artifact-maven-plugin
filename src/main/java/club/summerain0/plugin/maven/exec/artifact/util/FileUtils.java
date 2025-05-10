package club.summerain0.plugin.maven.exec.artifact.util;

import club.summerain0.plugin.maven.exec.artifact.bean.SQLToken;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * 文件工具类
 */
public class FileUtils {
    /**
     * 获取artifact版本目录
     *
     * @param baseDir     基准目录
     * @param versionName artifact版本
     * @return 路径
     */
    public static String getArtifactVersionDir(String baseDir, String versionName) {
        return baseDir + "/v" + versionName;
    }

    /**
     * 获取artifact版本目录
     *
     * @param baseDir     基准目录
     * @param versionName artifact版本
     * @return 路径
     */
    public static File getArtifactVersionDirFile(String baseDir, String versionName) {
        return new File(baseDir, "v" + versionName);
    }

    /**
     * 写入文件
     *
     * @param path      目标文件路径
     * @param tokenList SQLToken列表
     */
    public static void writerFile(String path, List<SQLToken> tokenList) throws MojoFailureException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (SQLToken sqlToken : tokenList) {
                if (sqlToken != null) {
                    if (sqlToken.getExecuteComment() != null) {
                        writer.write(sqlToken.getExecuteComment());
                        writer.newLine();
                    }
                    // 用newLine单行注释会多出一行也不知道为啥
                    writer.write(sqlToken.getContent() + "\n");
                }
            }
        } catch (Exception e) {
            throw new MojoFailureException("写入SQL文件失败，文件路径：" + path, e);
        }
    }
}
