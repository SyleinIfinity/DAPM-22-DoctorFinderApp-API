package doctor.Services.Business.Storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class MinhChungStorageServiceTests {

    @TempDir Path tempDir;
    private MinhChungStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService =
                new MinhChungStorageService(
                        tempDir.toString(), "http://127.0.0.1:8080", new GitStorageService());
    }

    @Test
    void uploadAndDeleteMinhChung_shouldUseLocalStoragePath() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "cmnd.pdf",
                        "application/pdf",
                        new byte[] {1, 2, 3, 4});

        MinhChungStorageService.UploadResult result =
                storageService.uploadMinhChung(12, "CCCD", 1, file);

        assertNotNull(result);
        assertTrue(
                result.getUrl().contains(
                        "/api/storage/minh-chung/B%C3%81C%20S%C4%A8%2012/Minh%20ch%E1%BB%A9ng/1%20-%20CCCD/cmnd.pdf"));
        assertTrue(Files.exists(result.getFilePath()));
        assertEquals("BÁC SĨ 12/Minh chứng/1 - CCCD/cmnd.pdf", result.getRelativePath());

        boolean deleted = storageService.deleteByUrl(result.getUrl());

        assertTrue(deleted);
        assertTrue(Files.notExists(result.getFilePath()));
    }
}
