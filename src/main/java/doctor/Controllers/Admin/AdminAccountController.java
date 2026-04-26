package doctor.Controllers.Admin;

import doctor.Models.DTOs.Admin.Requests.UpdateAccountRoleRequestDto;
import doctor.Models.DTOs.Admin.Responses.AdminAccountActionResponseDto;
import doctor.Models.DTOs.Admin.Responses.AdminAccountResponseDto;
import doctor.Services.Interfaces.Admin.AdminAccountService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {
    private final AdminAccountService adminAccountService;

    @GetMapping
    public ResponseEntity<List<AdminAccountResponseDto>> getAccounts() {
        return ResponseEntity.ok(adminAccountService.getAccounts());
    }

    @PatchMapping("/{maTaiKhoan}/lock")
    public ResponseEntity<AdminAccountActionResponseDto> lockAccount(@PathVariable Integer maTaiKhoan) {
        return ResponseEntity.ok(adminAccountService.lockAccount(maTaiKhoan));
    }

    @PatchMapping("/{maTaiKhoan}/unlock")
    public ResponseEntity<AdminAccountActionResponseDto> unlockAccount(@PathVariable Integer maTaiKhoan) {
        return ResponseEntity.ok(adminAccountService.unlockAccount(maTaiKhoan));
    }

    @PatchMapping("/{maTaiKhoan}/role")
    public ResponseEntity<AdminAccountActionResponseDto> updateRole(
            @PathVariable Integer maTaiKhoan, @RequestBody UpdateAccountRoleRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        return ResponseEntity.ok(adminAccountService.updateRole(maTaiKhoan, request));
    }
}

