package doctor.Controllers.Storage;

import doctor.Services.Business.Storage.CloudinaryStorageService;
import doctor.Services.Business.Storage.CloudinaryStorageService.CloudinaryDeleteResponse;
import doctor.Services.Business.Storage.CloudinaryStorageService.CloudinaryImageResponse;
import doctor.Utils.CloudinaryFileUploadHelper.UploadResult;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/storage/cloudinary")
@RequiredArgsConstructor
public class CloudinaryStorageController {
    private final CloudinaryStorageService cloudinaryStorageService;

    @GetMapping
    public ResponseEntity<CloudinaryImageResponse> getImageUrl(@RequestParam("publicId") String publicId) {
        return ResponseEntity.ok(cloudinaryStorageService.getImage(publicId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResult> uploadImage(@RequestPart("file") MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UploadResult result = cloudinaryStorageService.uploadImage(file, CloudinaryStorageService.AVATAR_FOLDER);
        if (result == null || result.getUrl() == null || result.getUrl().isBlank()) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    public ResponseEntity<CloudinaryDeleteResponse> deleteImage(@RequestParam("publicId") String publicId)
            throws IOException {
        return ResponseEntity.ok(cloudinaryStorageService.deleteImage(publicId));
    }
}
