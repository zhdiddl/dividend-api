package sample.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sample.dividend.exception.imple.AlreadyExistUserException;
import sample.dividend.exception.imple.NoAccountException;
import sample.dividend.exception.imple.PasswordNotMatchedException;
import sample.dividend.model.Auth;
import sample.dividend.model.MemberEntity;
import sample.dividend.persist.MemberRepository;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("load failed: could not found member with username: {}", username);
                    return new NoAccountException();
                });
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            log.warn("registration failed: user with the given name already exists: {}", member.getUsername());
            throw new AlreadyExistUserException();
        }
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));

        return this.memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> {
                    log.warn("authentication failed: no account found for username: {}", member.getUsername());
                    return new NoAccountException();
                });

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            log.warn("authentication failed: password does not match for username: {}", member.getUsername());
            throw new PasswordNotMatchedException();
        }

        return user;
    }
}
