package doctor.Models.DTOs.Schedules.Requests;

import java.time.LocalDate;

public record DoctorScheduleCalendarQueryDto(LocalDate fromDate, LocalDate toDate) {}
