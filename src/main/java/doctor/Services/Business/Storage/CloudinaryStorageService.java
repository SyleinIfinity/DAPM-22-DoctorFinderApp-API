package doctor.Services.Business.Storage;

import doctor.Utils.CloudinaryFileUploadHelper;
import doctor.Utils.CloudinaryFileUploadHelper.UploadResult;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {
    public static final String AVATAR_FOLDER = CloudinaryFileUploadHelper.ROOT + "/avatar/User";

    private final CloudinaryFileUploadHelper cloudinaryFileUploadHelper;

    public UploadResult uploadImage(MultipartFile file, String folder) throws IOException {
        return cloudinaryFileUploadHelper.uploadImage(file, folder);
    }

    public CloudinaryImageResponse getImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return new CloudinaryImageResponse(false, "publicId is required", null, null);
        }
        if (!cloudinaryFileUploadHelper.imageExists(publicId)) {
            return new CloudinaryImageResponse(false, "Image not found", publicId, null);
        }
        return new CloudinaryImageResponse(
                true, "Image found", publicId, cloudinaryFileUploadHelper.buildSecureUrl(publicId));
    }

    public CloudinaryDeleteResponse deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            return new CloudinaryDeleteResponse(false, "publicId is required", null, false);
        }
        boolean deleted = cloudinaryFileUploadHelper.deleteImage(publicId);
        return new CloudinaryDeleteResponse(
                deleted,
                deleted ? "Image deleted successfully" : "Image not found or already deleted",
                publicId,
                deleted);
    }

    public record CloudinaryImageResponse(
            boolean success, String message, String publicId, String url) {}

    public record CloudinaryDeleteResponse(
            boolean success, String message, String publicId, boolean deleted) {}
}
