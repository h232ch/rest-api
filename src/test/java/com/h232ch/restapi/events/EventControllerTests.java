package com.h232ch.restapi.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class) // 스프링 테스트 환경 구성 
@WebMvcTest // 웹 테스트 환경 구성 -> MockMVC 사용 가능 (가짜 요청 및 응답) 웹과 관련된 테스트만 진행하여 슬라이싱 테스트라고 함
public class EventControllerTests {
    
    @Autowired
    MockMvc mockMvc; // 슬라이싱 테스트 장점 (빠르다, 단위 테스트로 보기에는 어렵다, DataMapper, Handler 등이 생성되기 때문)
    // Spring MVC : Spring MVC 테스트를 위해 핵심적인 클래스
    // 웹 서버를 띄우지 않기 때문에 조금 더 빠르지만 Dispatcher Servlet 을 따로 만들어야 하기 때문에 단위 테스트보다는 조금 더 걸림

    // accept header를 지정하여 사용하는게 베스트프랙티스 (이게 안되면 확장자로 구분 abc.json, abc.xml)
    @Test
    public void createEvent() throws Exception {
        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)  // 요청에 JSON을 담아서 보내고 있다.
                .accept(MediaTypes.HAL_JSON) // HAL_JSON을 응답받기를 원한다. (HAL, Hypertext Application Language)
        ) // post에서 Servlet 생성 (MockMvc)
                .andExpect(status().isCreated());
    }
}
