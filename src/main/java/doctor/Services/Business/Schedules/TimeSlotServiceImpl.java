package doctor.Services.Business.Schedules;

import doctor.Models.DTOs.Schedules.Requests.CreateTimeSlotRequestDto;
import doctor.Models.DTOs.Schedules.Responses.TimeSlotResponseDto;
import doctor.Models.Entities.KhungGio;
import doctor.Repositories.Interfaces.KhungGioRepository;
import doctor.Services.Interfaces.Schedules.TimeSlotService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {
    private final KhungGioRepository khungGioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDto> getAllTimeSlots() {
        return khungGioRepository.selectAll().stream()
                .sorted(Comparator.comparing(KhungGio::getThoiLuongPhut))
                .map(khungGio -> new TimeSlotResponseDto(khungGio.getMaKhungGio(), khungGio.getThoiLuongPhut()))
                .toList();
    }

    @Override
    @Transactional
    public TimeSlotResponseDto createTimeSlot(CreateTimeSlotRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.thoiLuongPhut() == null) {
            throw new IllegalArgumentException("thoiLuongPhut is required");
        }
        if (request.thoiLuongPhut() <= 0) {
            throw new IllegalArgumentException("thoiLuongPhut phai > 0");
        }

        khungGioRepository.findByThoiLuongPhut(request.thoiLuongPhut()).ifPresent(existing -> {
            throw new IllegalArgumentException("thoiLuongPhut da ton tai");
        });

        KhungGio created = khungGioRepository.insert(new KhungGio(null, request.thoiLuongPhut()));
        return new TimeSlotResponseDto(created.getMaKhungGio(), created.getThoiLuongPhut());
    }
}

