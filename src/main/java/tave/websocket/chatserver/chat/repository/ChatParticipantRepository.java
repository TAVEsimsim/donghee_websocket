package tave.websocket.chatserver.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tave.websocket.chatserver.chat.domain.ChatParticipant;
import tave.websocket.chatserver.chat.domain.ChatRoom;
import tave.websocket.chatserver.member.domain.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
}
