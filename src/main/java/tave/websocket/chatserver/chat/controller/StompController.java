package tave.websocket.chatserver.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    @MessageMapping("/{roomId}") //쿨라이언트에서 특정 publish/{roomId}형태로 메시지를 발행시 MessageMapping
    @SendTo("/topic/{roomId}") //해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 전송
    public String sendMessagte(@DestinationVariable String roomId, String message) {
        System.out.println(message);
        return message;
    }
}
