package org.example.spai.config;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AIConfig {

	// 스프링 AI 에서 멀티턴 기능(과거 N개의 대화 데이터를 ChatMemoryRepository 인터페이스에 담을 수 있도록)을 제공 함
	// 단, 멀티턴이라고 해서 max_prompt_size, 비용, 딜레이, 토큰 개수 등의문제로 과거의 모든 데이터를 다룰 수는 없음
	// 대략 몇 개 또는 요약 데이터를 활용해 멀티턴을 구현함
	
	
	// 기본 설정시 자동 등록이지만, 수동 등록 가능
    @Bean
    public ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {	
//        return new InMemoryChatMemoryRepository(); //  in-memory 방식
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .transactionManager(transactionManager)
                .build();
    }
}
