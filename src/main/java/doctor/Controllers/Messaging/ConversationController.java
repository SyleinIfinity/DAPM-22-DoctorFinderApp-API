package doctor.Controllers.Messaging;

import doctor.Models.DTOs.Messaging.Requests.CreateConversationRequestDto;
import doctor.Models.DTOs.Messaging.Requests.SendMessageRequestDto;
import doctor.Models.DTOs.Messaging.Responses.ConversationSummaryResponseDto;
import doctor.Models.DTOs.Messaging.Responses.MessageResponseDto;
import doctor.Services.Interfaces.Messaging.MessagingService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final MessagingService messagingService;

    @GetMapping
    public ResponseEntity<List<ConversationSummaryResponseDto>> getConversations(
            @RequestParam(required = false) Integer maNguoiDung,
            @RequestParam(required = false) Integer maBacSi) {
        return ResponseEntity.ok(messagingService.getConversations(maNguoiDung, maBacSi));
    }

    @PostMapping
    public ResponseEntity<ConversationSummaryResponseDto> createOrGetConversation(
            @RequestBody CreateConversationRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        ConversationSummaryResponseDto result = messagingService.createOrGetConversation(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{maCuocHoiThoai}/messages")
    public ResponseEntity<List<MessageResponseDto>> getMessages(
            @PathVariable Integer maCuocHoiThoai,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime before) {
        return ResponseEntity.ok(messagingService.getMessages(maCuocHoiThoai, limit, before));
    }

    @PostMapping("/{maCuocHoiThoai}/messages")
    public ResponseEntity<MessageResponseDto> sendMessage(
            @PathVariable Integer maCuocHoiThoai, @RequestBody SendMessageRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        MessageResponseDto created = messagingService.sendMessage(maCuocHoiThoai, request);
        return ResponseEntity.ok(created);
    }
}
