package doctor.Services.Business.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class MinhChungStorageService {
    private static final String STORAGE_PREFIX = "/api/storage/minh-chung/";
    private static final String DOCTOR_PREFIX = "BÁC SĨ ";
    private static final String MINH_CHUNG_FOLDER = "Minh chứng";

    private final Path storageRoot;
    private final String publicBaseUrl;
    private final GitStorageService gitStorageService;

    public MinhChungStorageService(
            @Value("${app.storage.minh-chung.base-dir:/home/sylein/dapm_stogare}") String baseDir,
            @Value("${app.storage.public-base-url:http://127.0.0.1:8080}") String publicBaseUrl,
            GitStorageService gitStorageService) {
        if (baseDir == null || baseDir.isBlank()) {
            throw new IllegalStateException("Missing storage config: app.storage.minh-chung.base-dir");
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalStateException("Missing storage config: app.storage.public-base-url");
        }
        if (gitStorageService == null) {
            throw new IllegalStateException("Missing git storage service");
        }

        this.storageRoot = Paths.get(baseDir).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.gitStorageService = gitStorageService;
    }

    public UploadResult uploadMinhChung(
            Integer maBacSi, String tieuDeTaiLieu, Integer documentIndex, MultipartFile file)
            throws IOException {
        if (maBacSi == null || maBacSi <= 0) {
            throw new IllegalArgumentException("maBacSi is required");
        }
        if (documentIndex == null || documentIndex <= 0) {
            throw new IllegalArgumentException("documentIndex is required");
        }
        if (file == null || file.isEmpty()) {
            return null;
        }

        String doctorFolderName = DOCTOR_PREFIX + maBacSi;
        String titleFolderName = documentIndex + " - " + sanitizeFolderName(tieuDeTaiLieu);
        Path targetDirectory =
                storageRoot.resolve(doctorFolderName).resolve(MINH_CHUNG_FOLDER).resolve(titleFolderName);
        Files.createDirectories(targetDirectory);

        String fileName = sanitizeFileName(resolveFileName(file));
        Path destination = targetDirectory.resolve(fileName).normalize();
        ensureWithinStorageRoot(destination);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = storageRoot.relativize(destination).toString().replace('\\', '/');
        String url = publicBaseUrl + STORAGE_PREFIX + encodePath(relativePath);

        // Commit to git
        try {
            String commitMessage = String.format("Add document: %s (%s)", tieuDeTaiLieu, fileName);
            gitStorageService.commitFile(relativePath, commitMessage);
            log.info("Document uploaded and committed: {}", relativePath);
        } catch (IOException ex) {
            log.warn("Failed to commit file to git: {}", relativePath, ex);
        }

        return new UploadResult(url, relativePath, destination);
    }

    public boolean deleteByUrl(String url) throws IOException {
        String relativePath = extractRelativePath(url);
        if (relativePath == null) {
            return false;
        }

        Path filePath = storageRoot.resolve(relativePath).normalize();
        ensureWithinStorageRoot(filePath);
        if (!Files.exists(filePath)) {
            return false;
        }

        Files.delete(filePath);
        cleanupEmptyParents(filePath.getParent());

        // Commit deletion to git
        try {
            gitStorageService.deleteAndCommit(relativePath, "Delete document: " + relativePath);
            log.info("Document deleted and committed: {}", relativePath);
        } catch (IOException ex) {
            log.warn("Failed to commit deletion to git: {}", relativePath, ex);
        }

        return true;
    }

    public Resource loadAsResource(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }

        Path filePath = storageRoot.resolve(relativePath).normalize();
        ensureWithinStorageRoot(filePath);

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return null;
        }
        return resource;
    }

    public String extractRelativePath(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String normalized = url.trim();
        int prefixIndex = normalized.indexOf(STORAGE_PREFIX);
        if (prefixIndex < 0) {
            return null;
        }

        String relativePath = normalized.substring(prefixIndex + STORAGE_PREFIX.length());
        if (relativePath.isBlank()) {
            return null;
        }
        return decodePath(relativePath);
    }

    private String resolveFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename.trim();
        }
        return UUID.randomUUID().toString().replace("-", "") + resolveExtension(null, file.getContentType());
    }

    private String sanitizeFolderName(String value) {
        String normalized = normalizeName(value, "Khong ro");
        return normalized.replace('/', '_').replace('\\', '_').replace(':', '_');
    }

    private String sanitizeFileName(String value) {
        return normalizeName(value, "file").replace('/', '_').replace('\\', '_').replace(':', '_');
    }

    private String normalizeName(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        return value.trim();
    }

    private String resolveExtension(String originalFilename, String contentType) {
        String fileName = originalFilename == null ? null : originalFilename.trim();
        if (StringUtils.hasText(fileName)) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0 && lastDot < fileName.length() - 1 && lastDot >= fileName.length() - 6) {
                return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
            }
        }

        if (contentType == null || contentType.isBlank()) {
            return "";
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    private void cleanupEmptyParents(Path directory) throws IOException {
        Path current = directory;
        while (current != null && !current.equals(storageRoot)) {
            if (!Files.isDirectory(current)) {
                current = current.getParent();
                continue;
            }

            try (var stream = Files.list(current)) {
                if (stream.findAny().isPresent()) {
                    break;
                }
            }

            Files.deleteIfExists(current);
            current = current.getParent();
        }
    }

    private void ensureWithinStorageRoot(Path path) {
        if (!path.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
    }

    private String encodePath(String relativePath) {
        String[] segments = relativePath.split("/");
        StringBuilder encoded = new StringBuilder();
        for (String segment : segments) {
            if (Objects.isNull(segment) || segment.isBlank()) {
                continue;
            }
            if (encoded.length() > 0) {
                encoded.append('/');
            }
            encoded.append(URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return encoded.toString();
    }

    private String decodePath(String relativePath) {
        String[] segments = relativePath.split("/");
        StringBuilder decoded = new StringBuilder();
        for (String segment : segments) {
            if (Objects.isNull(segment) || segment.isBlank()) {
                continue;
            }
            if (decoded.length() > 0) {
                decoded.append('/');
            }
            decoded.append(URLDecoder.decode(segment, StandardCharsets.UTF_8));
        }
        return decoded.toString();
    }

    private String trimTrailingSlash(String value) {
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    @Getter
    public static class UploadResult {
        private final String url;
        private final String relativePath;
        private final Path filePath;

        public UploadResult(String url, String relativePath, Path filePath) {
            this.url = url;
            this.relativePath = relativePath;
            this.filePath = filePath;
        }
    }
}
