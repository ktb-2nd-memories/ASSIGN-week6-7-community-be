package ktb.community.be.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class SecurityUtil {

    private SecurityUtil() { }

    // SecurityContext 에 유저 정보가 저장되는 시점
    // Request 가 들어올 때 JwtFilter 의 doFilter 에서 저장
    public static Long getCurrentMemberId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || authentication.getName() == null
            || authentication.getName().equals("anonymousUser")) {
//            throw  new RuntimeException("Security Context 에 인증 정보가 없습니다.");
            throw new AuthenticationCredentialsNotFoundException("인증이 필요합니다.");  // 401 Unauthorized 처리
        }

        try {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails userDetails) {
                return Long.parseLong(userDetails.getUsername());
            } else {
                throw new AuthenticationCredentialsNotFoundException("잘못된 인증 정보입니다.");
            }
        } catch (NumberFormatException e) {
            throw new AuthenticationCredentialsNotFoundException("잘못된 인증 정보입니다.");
        }
    }
}
