package doctor.Security;

import doctor.Security.Exceptions.TooManyRequestsException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptService {
    private final ConcurrentMap<String, AttemptState> states = new ConcurrentHashMap<>();

    @Value("${app.security.login.max-failures:5}")
    private int maxFailures;

    @Value("${app.security.login.window-seconds:900}")
    private long windowSeconds;

    @Value("${app.security.login.block-seconds:900}")
    private long blockSeconds;

    public void assertAllowed(String username, String clientIp) {
        String key = buildKey(username, clientIp);
        AttemptState state = states.get(key);
        if (state == null) {
            return;
        }

        long now = Instant.now().getEpochSecond();
        if (state.blockedUntilEpochSeconds > now) {
            throw new TooManyRequestsException(
                    "Dang nhap qua nhieu lan that bai. Vui long thu lai sau.",
                    state.blockedUntilEpochSeconds - now);
        }

        if (state.blockedUntilEpochSeconds > 0 && state.blockedUntilEpochSeconds <= now) {
            states.remove(key);
        }
    }

    public void recordFailure(String username, String clientIp) {
        String key = buildKey(username, clientIp);
        long now = Instant.now().getEpochSecond();

        AttemptState updated =
                states.compute(
                        key,
                        (unused, existing) -> {
                            AttemptState state = existing;
                            if (state == null
                                    || state.windowStartedEpochSeconds <= 0
                                    || now - state.windowStartedEpochSeconds > windowSeconds) {
                                state = new AttemptState();
                                state.windowStartedEpochSeconds = now;
                                state.failureCount = 0;
                                state.blockedUntilEpochSeconds = 0;
                            }

                            if (state.blockedUntilEpochSeconds > now) {
                                return state;
                            }

                            state.failureCount += 1;
                            if (state.failureCount >= maxFailures) {
                                state.blockedUntilEpochSeconds = now + blockSeconds;
                            }
                            return state;
                        });

        if (updated != null && updated.blockedUntilEpochSeconds > now) {
            throw new TooManyRequestsException(
                    "Dang nhap qua nhieu lan that bai. Vui long thu lai sau.",
                    updated.blockedUntilEpochSeconds - now);
        }
    }

    public void reset(String username, String clientIp) {
        states.remove(buildKey(username, clientIp));
    }

    private String buildKey(String username, String clientIp) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        String normalizedIp = clientIp == null ? "unknown" : clientIp.trim();
        return normalizedUsername + "|" + normalizedIp;
    }

    private static final class AttemptState {
        private int failureCount;
        private long windowStartedEpochSeconds;
        private long blockedUntilEpochSeconds;
    }
}
