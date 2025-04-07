package tave.websocket.chatserver.chat.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tave.websocket.chatserver.chat.domain.ChatMessage;
import tave.websocket.chatserver.chat.domain.ChatParticipant;
import tave.websocket.chatserver.chat.domain.ChatRoom;
import tave.websocket.chatserver.chat.domain.ReadStatus;
import tave.websocket.chatserver.chat.dto.ChatMessageReqDto;
import tave.websocket.chatserver.chat.dto.ChatRoomListResDto;
import tave.websocket.chatserver.chat.repository.ChatMessageRepository;
import tave.websocket.chatserver.chat.repository.ChatParticipantRepository;
import tave.websocket.chatserver.chat.repository.ChatRoomRepository;
import tave.websocket.chatserver.chat.repository.ReadStatusRepository;
import tave.websocket.chatserver.member.domain.Member;
import tave.websocket.chatserver.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MemberRepository memberRepository;

    public ChatService(ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ChatRoomRepository chatRoomRepository, ReadStatusRepository readStatusRepository, MemberRepository memberRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.readStatusRepository = readStatusRepository;
        this.memberRepository = memberRepository;
    }


    public void saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
        //채팅방 조회
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new IllegalArgumentException("Room not found"));

        //보낸사람 조회
        Member sender=memberRepository.findByEmail(chatMessageReqDto.getSenderEmail()).orElseThrow(()->new IllegalArgumentException("Member not found"));

        //메시지 저장
        ChatMessage chatMessage=ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        //사용자별로 읽음여부 저장
        List<ChatParticipant> chatParticipants=chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c:chatParticipants){
            ReadStatus readStatus= ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().equals(sender)) //보낸사람은 true, 그 외 사람들은 false
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    public void createGroupRoom(String roomName) {
        //개설자: 이메일을 authentification 객체에서 찾아옴
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));

        //채팅방 생성
        ChatRoom chatRoom= ChatRoom.builder()
                .name(roomName)
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);
        //채팅 참여자로 개설자를 추가
        ChatParticipant chatParticipant= ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatRoomListResDto> getGroupChatRooms() {
        List<ChatRoom> chatRooms=chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos=new ArrayList<>();
        for(ChatRoom c:chatRooms){
            ChatRoomListResDto dto= ChatRoomListResDto.builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build();
            dtos.add(dto);
        }
        return dtos;
    }

    public void addParticipantToGroupChat(Long roomId) {
        //채팅방 조회
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room not found"));
        //member 조회
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        //이미 참여자인지 검증
        Optional<ChatParticipant> participant=chatParticipantRepository.findByChatRoomAndMember(chatRoom,member);
        if(!participant.isPresent()){
            addParticipantToRoom(chatRoom,member);
        }
    }

    //chatParticipant 객체 생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, Member member) {
        ChatParticipant chatParticipant= ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }
}
