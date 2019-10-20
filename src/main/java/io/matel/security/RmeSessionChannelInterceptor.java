//package io.matel.security;
//
//import com.auth0.jwt.JWT;
//import io.matel.common.Global;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.MessageHeaders;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.util.MultiValueMap;
//
//
//import java.util.List;
//import java.util.Map;
//
//import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
//
//public class RmeSessionChannelInterceptor implements ChannelInterceptor {
//
//    @Autowired
//    UserRepository userRepository;
//
//
//    //private AuthenticationManager authenticationManager;
//
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        System.out.println("Channel Interceptor pre send");
//        MessageHeaders headers = message.getHeaders();
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//
////        Authentication auth = authenticationManager.authenticate(getTokenFromStompHeader(headers));
//
//        String userName = JWT.require(HMAC512(Global.SECRET.getBytes()))
//                .build()
//                .verify(getTokenFromStompHeader(headers))
//                .getSubject();
//
////        User user = userRepository.findByUsername(userName);
////        UserPrincipal principal = new UserPrincipal(user);
//
////        Authentication authentication = jwtAuthorizationFilter.getUsernamePasswordAuthenticationByToken("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkYW4iLCJpZCI6MSwiZXhwIjoxNTcyNDA4NjcxfQ.QPzNiZJNaZlalrO-8Xhf1M82wp6zHWk-63Qp5vjdddBc3jLyFbPtghQFbZIbdKkq62U39gyCZu0rmG8H8eXKAA\n");
////        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return message;
//    }
//
//    public String getTokenFromStompHeader(MessageHeaders headers){
//        String token ="";
//
//        MultiValueMap<String, String> multiValueMap = headers.get(StompHeaderAccessor.NATIVE_HEADERS,MultiValueMap.class);
//        for(Map.Entry<String, List<String>> head : multiValueMap.entrySet())
//        {
//            System.out.println(head.getKey() + "#" + head.getValue());
//            if(head.getKey().equals("Authorization"))
//                token = head.getValue().toString().replace("[", "").replace("]","");
//        }
//
//        token = token.replace(Global.TOKEN_PREFIX,"");
//        System.out.println("My token is " + token);
//        return token;
//    }
//}