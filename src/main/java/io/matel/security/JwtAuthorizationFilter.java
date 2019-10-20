package io.matel.security;

import com.auth0.jwt.JWT;
import io.matel.common.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Read the Authorization header, where the JWT token should be
        String header = request.getHeader(Global.HEADER_STRING);


        // If header does not contain BEARER or is null delegate to Spring impl and exit
        if (header == null || !header.startsWith(Global.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        // If header is present, try grab user principal from database and perform authorization
        Authentication authentication = getUsernamePasswordAuthenticationByServlet(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue filter execution
        chain.doFilter(request, response);
    }

    public Authentication getUsernamePasswordAuthenticationByToken(String token){
        return getUsernamePasswordAuthentication(token);
    }

    private Authentication getUsernamePasswordAuthenticationByServlet(HttpServletRequest request){
        String token = request.getHeader(Global.HEADER_STRING)
                .replace(Global.TOKEN_PREFIX,"");
        return getUsernamePasswordAuthentication(token);
    }

    private Authentication getUsernamePasswordAuthentication(String token){
//        String token = request.getHeader(Global.HEADER_STRING)
//                .replace(Global.TOKEN_PREFIX,"");

        if (token!=null) {
            // parse the token and validate it
            try{
            String userName = JWT.require(HMAC512(Global.SECRET.getBytes()))
                    .build()
                    .verify(token)
                    .getSubject();


            // Search in the DB if we find the user by token subject (username)
            // If so, then grab user details and create spring auth token using username, pass, authorities/roles
            if (userName != null) {
                User user = userRepository.findByUsername(userName);
                UserPrincipal principal = new UserPrincipal(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userName, null, principal.getAuthorities());
                return auth;
            }

            }
            catch(com.auth0.jwt.exceptions.JWTDecodeException e){
                System.out.println(e.getMessage());
                return null;
            }
            return null;
        }
        return null;
    }
}
