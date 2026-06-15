package org.example.spai.api;

import java.util.List;
import java.util.Map;

import org.example.spai.domain.openai.dto.CityResponseDTO;
import org.example.spai.domain.openai.entity.ChatEntity;
import org.example.spai.domain.openai.service.ChatService;
import org.example.spai.domain.openai.service.OpenAIService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import reactor.core.publisher.Flux;

@Controller
public class ChatController {

	private final OpenAIService openAIService;
	private final ChatService chatService;

	public ChatController(OpenAIService openAIService, ChatService chatService) {
		this.openAIService = openAIService;
		this.chatService = chatService;
	}
	
	@GetMapping("/")
	public String chatPage() {
	    return "chat";	// 멀티턴, 히스토리 관리가 안됨(기존의 질문을 기억하지 못함: 내부적으로 히스토리로 관리해 질문을 던질때 기존의 질문도 같이 던질 수 있게 해야함 )
	}
	
	// none-stream 형태
    @ResponseBody
    @PostMapping("/chat")
//    public String chat(@RequestBody Map<String, String> body) {
    public CityResponseDTO chat(@RequestBody Map<String, String> body) {
        return openAIService.generate(body.get("text"));	// 사용자가 입력할 텍스트
    }

    // stream 형태
    @ResponseBody
    @PostMapping("/chat/stream")
    public Flux<String> streamChat(@RequestBody Map<String, String> body) {
        return openAIService.generateStream(body.get("text"));
    }	
    
    @ResponseBody
    @PostMapping("/chat/history/{userid}")
    public List<ChatEntity> getChatHistory(@PathVariable("userid") String userId) {
        return chatService.readAllChats(userId);
    }
}
