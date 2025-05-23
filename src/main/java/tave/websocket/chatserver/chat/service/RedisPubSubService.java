package tave.websocket.chatserver.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tave.websocket.chatserver.chat.dto.ChatMessageDto;

@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;

    public RedisPubSubService(StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;

    }

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel,message);
    }

    @Override
    //pattern에는 topic의 이름의 패턴이 담겨있고, 이 패턴을 기반으로 다이나믹한 코딩
    public void onMessage(Message message, byte[] pattern){
        String payload=new String(message.getBody());
        ObjectMapper mapper = new ObjectMapper();
        try{
            ChatMessageDto chatMessageDto = mapper.readValue(payload,ChatMessageDto.class);
            messageTemplate.convertAndSend("/topic/"+chatMessageDto.getRoomId(),chatMessageDto);
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
}
