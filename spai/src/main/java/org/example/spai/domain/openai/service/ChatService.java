package org.example.spai.domain.openai.service;

import java.util.List;

import org.example.spai.domain.openai.entity.ChatEntity;
import org.example.spai.domain.openai.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ChatService {

	private final ChatRepository chatRepository;

	public ChatService(ChatRepository chatRepository) {
		this.chatRepository = chatRepository;
	}
	
	@Transactional(readOnly = true)
	public List<ChatEntity> readAllChats(String userId) {
		return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
	}
}
