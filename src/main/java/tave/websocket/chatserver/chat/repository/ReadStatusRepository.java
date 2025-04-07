package tave.websocket.chatserver.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tave.websocket.chatserver.chat.domain.ReadStatus;

@Repository
public interface ReadStatusRepository extends JpaRepository<ReadStatus, Long> {
}
