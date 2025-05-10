package club.summerain0.plugin.maven.exec.artifact.config;

import club.summerain0.plugin.maven.exec.artifact.constants.ExecuteArtifactPolicyConstants;
import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 执行策略配置
 */
@Data
public class ExecuteConfig {
    /**
     * 执行策略
     *
     * @see club.summerain0.plugin.maven.exec.artifact.constants.ExecuteArtifactPolicyConstants
     */
    @Parameter(defaultValue = ExecuteArtifactPolicyConstants.PROJECT_VERSION)
    private String policy;

    /**
     * 当前项目版本
     */
    @Parameter(required = true)
    private String projectVersion;
}
