package com.h232ch.restapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.h232ch.restapi.events.EventRepository;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class) // 스프링 Junit 테스트 환경 구성
//@WebMvcTest // 웹 테스트 환경 구성 -> MockMVC 사용 가능 (가짜 요청 및 응답) 웹과 관련된 테스트만 진행하여 슬라이싱 테스트라고 함
@SpringBootTest // 실제 프로젝트 내에 존재하는 코드를 빈으로 등록하고 사용하는 테스트 방법
// 테스트코드는 SpringBootTest를 사용하는 것을 추천 이유는 @WebMvcTest에서는 관련 빈을 모두 목킹해줘야하고 그값의 입력되는 값또한 Mokito로 지정해줘야해서 번거롭고
// 코드를 변경할때마다 관리해줘야해서 공수가 많이 든다.
// @SpringBootTest는 프로덕환경과 가장 가까운 테스트 환경이다. (실제 빈이 모두 등록되며 사용 가능)
// @SpringBootTest는 통합테스트 개념이다. (MockMVC는 슬라이싱 테스트 성격을띔)
@AutoConfigureMockMvc
@AutoConfigureRestDocs // RESTDocs를 이용하기 위해 추가
@Import(RestDocsConfiguration.class) // RestDocs 설정값을 지정한 클래스 로드
@Ignore // BaseControllerTest는 테스트용 클래스가 아니기때문에 Ignore를 가지고 있어야 함 (해당 애노테이션을 붙이면 테스트코드로 간주하지 않음 )
public class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc; // 슬라이싱 테스트 장점 (빠르다, 단위 테스트로 보기에는 어렵다, DataMapper, Handler 등이 생성되기 때문)

    @Autowired
    protected ObjectMapper objectMapper; // json으로 변환하는 클래스

    @Autowired
    protected EventRepository eventRepository;
}
