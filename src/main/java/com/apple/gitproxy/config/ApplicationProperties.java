package com.apple.gitproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Properties specific to JHipster.
 * <p>
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String baseRepoDir;
    private List<String> gitRepos;

    public String getBaseRepoDir() {
        return baseRepoDir;
    }

    public void setBaseRepoDir(String baseRepoDir) {
        this.baseRepoDir = baseRepoDir;
    }

    public List<String> getGitRepos() {
        return gitRepos;
    }

    public void setGitRepos(List<String> gitRepos) {
        this.gitRepos = gitRepos;
    }
}
