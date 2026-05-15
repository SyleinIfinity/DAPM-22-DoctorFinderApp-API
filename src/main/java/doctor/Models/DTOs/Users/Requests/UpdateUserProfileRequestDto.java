package doctor.Models.DTOs.Users.Requests;

public record UpdateUserProfileRequestDto(
        String hoLot, String ten, String soDienThoai, String email, String cccd) {}
