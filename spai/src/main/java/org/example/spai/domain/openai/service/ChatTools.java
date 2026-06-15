package org.example.spai.domain.openai.service;

import org.example.spai.domain.openai.dto.UserResponseDTO;
import org.springframework.ai.tool.annotation.Tool;

public class ChatTools {

	// Agent: 목표가 주어지면 그것을 이행하기 위해 LLM이 스스로 판단하여 행동을 수행하는 방식(ex. 주식자동매매 에이전트: LLM이 스스로 판단하여 주식을 자동으로 사고 파는 행위)
	// Tools: LLM이 문제를 해결하는 과정에서 외부의 도움이 필요하다고 판단 되었을 때 사용하는 도구(ex. 주식계좌, 계좌 비밀번호 등)
	// Tool Calling: Spring AI에서 LLM API에게 tools를 붙여주는 기능
	// tool : 검색 툴, DB 조회 툴, 계산 툴, 코드 실행 툴, 시각화 툴 등
	// 스프링 서비스단을 툴화 시켜 붙여줄 수 있음(LLM에게 메소드 사용법을 알려주고 활용하도록 함)
	
	
	// Spring AI에서 Tool 사용 원리
	// 1. 사용자의 프롬프트와 툴 목록을 함께 보내줌 (어떤 툴을 사용하고 어떨때 사용할지 보냄)
	// 2. LLM API가 사용자의 질문과 툴 목록을 확인 후, 특정 툴을 사용하겠다고 콜백을 줌(LLM이 질문에서 툴이 필요하다고 판단하면 콜백이 옴) 
	// 3. 툴을 활용해서 데이터를 처리하거나 가져옴(스프링 AI 어플리케이션에서 툴 실행) -> (특정한 Tool을 활용하여 질문에 대한 응답을 하라고 spring AI의 Tool Calling이 Tool을 실행시킴)
	// 4. 툴 처리 완료
	// 5. 툴 실행 결과를 다시 LLM API에 제공
	// 6. LLM API 최종 응답
	
	// - 기존 사용자의 프롬프트만 보냈을 때 : 1번 -> LLM API -> Chat Response
	// - Tool 이 추가 되었을 때 : LLM API 중간에 Tool 에 요청을 보내는 과정이 추가됨
	
	@Tool(description="User personal information : name, age, address, phone, etc")
	public UserResponseDTO getUserInfoTool() {
        return new UserResponseDTO("김지훈", 15L, "서울특별시 종로구 청와대로 1", "010-0000-0000", "03048");
    }
}
