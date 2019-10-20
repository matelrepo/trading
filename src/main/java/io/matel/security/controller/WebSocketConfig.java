package io.matel.security.controller;

import com.auth0.jwt.JWT;
import io.matel.common.Global;
import io.matel.security.config.User;
import io.matel.security.config.UserPrincipal;
import io.matel.security.config.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;


@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig  implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

private UserRepository userRepository;

    public WebSocketConfig( UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/get");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api-feed")
                .setAllowedOrigins("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        System.out.println("Handshake interceptor beforeHandshake");
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
                        System.out.println("Handshake interceptor after");
                    }
                });
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(200000); // default : 64 * 1024
        registration.setSendTimeLimit(20 * 10000); // default : 10 * 10000
        registration.setSendBufferSizeLimit(3 * 512 * 1024); // default : 512 * 1024
    }

//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.setInterceptors(rmeSessionChannelInterceptor());
//    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                MessageHeaders headers = message.getHeaders();

                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    String token = getTokenFromStompHeader(headers);

                    Authentication user = getUsernamePasswordAuthentication(token);
                    accessor.setUser(user);

                }
                return message;
            }
        });
    }

//    @Bean
//    public RmeSessionChannelInterceptor rmeSessionChannelInterceptor() {
//        return new RmeSessionChannelInterceptor();
//    }

    private String getTokenFromStompHeader(MessageHeaders headers){
        String token ="";

        MultiValueMap<String, String> multiValueMap = headers.get(StompHeaderAccessor.NATIVE_HEADERS,MultiValueMap.class);
        for(Map.Entry<String, List<String>> head : multiValueMap.entrySet())
        {
            System.out.println(head.getKey() + "#" + head.getValue());
            if(head.getKey().equals("Authorization"))
                token = head.getValue().toString().replace("[", "").replace("]","");
        }

        token = token.replace(Global.TOKEN_PREFIX,"");
        System.out.println("My token is " + token);
        return token;
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