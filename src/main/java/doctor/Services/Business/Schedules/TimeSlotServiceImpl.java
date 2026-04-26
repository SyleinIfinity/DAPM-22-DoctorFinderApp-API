package doctor.Services.Business.Schedules;

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
}

