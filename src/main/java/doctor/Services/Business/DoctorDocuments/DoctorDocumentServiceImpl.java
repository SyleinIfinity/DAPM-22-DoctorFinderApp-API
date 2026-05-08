package doctor.Services.Business.DoctorDocuments;

import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentDeleteResponseDto;
import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.TaiLieuBacSi;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.TaiLieuBacSiRepository;
import doctor.Services.Business.Storage.MinhChungStorageService;
import doctor.Services.Interfaces.DoctorDocuments.DoctorDocumentService;
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
    private final MinhChungStorageService minhChungStorageService;

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

        Integer documentIndex = taiLieuBacSiRepository.findByMaBacSi(normalizedMaBacSi).size() + 1;

        MinhChungStorageService.UploadResult upload;
        try {
            upload = minhChungStorageService.uploadMinhChung(
                    normalizedMaBacSi, normalizedTieuDe, documentIndex, file);
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
    public DoctorDocumentResponseDto updateDocument(Integer maBacSi, Integer maTaiLieu, String tieuDeTaiLieu, MultipartFile file) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        Integer normalizedMaTaiLieu = normalizePositiveId(maTaiLieu, "maTaiLieu");
        String normalizedTieuDe = requireNotBlank(tieuDeTaiLieu, "tieuDeTaiLieu");
        if (normalizedTieuDe.length() > 100) {
            throw new IllegalArgumentException("tieuDeTaiLieu toi da 100 ky tu");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        
        requireBacSi(normalizedMaBacSi);

        TaiLieuBacSi existing = taiLieuBacSiRepository
                .selectById(normalizedMaTaiLieu)
                .orElseThrow(() -> new IllegalArgumentException("Tai lieu khong ton tai"));

        if (!normalizedMaBacSi.equals(existing.getMaBacSi())) {
            throw new IllegalArgumentException("Tai lieu khong thuoc bac si nay");
        }

        // Delete old file
        if (existing.getDuongDanFileUrl() != null && !existing.getDuongDanFileUrl().isBlank()) {
            try {
                minhChungStorageService.deleteByUrl(existing.getDuongDanFileUrl());
            } catch (IOException ignored) {
                // Continue with update even if deletion fails
            }
        }

        // Upload new file
        Integer documentIndex = resolveDocumentIndex(normalizedMaBacSi, normalizedMaTaiLieu);
        MinhChungStorageService.UploadResult upload;
        try {
            upload = minhChungStorageService.uploadMinhChung(
                    normalizedMaBacSi, normalizedTieuDe, documentIndex, file);
        } catch (IOException ex) {
            throw new IllegalStateException("Khong the cap nhat minh chung", ex);
        }
        if (upload == null || upload.getUrl() == null || upload.getUrl().isBlank()) {
            throw new IllegalStateException("Khong the cap nhat minh chung");
        }

        // Update entity
        existing.setTieuDeTaiLieu(normalizedTieuDe);
        existing.setDuongDanFileUrl(upload.getUrl());

        TaiLieuBacSi updated = taiLieuBacSiRepository.update(existing);
        return mapToResponse(updated);
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
        if (existing.getDuongDanFileUrl() != null && !existing.getDuongDanFileUrl().isBlank()) {
            try {
                deletedFromStorage = minhChungStorageService.deleteByUrl(existing.getDuongDanFileUrl());
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

    private Integer resolveDocumentIndex(Integer maBacSi, Integer maTaiLieu) {
        List<TaiLieuBacSi> documents = taiLieuBacSiRepository.findByMaBacSi(maBacSi);
        for (int index = 0; index < documents.size(); index++) {
            TaiLieuBacSi document = documents.get(index);
            if (document != null && maTaiLieu.equals(document.getMaTaiLieu())) {
                return index + 1;
            }
        }
        return documents.size() + 1;
    }
}
