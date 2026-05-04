package doctor.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.basic.username:sylein}") String username,
            @Value("${app.security.basic.password:123456}") String password,
            @Value("${app.security.basic.roles:ADMIN}") String roles,
            PasswordEncoder passwordEncoder) {
        String[] roleList =
                roles == null
                        ? new String[] {"ADMIN"}
                        : java.util.Arrays.stream(roles.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toArray(String[]::new);

        String encodedPassword = encodeIfNeeded(password, passwordEncoder);

        UserDetails user =
                User.builder()
                        .username(username == null || username.isBlank() ? "sylein" : username.trim())
                        .password(encodedPassword)
                        .roles(roleList.length == 0 ? new String[] {"ADMIN"} : roleList)
                        .build();

        return new InMemoryUserDetailsManager(user);
    }

    private static String encodeIfNeeded(String password, PasswordEncoder passwordEncoder) {
        if (password == null) {
            return passwordEncoder.encode("");
        }
        String trimmed = password.trim();
        if (trimmed.startsWith("{bcrypt}")) {
            return trimmed.substring("{bcrypt}".length());
        }
        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            return trimmed;
        }
        return passwordEncoder.encode(trimmed);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        if (!securityEnabled) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/auth/login",
                                                "/api/auth/register/user",
                                                "/api/auth/register/doctor",
                                                "/api/auth/otp/send",
                                                "/api/auth/otp/verify")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
