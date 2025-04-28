package tave.websocket.chatserver.chat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tave.websocket.chatserver.common.domain.BaseTimeEntity;
import tave.websocket.chatserver.member.domain.Member;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReadStatus extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_room_id",nullable=false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id",nullable=false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_message_id",nullable=false)
    private ChatMessage chatMessage;

    @Column
    private Boolean isRead;

    public void updateIsRead(boolean isRead) {
        this.isRead=isRead;
    }
}
