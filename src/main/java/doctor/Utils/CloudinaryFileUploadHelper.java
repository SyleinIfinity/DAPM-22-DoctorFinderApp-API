package doctor.Utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CloudinaryFileUploadHelper {
    public static final String ROOT = "DAPM";
    public static final String AVATAR_USER_FOLDER = ROOT + "/avatar/User";
    public static final String AVATAR_DOCTOR_FOLDER = ROOT + "/avatar/Doctor";
    public static final String CHAT_FOLDER = ROOT + "/chat";
    public static final String MINH_CHUNG_FOLDER = ROOT + "/MinhChung";

    private final Cloudinary cloudinary;

    public CloudinaryFileUploadHelper(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        if (cloudName == null || cloudName.isBlank()) {
            throw new IllegalStateException("Missing Cloudinary config: cloudinary.cloud-name");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing Cloudinary config: cloudinary.api-key");
        }
        if (apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("Missing Cloudinary config: cloudinary.api-secret");
        }

        this.cloudinary =
                new Cloudinary(
                        ObjectUtils.asMap(
                                "cloud_name", cloudName,
                                "api_key", apiKey,
                                "api_secret", apiSecret,
                                "secure", true));
    }

    public UploadResult uploadUserAvatar(MultipartFile file) throws IOException {
        return uploadImage(file, AVATAR_USER_FOLDER);
    }

    public UploadResult uploadDoctorAvatar(MultipartFile file) throws IOException {
        return uploadImage(file, AVATAR_DOCTOR_FOLDER);
    }

    public UploadResult uploadChatImage(MultipartFile file) throws IOException {
        return uploadImage(file, CHAT_FOLDER);
    }

    public UploadResult uploadMinhChung(MultipartFile file) throws IOException {
        return uploadImage(file, MINH_CHUNG_FOLDER);
    }

    public UploadResult uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String publicId = UUID.randomUUID().toString().replace("-", "");
        Map<?, ?> uploadResult =
                cloudinary
                        .uploader()
                        .upload(
                                file.getBytes(),
                                ObjectUtils.asMap(
                                        "folder", folder,
                                        "public_id", publicId,
                                        "overwrite", true,
                                        "resource_type", "image"));

        String secureUrl = valueAsString(uploadResult.get("secure_url"));
        String returnedPublicId = valueAsString(uploadResult.get("public_id"));
        return new UploadResult(secureUrl, returnedPublicId);
    }

    public boolean deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }

        Map<?, ?> result =
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        String status = valueAsString(result.get("result"));
        return "ok".equalsIgnoreCase(status);
    }

    private static String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    @Getter
    public static class UploadResult {
        private final String url;
        private final String publicId;

        public UploadResult(String url, String publicId) {
            this.url = url;
            this.publicId = publicId;
        }
    }
}

