package org.example.spai.domain.openai.service;

import java.util.List;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
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
    
    // 의존성 주입(생성자 방식)
	public OpenAIService(OpenAiChatModel openAiChatModel, OpenAiEmbeddingModel openAiEmbeddingModel,
			OpenAiImageModel openAiImageModel, OpenAiAudioSpeechModel openAiAudioSpeechModel,
			OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel) {
		this.openAiChatModel = openAiChatModel;
		this.openAiEmbeddingModel = openAiEmbeddingModel;
		this.openAiImageModel = openAiImageModel;
		this.openAiAudioSpeechModel = openAiAudioSpeechModel;
		this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
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

	    // 메시지
	    SystemMessage systemMessage = new SystemMessage("");
	    UserMessage userMessage = new UserMessage(text);
	    AssistantMessage assistantMessage = new AssistantMessage("");

	    // 옵션
	    OpenAiChatOptions options = OpenAiChatOptions.builder()
	            .model("gpt-4.1-mini")
	            .temperature(0.7)
	            .build();

	    // 프롬프트
	    Prompt prompt = new Prompt(List.of(systemMessage, userMessage, assistantMessage), options);

	    // 요청 및 응답
	    // 위의 메서드에서는 call로 호출했지만, 여기서는 stream호출함(요청한 이후에 토큰 단위로 응답받을 수 있음)
	    return openAiChatModel.stream(prompt)
	            .mapNotNull(response -> response.getResult().getOutput().getText());
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
