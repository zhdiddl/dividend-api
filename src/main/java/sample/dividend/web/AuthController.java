package sample.dividend.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sample.dividend.model.Auth;
import sample.dividend.security.TokenProvider;
import sample.dividend.service.MemberService;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        // 회원 가입을 위한 API
        var result = this.memberService.register(request);
        log.info("user signed up: {}", request.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        // 로그인용 API
        // 1. 입력받은 아이디와 패스워드가 일치하는지 검증
        var member = this.memberService.authenticate(request);
        // 2. 검증이 완료되면 토큰을 생성해서 반환
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
        log.info("user logged in: {}", request.getUsername());
        return ResponseEntity.ok(token);
    }
}
