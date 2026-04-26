package doctor.Controllers.DoctorDocuments;

import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentDeleteResponseDto;
import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentResponseDto;
import doctor.Services.Interfaces.DoctorDocuments.DoctorDocumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/doctors/{maBacSi}/documents")
@RequiredArgsConstructor
public class DoctorDocumentController {
    private final DoctorDocumentService doctorDocumentService;

    @GetMapping
    public ResponseEntity<List<DoctorDocumentResponseDto>> getDocuments(@PathVariable Integer maBacSi) {
        return ResponseEntity.ok(doctorDocumentService.getDocuments(maBacSi));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DoctorDocumentResponseDto> uploadDocument(
            @PathVariable Integer maBacSi,
            @RequestParam String tieuDeTaiLieu,
            @RequestPart("file") MultipartFile file) {
        DoctorDocumentResponseDto created = doctorDocumentService.uploadDocument(maBacSi, tieuDeTaiLieu, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{maTaiLieu}")
    public ResponseEntity<DoctorDocumentDeleteResponseDto> deleteDocument(
            @PathVariable Integer maBacSi, @PathVariable Integer maTaiLieu) {
        return ResponseEntity.ok(doctorDocumentService.deleteDocument(maBacSi, maTaiLieu));
    }
}

