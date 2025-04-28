package tave.websocket.chatserver.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import tave.websocket.chatserver.chat.dto.ChatMessageDto;
import tave.websocket.chatserver.chat.service.ChatService;
import tave.websocket.chatserver.chat.service.RedisPubSubService;

@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;
    private final RedisPubSubService pubSubService;

    public StompController(SimpMessageSendingOperations messageTemplate, ChatService chatService, RedisPubSubService pubSubService) {
        this.messageTemplate = messageTemplate;
        this.chatService=chatService;
        this.pubSubService=pubSubService;
    }

//    방법1. MessageMapping(수신) 과 SendTO(topic에 메시지 전달) 한꺼번에 처리
//    @MessageMapping("/{roomId}") //쿨라이언트에서 특정 publish/{roomId}형태로 메시지를 발행시 MessageMapping
//    @SendTo("/topic/{roomId}") //해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 전송
//    public String sendMessage(@DestinationVariable String roomId, String message) {
//        System.out.println(message);
//        return message;
//    }


//    방법2. MessageMapping 어노테이션만 활용
    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) throws JsonProcessingException {
        System.out.println(chatMessageDto.getMessage());
        chatService.saveMessage(roomId, chatMessageDto); //메시지 저장
        chatMessageDto.setRoomId(roomId);
//        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageDto);//해당 roomId에 메시지를 발행
        ObjectMapper mapper = new ObjectMapper();
        String message=mapper.writeValueAsString(chatMessageDto);
        pubSubService.publish("chat",message);
    }
}

