package doctor.Services.Interfaces.DoctorDocuments;

import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentDeleteResponseDto;
import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface DoctorDocumentService {
    DoctorDocumentResponseDto uploadDocument(Integer maBacSi, String tieuDeTaiLieu, MultipartFile file);

    List<DoctorDocumentResponseDto> getDocuments(Integer maBacSi);

    DoctorDocumentResponseDto updateDocument(Integer maBacSi, Integer maTaiLieu, String tieuDeTaiLieu, MultipartFile file);

    DoctorDocumentDeleteResponseDto deleteDocument(Integer maBacSi, Integer maTaiLieu);
}

