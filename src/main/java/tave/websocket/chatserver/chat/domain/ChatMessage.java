package tave.websocket.chatserver.chat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tave.websocket.chatserver.common.domain.BaseTimeEntity;
import tave.websocket.chatserver.member.domain.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_room_id",nullable=false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id",nullable=false)
    private Member member;

    @Column(nullable = false,length =500)
    private String content;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.REMOVE,orphanRemoval = true)
    private List<ReadStatus> readStatuses=new ArrayList<>();

}
