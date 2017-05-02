package com.apple.gitproxy.web.rest;

import com.apple.gitproxy.config.ApplicationProperties;
import com.apple.gitproxy.domain.Payload;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

/**
 * REST controller for managing users.
 * <p>
 * <p>This class accesses the User entity, and needs to fetch its collection of authorities.</p>
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * </p>
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>Another option would be to have a specific JPA entity graph to handle this case.</p>
 */
@RestController
@RequestMapping("/api")
public class WebHookResource {

    private static final String EOL = "\n";
    private static final int SIGNATURE_LENGTH = 45;
    private final Logger log = LoggerFactory.getLogger(WebHookResource.class);
    private final String version = "1.0";
    private CredentialsProvider credentialsProvider;
    private String baseDir;

    public WebHookResource(ApplicationProperties properties) {
        log.info("{}", properties.getGitRepos());
        credentialsProvider = new UsernamePasswordCredentialsProvider("osm_buildbot", "847ea9510d2b4edc57f51098506330efb46db1f3");
        baseDir = properties.getBaseRepoDir();
    }

    @PostMapping("/hook")
    public ResponseEntity<String> handle(@RequestBody Payload payload) throws IOException, GitAPIException {
        log.info(payload.getRepository().getHtmlUrl());
        log.info(payload.getZen());
        String fullName = payload.getRepository().getFullName();
        String httpUrl = payload.getRepository().getHtmlUrl();
        File localPath = new File(baseDir, fullName);
        if (!localPath.exists()) {
            // do git clone
            log.info("local repo doesn't exist, doing a git clone from {} to {}", httpUrl, localPath);
            Git result = null;
            try {
                result = Git.cloneRepository()
                    .setURI(httpUrl)
                    .setDirectory(localPath)
                    .setCredentialsProvider(credentialsProvider)
                    .call();
                Ref head = result.getRepository().exactRef("refs/heads/dev");
                System.out.println("Ref of refs/heads/master: " + head);
                log.info("dir from clone is {} ", result.getRepository().getDirectory());
                // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
                log.info("Having repository: {}", result.getRepository().getDirectory());
            } catch (InvalidRemoteException e) {
                e.printStackTrace();
            } catch (TransportException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            } finally {
                if (result != null) {
                    result.close();
                }
            }
        } else {
            // repo already exists, do a fetch to keep it up to date
            File gitPath = new File(localPath, ".git");
            Repository repo = new FileRepositoryBuilder()
                .setGitDir(gitPath)
                .readEnvironment()
                .findGitDir()
                .build();
            Git git = new Git(repo);
            PullResult result = null;
            try {
                result = git.pull()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
            log.info("{}", result.isSuccessful());
        }

        return ResponseEntity.ok().build();
    }

}
