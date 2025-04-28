package tave.websocket.chatserver.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import tave.websocket.chatserver.chat.service.ChatService;

@Component
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;
    @Value("${jwt.secretKey}")
    private String secretKey;

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    //
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT == accessor.getCommand()) {
            System.out.println("connect요청시 토큰 유호성 검증");
            String bearerToken=accessor.getFirstNativeHeader("Authorization");
            String token=bearerToken.substring(7);
//            토큰 검증
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("토큰 검증 완료");
        }
        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            String bearerToken=accessor.getFirstNativeHeader("Authorization");
            String token=bearerToken.substring(7);
//            토큰 검증
            Claims claims=Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String email=claims.getSubject();
            String roomId=accessor.getDestination().split("/")[2];
            if(!chatService.isParticipant(email,Long.parseLong(roomId))){
                throw new AuthenticationServiceException("해당 room 에 권한이 없습니다.");
            }
        }
        return message;
    }

}
