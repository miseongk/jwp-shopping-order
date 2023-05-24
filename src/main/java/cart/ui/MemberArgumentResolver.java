package cart.ui;

import cart.dao.MemberDao;
import cart.domain.Member;
import cart.exception.AuthenticationException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class MemberArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String BASIC_AUTH_PREFIX = "Basic ";
    private final MemberDao memberDao;

    public MemberArgumentResolver(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthPrincipal.class) &&
                parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String authorization = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        validateAuthorizationHeader(authorization);

        String[] credentials = decode(authorization);
        String email = credentials[0];
        String password = credentials[1];

        Member member = memberDao.getMemberByEmail(email);
        if (!member.checkPassword(password)) {
            throw new AuthenticationException();
        }
        return member;
    }

    private String[] decode(String authorization) {
        String token = authorization.replace(BASIC_AUTH_PREFIX, "");
        String decodedToken = new String(Base64.decodeBase64(token));
        String[] credentials = decodedToken.split(":");
        return credentials;
    }

    private void validateAuthorizationHeader(String authorization) {
        if (authorization == null) {
            throw new AuthenticationException();
        }
        if (!authorization.startsWith(BASIC_AUTH_PREFIX)) {
            throw new AuthenticationException();
        }
    }
}
