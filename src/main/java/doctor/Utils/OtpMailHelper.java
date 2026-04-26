package doctor.Utils;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class OtpMailHelper {
    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String displayName;

    public OtpMailHelper(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromEmail,
            @Value("${app.mail.display-name}") String displayName) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.displayName = displayName;
    }

    public void sendOtpToGmail(String toEmail, String otpCode) {
        sendOtpToGmail(toEmail, otpCode, "REGISTER", Duration.ofMinutes(5));
    }

    public void sendOtpToGmail(
            String toEmail, String otpCode, String purpose, Duration validDuration) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("toEmail is required");
        }
        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalArgumentException("otpCode is required");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("purpose is required");
        }
        if (validDuration == null || validDuration.isZero() || validDuration.isNegative()) {
            throw new IllegalArgumentException("validDuration must be positive");
        }

        String normalizedPurpose = purpose.trim().toUpperCase();
        long validMinutes = Math.max(1L, validDuration.toMinutes());

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail, displayName);
            helper.setTo(toEmail.trim());
            helper.setSubject("[WEBPC] Ma OTP " + normalizedPurpose);
            helper.setText(buildOtpHtml(otpCode.trim(), normalizedPurpose, validMinutes), true);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot build OTP email message", e);
        }

        mailSender.send(message);
    }

    private String buildOtpHtml(String otpCode, String purpose, long validMinutes) {
        return "<div style=\"font-family:Arial,sans-serif;font-size:14px;line-height:1.6\">"
                + "<p>Xin chao,</p>"
                + "<p>Yeu cau: <strong>"
                + purpose
                + "</strong></p>"
                + "<p>Ma OTP cua ban la:</p>"
                + "<p style=\"font-size:28px;font-weight:bold;letter-spacing:4px;margin:12px 0\">"
                + otpCode
                + "</p>"
                + "<p>Ma co hieu luc trong <strong>"
                + validMinutes
                + " phut</strong>. Khong chia se ma nay voi bat ky ai.</p>"
                + "<p>Tran trong,<br/>WEBPC Support</p>"
                + "</div>";
    }
}
