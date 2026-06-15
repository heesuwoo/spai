package org.example.spai.domain.openai.service;

import java.util.List;

import org.example.spai.domain.openai.entity.ChatEntity;
import org.example.spai.domain.openai.repository.ChatRepository;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class OpenAIService {

    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiImageModel openAiImageModel;
    private final OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private final OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatRepository chatRepository;
    
    
    // 의존성 주입(생성자 방식)
	public OpenAIService(OpenAiChatModel openAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel,
			OpenAiImageModel openAiImageModel, OpenAiAudioSpeechModel openAiAudioSpeechModel,
			OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel, 
			@Qualifier("chatMemoryRepository") ChatMemoryRepository chatMemoryRepository, ChatRepository chatRepository) {
		this.openAiChatModel = openAiChatModel;
		this.openAiEmbeddingModel = openAiEmbeddingModel;
		this.openAiImageModel = openAiImageModel;
		this.openAiAudioSpeechModel = openAiAudioSpeechModel;
		this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
		this.chatMemoryRepository = chatMemoryRepository;
		this.chatRepository = chatRepository;
		
	}
	
    
    // 1. chatmodel : response
	public String generate(String text) {
		
		// 메시지(프롬포트)
	    SystemMessage systemMessage = new SystemMessage("");
	    UserMessage userMessage = new UserMessage(text);
	    AssistantMessage assistantMessage = new AssistantMessage("");
	    
	    // 옵션
	    // https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html 참고하여 필요한 옵션 추가
	    OpenAiChatOptions options = OpenAiChatOptions.builder()
	            .model("gpt-4.1-mini")
	            .temperature(0.7)
	            .build();	    
	    
	    // 프롬프트
	    Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);
	    
	    // call 메서드를 통해서 openAiChatModel을 prompt에 넣어줌
	    // 요청 및 응답
	    ChatResponse response = openAiChatModel.call(prompt);
	    return response.getResult().getOutput().getText();	    
	}
    
	
	
	
	// 1. chatmodel : response를 stream 하게 받는 방법
	// 데이터를 호출하고 응답 받을 때 Flux로 받음 + openai chat 모델을 호출할 때도 stream메서드로 바뀜
	public Flux<String> generateStream(String text) {
		
		// ChatClient 사용하는 이유
//		: ChatClient로 감싼 객체를 통해 아래 기능들을 쉽게 추가 가능
//		1. ObservationRegistry: 요청/응답에 대해 로깅 추가 가능
//		2. tools: LLM에게 사용할 툴(계산기 툴, 날씨 툴 등)을 붙여줌
//		3. advisors: RAG
//		4. entity: 응답 데이터를 java 객체로 파싱(call 메소드만)
//		5. 추상화: 모델 변경되어도(open ai 호출 엔트로픽 호출 등) 동일한 메소드로 호출 가능
		
		ChatClient chatClient = ChatClient.create(openAiChatModel); // 와핑
		

	    // 유저&페이지별 ChatMemory를 관리하기 위한 key (우선은 명시적으로)
	    String userId = "xxxjjhhh" + "_" + "3";
		
	    // 전체 대화 저장용
	    ChatEntity chatUserEntity = new ChatEntity();
	    chatUserEntity.setUserId(userId);
	    chatUserEntity.setType(MessageType.USER);
	    chatUserEntity.setContent(text);
	    
	    
	    // 메시지
//	    SystemMessage systemMessage = new SystemMessage("");
//	    UserMessage userMessage = new UserMessage(text);
//	    AssistantMessage assistantMessage = new AssistantMessage("");
	    ChatMemory chatMemory = MessageWindowChatMemory.builder()
	            .maxMessages(10)	// 몇개를 들고 있을건지
	            .chatMemoryRepository(chatMemoryRepository)
	            .build();
	    chatMemory.add(userId, new UserMessage(text)); // 신규 메시지도 추가
	    
	    
	    // 옵션
	    OpenAiChatOptions options = OpenAiChatOptions.builder()
	            .model("gpt-4.1-mini")
	            .temperature(0.7)
	            .build();

	    // 프롬프트
//	    Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);
	    Prompt prompt = new Prompt(chatMemory.get(userId), options);

	    
	    // 응답 메시지를 저장할 임시 버퍼
	    StringBuilder responseBuffer = new StringBuilder();
	    
	    
	    // 요청 및 응답
	    // 위의 메서드에서는 call로 호출했지만, 여기서는 stream호출함(요청한 이후에 토큰 단위로 응답받을 수 있음)
//	    return openAiChatModel.stream(prompt)
//	            .mapNotNull(response -> response.getResult().getOutput().getText());
	    // 요청 및 응답
//	    return openAiChatModel.stream(prompt)
//	            .mapNotNull(response -> {
//	                String token = response.getResult().getOutput().getText();
//	                responseBuffer.append(token);	// 각각의 토큰을 추가해줌
//	                return token;
//	            })
//	            .doOnComplete(() -> {
//
//	                chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
//	                chatMemoryRepository.saveAll(userId, chatMemory.get(userId));	// DB에 저장
//	                
//	                // 전체 대화 저장용
//	                // 응답받은 메시지
//	                ChatEntity chatAssistantEntity = new ChatEntity();
//	                chatAssistantEntity.setUserId(userId);
//	                chatAssistantEntity.setType(MessageType.ASSISTANT);
//	                chatAssistantEntity.setContent(responseBuffer.toString());
//	                
//	                chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
//	            });
	    // 요청 및 응답 -> ChatClient 사용
	    return chatClient.prompt(prompt)	// openAiChatModel 대신 chatClient로 진행
//	    		.advisors()
//	    		.tools()
	    		.stream()	// stream 응답(api 호출)
	    		.content()	// response.getResult().getOutput().getText()
	    		.map(token -> {	// token 받은 것 순회
	    			responseBuffer.append(token);
	    			return token;
	    		})
	    		.doOnComplete(() -> {
	                // chatMemory 저장
	                chatMemory.add(userId, new AssistantMessage(responseBuffer.toString()));
	                chatMemoryRepository.saveAll(userId, chatMemory.get(userId));

	                // 전체 대화 저장용
	                ChatEntity chatAssistantEntity = new ChatEntity();
	                chatAssistantEntity.setUserId(userId);
	                chatAssistantEntity.setType(MessageType.ASSISTANT);
	                chatAssistantEntity.setContent(responseBuffer.toString());

	                chatRepository.saveAll(List.of(chatUserEntity, chatAssistantEntity));
	    		});
	    		
	    		
	    		
	}	
	
	
	// 2. 임베딩 api 호출 메서드
	// 여러개의 문장에 대해서 여러개의 임베딩을 List float 형식으로 응답받음
	public List<float[]> generateEmbedding(List<String> texts, String model) {

	    // 옵션
		// 간단하게 어떤 모델을 사용해서 임베딩을 만들건지에 대해서만 세팅
	    EmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
	            .model(model).build();

	    // 프롬프트
	    EmbeddingRequest prompt = new EmbeddingRequest(texts, embeddingOptions);

	    // 요청 및 응답
	    EmbeddingResponse response = openAiEmbeddingModel.call(prompt);
	    return response.getResults().stream()
	            .map(Embedding::getOutput)
	            .toList();
	}	
	
	
	// 3. 이미지 모델 api 호출 메소드
	public List<String> generateImages(String text, int count, int height, int width) {

	    // 옵션
	    OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
	            .quality("hd")
	            .N(count)
	            .height(height)
	            .width(width)
	            .build();

	    // 프롬프트
	    ImagePrompt prompt = new ImagePrompt(text, imageOptions);

	    // 요청 및 응답
	    // 응답 데이터: base64 기반의 이미지 데이터, openai 서버에 올라가 있는 이미지 url
	    ImageResponse response = openAiImageModel.call(prompt);
	    return response.getResults().stream()
	            .map(image -> image.getOutput().getUrl())
	            .toList();
	}
	
	// 4. TTS : 텍스트 > 오디오(base64 기반의 byte)
	public byte[] tts(String text) {

	    // 옵션
	    OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
	            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
	            .speed((double) 1.0f)
	            .model(OpenAiAudioApi.TtsModel.TTS_1.value)
	            .build();

	    // 프롬프트
	    TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, speechOptions);

	    // 요청 및 응답
	    TextToSpeechResponse response = openAiAudioSpeechModel.call(prompt);
	    return response.getResult().getOutput();
	}	
	
	// 4. STT
	public String stt(Resource audioFile) {

	    // 옵션
	    OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT;
	    OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
	            .language("ko") // 인식할 언어
	            .prompt("Ask not this, but ask that") // 음성 인식 전 참고할 텍스트 프롬프트
	            .temperature(0f)
	            .model(OpenAiAudioApi.TtsModel.TTS_1.value)
	            .responseFormat(responseFormat) // 결과 타입 지정 VTT 자막형식
	            .build();

	    // 프롬프트
	    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);

	    // 요청 및 응답
	    AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
	    return response.getResult().getOutput();
	}	
	
}
