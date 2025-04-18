package tave.websocket.chatserver.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tave.websocket.chatserver.chat.dto.ChatRoomListResDto;
import tave.websocket.chatserver.chat.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 그룹 채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestParam String roomName){
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok().build();
    }

    //그룹 채팅 목록 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupRooms(){
        List<ChatRoomListResDto> chatRooms=chatService.getGroupChatRooms();
        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
    }

    //그룹 채팅방 참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupRoom(@PathVariable Long roomId) {
        chatService.addParticipantToGroupChat(roomId);
        return ResponseEntity.ok().build();
    }
}
