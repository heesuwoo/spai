package org.example.spai.domain.openai.repository;


import java.util.List;

import org.example.spai.domain.openai.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Long>{
	List<ChatEntity> findByUserIdOrderByCreatedAtAsc(String userId);	// userId, 페이지 기반으로 시간순 정렬
}
