package club.summerain0.plugin.maven.exec.artifact.mojo;

import club.summerain0.plugin.maven.exec.artifact.bean.SQLToken;
import club.summerain0.plugin.maven.exec.artifact.config.ExecuteConfig;
import club.summerain0.plugin.maven.exec.artifact.config.MySQLConfig;
import club.summerain0.plugin.maven.exec.artifact.constants.DBType;
import club.summerain0.plugin.maven.exec.artifact.constants.ExecuteArtifactPolicyConstants;
import club.summerain0.plugin.maven.exec.artifact.constants.SQLTokenType;
import club.summerain0.plugin.maven.exec.artifact.parser.SQLParser;
import club.summerain0.plugin.maven.exec.artifact.support.MySQLSupport;
import club.summerain0.plugin.maven.exec.artifact.util.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 执行交付物
 */
@Mojo(name = "execute")
public class ExecuteArtifactMojo extends AbstractMojo {
    private static final String EXECUTED_COMMIT = "/*语句已执行*/";
    private static final String EXECUTED_FAILURE_COMMIT = "/*执行失败\n%s\n*/";

    /**
     * 交付物基准目录
     */
    @Parameter
    private String baseDir;

    /**
     * 执行交付物配置
     */
    @Parameter
    private ExecuteConfig executeConfig;

    /**
     * MySQL配置
     */
    @Parameter
    private MySQLConfig mySQLConfig;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("================= 开始执行交付物 =================");
        getLog().info(">> 交付物执行基础位置：" + baseDir);
        String executePolicy = executeConfig.getPolicy();
        getLog().info("交付物执行策略为" + executePolicy);

        switch (executePolicy) {
            case ExecuteArtifactPolicyConstants.PROJECT_VERSION:
                executeVersionArtifact(executeConfig.getProjectVersion());
                break;

            default:
                getLog().error("交付物执行策略不存在");
                break;
        }

        getLog().info("================= 执行交付物结束 =================");
    }

    /**
     * 执行版本交付物
     *
     * @param version 版本号
     */
    private void executeVersionArtifact(String version) throws MojoExecutionException, MojoFailureException {
        getLog().info(String.format("-- 开始处理版本v%s下的交付物", version));
        File versionDir = FileUtils.getArtifactVersionDirFile(baseDir, version);
        if (!versionDir.exists()) {
            getLog().error("交付物版本目录不存在");
            return;
        }

        File[] files = versionDir.listFiles();
        if (files == null) {
            getLog().error("交付物版本目录为空");
            return;
        }

        for (File file : files) {
            if (file.isFile()) continue;
            String dirName = file.getName();

            if (Objects.equals(dirName, DBType.SQL.getDirName())) {
                executeSQLArtifact(file);
            } else {
                getLog().warn(String.format("-- 未知交付物类型%s", dirName));
            }
        }
    }

    /**
     * 执行SQL交付物
     *
     * @param file SQL交付物目录
     */
    private void executeSQLArtifact(File file) throws MojoExecutionException, MojoFailureException {
        MySQLSupport mySQLSupport;
        try {
            mySQLSupport = MySQLSupport.getConnection(
                    mySQLConfig.getUrl(),
                    mySQLConfig.getUsername(),
                    mySQLConfig.getPassword()
            );
        } catch (Exception e) {
            throw new MojoFailureException("获取MySQL连接失败", e);
        }

        File[] files = file.listFiles();
        if (files == null) return;

        // 排序，含有ddl的放在最前面
        List<File> fileList = Arrays.stream(files).sorted((o1, o2) -> {
            boolean ddl1 = o1.getName().toLowerCase().contains("ddl");
            boolean ddl2 = o2.getName().toLowerCase().contains("ddl");
            if (ddl1 == ddl2) return 0;
            return ddl1 ? -1 : 1;
        }).toList();

        // 遍历执行SQL交付物
        for (File sqlFile : fileList) {
            if (!sqlFile.isFile()) return;

            getLog().info(String.format("-- 开始处理%s", sqlFile.getAbsolutePath()));

            // 读取SQL文件
            String fileContent;
            try {
                fileContent = Files.readString(sqlFile.toPath());
            } catch (Exception e) {
                throw new MojoFailureException("读取SQL文件失败，文件路径：" + sqlFile.getAbsolutePath(), e);
            }

            // 解析SQL文件
            List<SQLToken> sqlTokenList;
            try {
                sqlTokenList = SQLParser.parseSQL(fileContent);
            } catch (Exception e) {
                throw new MojoFailureException(e);
            }

            // 执行SQL
            if (sqlTokenList.isEmpty()) return;
            boolean skipExecute = false; // 标记是否需要跳过下一条语句的执行
            for (SQLToken sqlToken : sqlTokenList) {
                if (!skipExecute) { // 当前不用跳过
                    // 如果是注释，并且内容是已执行标识，则跳过执行下一条SQL
                    if (Objects.equals(sqlToken.getType(), SQLTokenType.COMMENT) && Objects.equals(sqlToken.getContent(), EXECUTED_COMMIT)) {
                        skipExecute = true;
                        continue;
                    }

                    // 如果遇到语句，则执行
                    if (Objects.equals(sqlToken.getType(), SQLTokenType.SQL)) {
                        try {
                            mySQLSupport.execute(sqlToken.getContent());
                            sqlToken.setExecuteComment(EXECUTED_COMMIT);
                        } catch (SQLException e) {
                            sqlToken.setExecuteComment(
                                    String.format(EXECUTED_FAILURE_COMMIT, e.getMessage())
                            );
                            break;
                        }
                    }
                } else { // 当前需要跳过
                    if (Objects.equals(sqlToken.getType(), SQLTokenType.SQL)) {
                        skipExecute = false;
                        continue;
                    }
                }
            }

            // 输出文件
            FileUtils.writerFile(sqlFile.getAbsolutePath(), sqlTokenList);

            getLog().info(String.format("-- 结束处理%s", sqlFile.getAbsolutePath()));
        }

        // 关闭链接
        mySQLSupport.close();
    }
}
