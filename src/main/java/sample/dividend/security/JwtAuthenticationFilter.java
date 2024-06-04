package sample.dividend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "Authorization"; // 토큰을 찾을 때 쓰는 헤더 이름
    public static final String TOKEN_PREFIX = "Bearer "; // 토큰 앞에 붙는 접두사

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request); // 요청에서 토큰 추출

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) { // 토큰 유효성 검증
            Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth); // 인증 정보를 홀더에 저장

            log.info(String.format("[%s] -> %s", this.tokenProvider.getUsername(token), request.getRequestURI()));
        }

        filterChain.doFilter(request, response); // 요청을 다음 필터로 넘기기
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
