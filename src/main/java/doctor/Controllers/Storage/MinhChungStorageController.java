package doctor.Controllers.Storage;

import doctor.Services.Business.Storage.MinhChungStorageService;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage/minh-chung")
@RequiredArgsConstructor
public class MinhChungStorageController {
    private final MinhChungStorageService minhChungStorageService;

    @GetMapping("/**")
    public ResponseEntity<Resource> getMinhChungFile(HttpServletRequest request) throws IOException {
        String requestUri = request.getRequestURI();
        String basePath = "/api/storage/minh-chung/";
        
        if (!requestUri.contains(basePath)) {
            return ResponseEntity.notFound().build();
        }
        
        int index = requestUri.indexOf(basePath);
        String relativePath = requestUri.substring(index + basePath.length());
        
        if (relativePath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String decodedRelativePath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
        Resource resource = minhChungStorageService.loadAsResource(decodedRelativePath);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache());

        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().headers(headers).contentType(mediaType).body(resource);
    }
}