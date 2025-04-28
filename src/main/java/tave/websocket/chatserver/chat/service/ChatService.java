package tave.websocket.chatserver.chat.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tave.websocket.chatserver.chat.domain.ChatMessage;
import tave.websocket.chatserver.chat.domain.ChatParticipant;
import tave.websocket.chatserver.chat.domain.ChatRoom;
import tave.websocket.chatserver.chat.domain.ReadStatus;
import tave.websocket.chatserver.chat.dto.ChatMessageDto;
import tave.websocket.chatserver.chat.dto.ChatRoomListResDto;
import tave.websocket.chatserver.chat.dto.MyChatListResDto;
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


    public void saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        //채팅방 조회
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new IllegalArgumentException("Room not found"));

        //보낸사람 조회
        Member sender=memberRepository.findByEmail(chatMessageDto.getSenderEmail()).orElseThrow(()->new IllegalArgumentException("Member not found"));

        //메시지 저장
        ChatMessage chatMessage=ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(chatMessageDto.getMessage())
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

        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("그룹채팅이 아닙니다");
        }
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

    public List<ChatMessageDto> getChatHistory(Long roomId) {
        //내가 해당 채팅방의 참여자가 아닐 경우 오류 발생 (채팅 참여자가 아닌사람이 roomId를 이용해 메시지 내역을 조회하는 것을 방지)
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room not found"));
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        List<ChatParticipant> chatParticipants=chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check=false;
        for(ChatParticipant c:chatParticipants){
            if(c.getMember().equals(member)){
                check=true;
            }
        }
        if(!check) throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
        // 특정 room에 대한 메시지 조회
        List<ChatMessage> chatMessages=chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos=new ArrayList<>();
        for(ChatMessage c:chatMessages){
            ChatMessageDto chatMessageDto= ChatMessageDto.builder()
                    .message(c.getContent())
                    .senderEmail(member.getEmail())
                    .build();
        }
        return chatMessageDtos;
    }
    public boolean isParticipant(String email, Long roomId){
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room not found"));
        Member member=memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("Member not found"));
        
        List<ChatParticipant> chatParticipants=chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c:chatParticipants){
            if(c.getMember().equals(member)){
                return true;
            }
        }
        return false;
    }

    public void messageRead(Long roomId) {
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room not found"));
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        List<ReadStatus> readStatuses=readStatusRepository.findByChatRoomAndMember(chatRoom,member);
        for(ReadStatus r:readStatuses){
            r.updateIsRead(true);
        }
    }

    public List<MyChatListResDto> getMyRooms() {
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        List<ChatParticipant> chatParticipants=chatParticipantRepository.findAllByMember(member);
        List<MyChatListResDto> chatListDtos=new ArrayList<>();
        for(ChatParticipant c:chatParticipants){
            Long count=readStatusRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(),member);
            MyChatListResDto dto= MyChatListResDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unreadCount(count)
                    .build();
            chatListDtos.add(dto);
        }
        return  chatListDtos;
    }

    public void leaveGroupChatRoom(Long roomId) {
        ChatRoom chatRoom=chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("Room not found"));
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }
        ChatParticipant c=chatParticipantRepository.findByChatRoomAndMember(chatRoom,member).orElseThrow(()->new EntityNotFoundException("Member not found"));
        chatParticipantRepository.delete(c);

        //채팅방에 참여자가 없으면 채팅방 삭제 (양방향 매핑의 CasecadeType.REMOVE를 통해 관련한 메시지와 읽음여부 데이터까지 함께 삭제)
        List<ChatParticipant> chatParticipants=chatParticipantRepository.findByChatRoom(chatRoom);
        if(chatParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom);
        }

    }

    public Long getOrCreatePrivateRoom(Long otherMemberId) {
        Member member=memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("Member not found"));
        Member otherMember=memberRepository.findById(otherMemberId).orElseThrow(()->new EntityNotFoundException("Other member not found"));

        //나와 상대방이 1:1이 이미 참여하고있다면 해당 roomId return
        Optional<ChatRoom> chatRoom=chatParticipantRepository.findExistingPrivateRoom(member.getId(),otherMember.getId());
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }

        //1:1 채빙당이 없을 경우, 새로 채팅방 개설 (기존의 로직 이용)
        ChatRoom newRoom= ChatRoom.builder()
                .isGroupChat("N")
                .name(member.getName() + "-" + otherMember.getName())
                .build();
        chatRoomRepository.save(newRoom);

        // 두 사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom,member);
        addParticipantToRoom(newRoom,otherMember);

        return newRoom.getId();

    }
}
