package doctor.Services.Interfaces.Admin;

import doctor.Models.DTOs.Admin.Requests.UpdateAccountRoleRequestDto;
import doctor.Models.DTOs.Admin.Responses.AdminAccountActionResponseDto;
import doctor.Models.DTOs.Admin.Responses.AdminAccountResponseDto;
import java.util.List;

public interface AdminAccountService {
    List<AdminAccountResponseDto> getAccounts();

    AdminAccountActionResponseDto lockAccount(Integer maTaiKhoan);

    AdminAccountActionResponseDto unlockAccount(Integer maTaiKhoan);

    AdminAccountActionResponseDto updateRole(Integer maTaiKhoan, UpdateAccountRoleRequestDto request);
}

