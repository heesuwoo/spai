package org.example.spai.domain.openai.dto;

import java.util.List;

public record CityResponseDTO(List<String> city) {

	// record란 : java 16에서 공개됨
	// - 불변 객체로 모든 필드들이 final로 선언되어 생성자 초기화 후 변경 불가함. = set이 안됨
	// - 생성자, getter, hashCode, equals, toString 자동 제공 => 생성자 느낌으로 선언만 하면됨
	// - 상속 불가
	// 스프링 부트 프로젝트에서 응답 DTO에 사용(setter가 없는 dto) -> 성능향상에 도움
	
	//  성능 향상
	// 1. 컴파일 단계: 프로젝트를 다 만들고 컴파일 할 때
	// "record" 와 "클래스 + @Lombok" 모두 컴파일 단계에서 "생성자, getter, hashCode, equals, toString" 들이 생성됨.
	// "record"는 컴파일러가 가장 최적화된 방식으로 해당하는 메소드를 생성함. => JVM이 훨씬 빠르고 효율적이게 컴파일을 진행하므로 "클래스 + @Lombok" 보다 훨씬 빠르고 최적화됨.
	
	// 2. 런타임 단계: 스프링부트를 배포하고 서버에 띄우고 있을 때
	// JVM이 record에 대해서 어느 정도 최적화된 동작을 할 수 있지만 아주 미미한 차이를 가짐
	// 다만, "클래스 + @Lombok"에 final 선언을 하지 않은 경우 GC(Garbage Collection) 동작에 대해서는 "record"가 훨씬 더 좋은 효율을 가짐.
	
}
