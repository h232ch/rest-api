package com.h232ch.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class) // 스프링 Junit 테스트 환경 구성
@WebMvcTest // 웹 테스트 환경 구성 -> MockMVC 사용 가능 (가짜 요청 및 응답) 웹과 관련된 테스트만 진행하여 슬라이싱 테스트라고 함
public class EventControllerTests {
    
    @Autowired
    MockMvc mockMvc; // 슬라이싱 테스트 장점 (빠르다, 단위 테스트로 보기에는 어렵다, DataMapper, Handler 등이 생성되기 때문)
    // Spring MVC : Spring MVC 테스트를 위해 핵심적인 클래스
    // 웹 서버를 띄우지 않기 때문에 조금 더 빠르지만 Dispatcher Servlet 을 따로 만들어야 하기 때문에 단위 테스트보다는 조금 더 걸림

    // accept header를 지정하여 사용하는게 베스트프랙티스 (이게 안되면 확장자로 구분 abc.json, abc.xml)


    @Autowired
    ObjectMapper objectMapper; // json으로 변환하는 클래스

    @MockBean
    EventRepository eventRepository; // 이벤트 레파시토리의 테스트용 빈을 생성해줘야함 (@WebMvcTest는 웹과 관련된 기능만 제공하지 빈까지 모두 자동으로 등록해주지 않음)
    // 이 객체는 Mock(가짜) 객체이기 때문에 Save 등의 메서드를 사용해도 Null값이 반환됨 (껍데기만 있는 객체임)
    // 그래서 Mockito를 사용해서 save가 호출될 때 event를 리턴하라고 명시해줘야 함

    @Test
    public void createEvent() throws Exception {

        Event event = Event.builder()
                .name("Srping")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Ganam")
                .build();

        event.setId(10);
        Mockito.when(eventRepository.save(event)).thenReturn(event); // 이를 스터빙한다고 표현함

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)  // 요청에 JSON을 담아서 보내고 있다.
                .accept(MediaTypes.HAL_JSON) // HAL_JSON을 응답받기를 원한다. (HAL, Hypertext Application Language)
                .content(objectMapper.writeValueAsString(event)) // 요청 본문에 Event 객체를 json형태로 변환해서 보냄
        ) // post에서 Servlet 생성 (MockMvc)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists());
    }
}
