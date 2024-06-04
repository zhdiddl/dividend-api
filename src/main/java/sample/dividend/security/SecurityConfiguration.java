package sample.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity // 웹 보안 활성화
@EnableMethodSecurity
// 메소드 보안 활성화, @EnableGlobalMethodSecurity(prePostEnabled = true) 대체
@RequiredArgsConstructor // 필수 생성자 생성
public class SecurityConfiguration {

    private final JwtAuthenticationFilter authenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // HTTP 보안 설정
        http
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP 기본 인증 비활성화 (보안 수준이 낮아서 불필요)
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (세션을 쓰지 않기 때문에 불필요)
                .authorizeHttpRequests(authz -> authz // 인증 없이 접근 가능한 경로 설정
                        .requestMatchers("/auth/signup", "/auth/signin", "/h2-console/**").permitAll()
                        .anyRequest().authenticated() // 이 외에는 인증 필요
                )
                .addFilterBefore(this.authenticationFilter, UsernamePasswordAuthenticationFilter.class) // 특정 클래스에 인증 필터 추가
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 미사용 설정
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // 동일 출처 콘텐츠를 iframe으로 포함 (외부 출처의 악성 콘텐츠 삽입 방지)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); // 인증 관리자 빈 생성
    }
}