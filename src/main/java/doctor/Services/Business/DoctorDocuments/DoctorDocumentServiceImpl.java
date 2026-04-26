package doctor.Services.Business.DoctorDocuments;

import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentDeleteResponseDto;
import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.TaiLieuBacSi;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.TaiLieuBacSiRepository;
import doctor.Services.Interfaces.DoctorDocuments.DoctorDocumentService;
import doctor.Utils.CloudinaryFileUploadHelper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DoctorDocumentServiceImpl implements DoctorDocumentService {
    private final BacSiRepository bacSiRepository;
    private final TaiLieuBacSiRepository taiLieuBacSiRepository;
    private final CloudinaryFileUploadHelper cloudinaryFileUploadHelper;

    @Override
    @Transactional
    public DoctorDocumentResponseDto uploadDocument(
            Integer maBacSi, String tieuDeTaiLieu, MultipartFile file) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        String normalizedTieuDe = requireNotBlank(tieuDeTaiLieu, "tieuDeTaiLieu");
        if (normalizedTieuDe.length() > 100) {
            throw new IllegalArgumentException("tieuDeTaiLieu toi da 100 ky tu");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        requireBacSi(normalizedMaBacSi);

        CloudinaryFileUploadHelper.UploadResult upload;
        try {
            upload = cloudinaryFileUploadHelper.uploadMinhChung(file);
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the upload minh chung", ex);
        }
        if (upload == null || upload.getUrl() == null || upload.getUrl().isBlank()) {
            throw new IllegalStateException("Khong the upload minh chung");
        }

        TaiLieuBacSi entity = new TaiLieuBacSi();
        entity.setMaBacSi(normalizedMaBacSi);
        entity.setTieuDeTaiLieu(normalizedTieuDe);
        entity.setDuongDanFileUrl(upload.getUrl());

        TaiLieuBacSi created = taiLieuBacSiRepository.insert(entity);
        return mapToResponse(created);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDocumentResponseDto> getDocuments(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);
        return taiLieuBacSiRepository.findByMaBacSi(normalizedMaBacSi).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoctorDocumentDeleteResponseDto deleteDocument(Integer maBacSi, Integer maTaiLieu) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        Integer normalizedMaTaiLieu = normalizePositiveId(maTaiLieu, "maTaiLieu");
        requireBacSi(normalizedMaBacSi);

        TaiLieuBacSi existing =
                taiLieuBacSiRepository
                        .selectById(normalizedMaTaiLieu)
                        .orElseThrow(() -> new IllegalArgumentException("Tai lieu khong ton tai"));

        if (!normalizedMaBacSi.equals(existing.getMaBacSi())) {
            throw new IllegalArgumentException("Tai lieu khong thuoc bac si nay");
        }

        taiLieuBacSiRepository.deleteById(normalizedMaTaiLieu);

        boolean deletedFromStorage = false;
        String publicId = tryExtractCloudinaryPublicId(existing.getDuongDanFileUrl());
        if (publicId != null) {
            try {
                deletedFromStorage = cloudinaryFileUploadHelper.deleteImage(publicId);
            } catch (IOException ignored) {
                deletedFromStorage = false;
            }
        }

        return new DoctorDocumentDeleteResponseDto(
                true,
                deletedFromStorage
                        ? "Xoa tai lieu thanh cong"
                        : "Xoa tai lieu thanh cong (khong the xoa file tren storage)",
                normalizedMaTaiLieu,
                deletedFromStorage);
    }

    private DoctorDocumentResponseDto mapToResponse(TaiLieuBacSi entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity is required");
        }
        return new DoctorDocumentResponseDto(
                entity.getMaTaiLieu(),
                entity.getMaBacSi(),
                entity.getTieuDeTaiLieu(),
                entity.getDuongDanFileUrl());
    }

    private BacSi requireBacSi(Integer maBacSi) {
        return bacSiRepository
                .selectById(maBacSi)
                .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));
    }

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String tryExtractCloudinaryPublicId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String withoutQuery = url.split("\\?")[0];
        int uploadIndex = withoutQuery.indexOf("/upload/");
        if (uploadIndex < 0) {
            return null;
        }

        String path = withoutQuery.substring(uploadIndex + "/upload/".length());
        if (path.isBlank()) {
            return null;
        }

        String[] segments = path.split("/");
        int startIdx = 0;
        for (int i = 0; i < segments.length; i++) {
            if (segments[i] != null && segments[i].matches("v\\d+")) {
                startIdx = i + 1;
                break;
            }
        }

        if (startIdx >= segments.length) {
            return null;
        }

        StringBuilder joined = new StringBuilder();
        for (int i = startIdx; i < segments.length; i++) {
            String seg = segments[i];
            if (seg == null || seg.isBlank()) {
                continue;
            }
            if (joined.length() > 0) {
                joined.append("/");
            }
            joined.append(seg);
        }

        String withExt = joined.toString();
        if (withExt.isBlank()) {
            return null;
        }

        int lastDot = withExt.lastIndexOf('.');
        return lastDot > 0 ? withExt.substring(0, lastDot) : withExt;
    }
}

