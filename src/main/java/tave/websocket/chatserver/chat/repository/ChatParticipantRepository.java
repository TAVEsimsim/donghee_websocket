package tave.websocket.chatserver.chat.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    List<ChatParticipant> findAllByMember(Member member);

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id=cp2.chatRoom.id WHERE cp1.member.id=:myId and cp2.member.id=:otherMemberId AND cp1.chatRoom.isGroupChat='N'")
    Optional<ChatRoom> findExistingPrivateRoom(@Param("myId") Long id, @Param("otherMemberId") Long id1);
}
