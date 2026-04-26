package doctor.Services.Interfaces.Messaging;

import doctor.Models.DTOs.Messaging.Requests.CreateConversationRequestDto;
import doctor.Models.DTOs.Messaging.Requests.SendMessageRequestDto;
import doctor.Models.DTOs.Messaging.Responses.ConversationSummaryResponseDto;
import doctor.Models.DTOs.Messaging.Responses.MessageResponseDto;
import java.time.LocalDateTime;
import java.util.List;

public interface MessagingService {
    List<ConversationSummaryResponseDto> getConversations(Integer maNguoiDung, Integer maBacSi);

    ConversationSummaryResponseDto createOrGetConversation(CreateConversationRequestDto request);

    List<MessageResponseDto> getMessages(Integer maCuocHoiThoai, Integer limit, LocalDateTime before);

    MessageResponseDto sendMessage(Integer maCuocHoiThoai, SendMessageRequestDto request);
}

