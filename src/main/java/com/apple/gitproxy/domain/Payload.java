package com.apple.gitproxy.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by yiqing_jin on 5/2/17.
 */
public class Payload {

    private String zen;
    private Repository repository;

    public String getZen() {
        return zen;
    }

    public void setZen(String zen) {
        this.zen = zen;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public class Repository {
        @JsonProperty("html_url")
        private String htmlUrl;
        @JsonProperty("full_name")
        private String fullName;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }
    }
}
