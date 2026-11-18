package br.com.meritcoin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
class AuthService implements HandlerInterceptor {
    private final LoginSessionRepository sessions;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    AuthService(LoginSessionRepository sessions) { this.sessions=sessions; }

    String hash(String password) { return encoder.encode(password); }
    boolean matches(String password, String hash) { return hash != null && encoder.matches(password, hash); }
    String issue(UserAccount user) {
        LoginSession session=new LoginSession();
        session.token=UUID.randomUUID().toString()+UUID.randomUUID();
        session.usuario=user;
        session.expiraEm=LocalDateTime.now().plusDays(7);
        sessions.save(session);
        return session.token;
    }
    void logout(HttpServletRequest request) {
        String token=token(request);
        if(token!=null) sessions.deleteById(token);
    }
    private String token(HttpServletRequest request) {
        String header=request.getHeader("Authorization");
        return header!=null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) {
        String path=request.getRequestURI();
        if(path.equals("/api/auth/login") || path.equals("/api/usuarios") && request.getMethod().equals("POST") ||
           request.getMethod().equals("GET") && (path.equals("/api/instituicoes") || path.equals("/api/cursos") || path.equals("/api/vantagens"))) return true;
        LoginSession session=token(request)==null?null:sessions.findById(token(request)).orElse(null);
        if(session==null || session.expiraEm.isBefore(LocalDateTime.now()) || session.usuario!=null && !session.usuario.ativo) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Entre novamente para continuar.");
        request.setAttribute("account",session.usuario);
        request.setAttribute("admin",session.usuario==null);
        return true;
    }
    void require(HttpServletRequest request,Role role) {
        if(role==Role.ADMIN ? !Boolean.TRUE.equals(request.getAttribute("admin")) :
           request.getAttribute("account")==null || ((UserAccount)request.getAttribute("account")).role!=role)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Acesso negado.");
    }
    void requireSelf(HttpServletRequest request,Long id) {
        UserAccount user=(UserAccount)request.getAttribute("account");
        if(!Boolean.TRUE.equals(request.getAttribute("admin")) && (user==null || !user.id.equals(id)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Acesso negado.");
    }
}

@Configuration
class AuthWebConfig implements WebMvcConfigurer {
    private final AuthService auth;
    AuthWebConfig(AuthService auth) { this.auth=auth; }
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(auth).addPathPatterns("/api/**"); }
}
