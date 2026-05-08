package doctor.Services.Business.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GitStorageService {
    @Value("${app.storage.minh-chung.base-dir:/home/sylein/dapm_stogare}")
    private String storageRoot;

    @Value("${app.storage.git.repo-url:https://github.com/SyleinIfinity/DAPM-22-FinderDoctor-Storaged.git}")
    private String repoUrl;

    @Value("${app.storage.git.ssh-key-path:/home/sylein/.ssh/id_ed25519}")
    private String sshKeyPath;

    @Value("${app.storage.git.auto-commit:true}")
    private boolean autoCommit;

    @Value("${app.storage.git.auto-push:true}")
    private boolean autoPush;

    @Value("${app.storage.git.commit-author-name:DAPM-Storage}")
    private String authorName;

    @Value("${app.storage.git.commit-author-email:storage@dapm.local}")
    private String authorEmail;

    /**
     * Stage file and commit to local git repository
     */
    public void commitFile(String relativePath, String commitMessage) throws IOException {
        if (!autoCommit) {
            log.debug("Git auto-commit disabled, skipping commit for: {}", relativePath);
            return;
        }
        ensureRepoReady();

        try {
            pull();
            // Stage file
            executeGitCommand("add", relativePath);
            log.debug("Git staged file: {}", relativePath);

            // Check if there are changes to commit
            String status = executeGitCommand("status", "--porcelain");
            if (status == null || status.trim().isEmpty()) {
                log.debug("No changes to commit for: {}", relativePath);
                return;
            }

            // Commit with author info
            executeGitCommand(
                    "-c", "user.name=" + authorName,
                    "-c", "user.email=" + authorEmail,
                    "commit", "-m", commitMessage);
            log.info("Git committed: {} - Message: {}", relativePath, commitMessage);

            // Push if enabled
            if (autoPush) {
                push();
            }
        } catch (IOException ex) {
            log.error("Git commit failed for: {}", relativePath, ex);
            throw ex;
        }
    }

    /**
     * Delete file and commit removal to git
     */
    public void deleteAndCommit(String relativePath, String commitMessage) throws IOException {
        if (!autoCommit) {
            log.debug("Git auto-commit disabled, skipping delete commit for: {}", relativePath);
            return;
        }
        ensureRepoReady();

        try {
            pull();
            executeGitCommand("rm", relativePath);
            log.debug("Git removed file: {}", relativePath);

            // Commit deletion
            executeGitCommand(
                    "-c", "user.name=" + authorName,
                    "-c", "user.email=" + authorEmail,
                    "commit", "-m", commitMessage);
            log.info("Git committed deletion: {} - Message: {}", relativePath, commitMessage);

            // Push if enabled
            if (autoPush) {
                push();
            }
        } catch (IOException ex) {
            log.error("Git delete commit failed for: {}", relativePath, ex);
            throw ex;
        }
    }

    /**
     * Push changes to remote repository
     */
    public void push() throws IOException {
        ensureRepoReady();

        if (!autoPush) {
            log.debug("Git auto-push disabled");
            return;
        }

        try {
            executeGitCommand("push", "origin", "main");
            log.info("Git pushed to origin/main");
        } catch (IOException ex) {
            log.error("Git push failed", ex);
            throw ex;
        }
    }

    public void pull() throws IOException {
        ensureRepoReady();
        try {
            executeGitCommand("pull", "--rebase", "origin", "main");
            log.debug("Git pulled origin/main");
        } catch (IOException ex) {
            log.warn("Git pull failed, continue with local state", ex);
        }
    }

    /**
     * Get git status
     */
    public String getStatus() throws IOException {
        ensureRepoReady();
        return executeGitCommand("status", "--porcelain");
    }

    private void ensureRepoReady() throws IOException {
        Path root = Path.of(storageRoot).toAbsolutePath().normalize();
        Files.createDirectories(root);

        Path gitDir = root.resolve(".git");
        if (Files.exists(gitDir)) {
            return;
        }

        if (repoUrl == null || repoUrl.isBlank()) {
            throw new IOException("Missing git repo url: app.storage.git.repo-url");
        }
        executeGitCommandInParent(root, "clone", repoUrl, root.getFileName().toString());
        log.info("Cloned storage repo {} into {}", repoUrl, root);
    }

    /**
     * Execute git command in storage root directory
     */
    private String executeGitCommand(String... args) throws IOException {
        return executeGitCommandInDirectory(new File(storageRoot), args);
    }

    private String executeGitCommandInParent(Path repoDirectory, String... args) throws IOException {
        Path parent = repoDirectory.getParent();
        if (parent == null) {
            throw new IOException("Invalid storage root directory: " + repoDirectory);
        }
        return executeGitCommandInDirectory(parent.toFile(), args);
    }

    private String executeGitCommandInDirectory(File directory, String... args) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(directory);

        List<String> command = new ArrayList<>();
        command.add("git");
        for (String arg : args) {
            command.add(arg);
        }
        processBuilder.command(command);

        if (sshKeyPath != null && !sshKeyPath.isBlank()) {
            processBuilder.environment()
                    .put(
                            "GIT_SSH_COMMAND",
                            "ssh -i " + sshKeyPath + " -o IdentitiesOnly=yes -o StrictHostKeyChecking=accept-new");
        }

        // Redirect stderr to stdout for better error handling
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Wait for process to complete with timeout
        boolean completed = false;
        try {
            completed = process.waitFor() == 0;
        } catch (InterruptedException ex) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("Git command interrupted", ex);
        }

        if (!completed) {
            String errorOutput = output.toString();
            log.warn("Git command failed: git {} \n{}", String.join(" ", args), errorOutput);
            throw new IOException("Git command failed: " + errorOutput);
        }

        return output.toString().trim();
    }
}
