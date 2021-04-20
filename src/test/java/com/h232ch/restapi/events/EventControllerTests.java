package com.h232ch.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.h232ch.restapi.accounts.Account;
import com.h232ch.restapi.accounts.AccountRepository;
import com.h232ch.restapi.accounts.AccountRole;
import com.h232ch.restapi.accounts.AccountService;
import com.h232ch.restapi.common.AppProperties;
import com.h232ch.restapi.common.BaseControllerTest;
import com.h232ch.restapi.common.RestDocsConfiguration;
import com.h232ch.restapi.common.TestDescription;
import org.codehaus.jackson.JsonParser;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.ui.ModelMap;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@RunWith(SpringRunner.class) // 스프링 Junit 테스트 환경 구성
////@WebMvcTest // 웹 테스트 환경 구성 -> MockMVC 사용 가능 (가짜 요청 및 응답) 웹과 관련된 테스트만 진행하여 슬라이싱 테스트라고 함
//@SpringBootTest // 실제 프로젝트 내에 존재하는 코드를 빈으로 등록하고 사용하는 테스트 방법
//// 테스트코드는 SpringBootTest를 사용하는 것을 추천 이유는 @WebMvcTest에서는 관련 빈을 모두 목킹해줘야하고 그값의 입력되는 값또한 Mokito로 지정해줘야해서 번거롭고
//// 코드를 변경할때마다 관리해줘야해서 공수가 많이 든다.
//// @SpringBootTest는 프로덕환경과 가장 가까운 테스트 환경이다. (실제 빈이 모두 등록되며 사용 가능)
//// @SpringBootTest는 통합테스트 개념이다. (MockMVC는 슬라이싱 테스트 성격을띔)
//@AutoConfigureMockMvc
//@AutoConfigureRestDocs // RESTDocs를 이용하기 위해 추가
//@Import(RestDocsConfiguration.class) // RestDocs 설정값을 지정한 클래스 로드
//public class EventControllerTests {
public class EventControllerTests extends BaseControllerTest { // 테스트 코드를 간결하게 리팩터링하기 위해
    // 애노테이션과 주입받는 빈을 BaseController에 기재하고 BaseController를 상속받으면 테스트 코드가 간결해지고
    // 기존에 사용하던 기능을 모두 동일하게 사용가능하다.

//    @Autowired
//    MockMvc mockMvc; // 슬라이싱 테스트 장점 (빠르다, 단위 테스트로 보기에는 어렵다, DataMapper, Handler 등이 생성되기 때문)
    // Spring MVC : Spring MVC 테스트를 위해 핵심적인 클래스
    // 웹 서버를 띄우지 않기 때문에 조금 더 빠르지만 Dispatcher Servlet 을 따로 만들어야 하기 때문에 단위 테스트보다는 조금 더 걸림

    // accept header를 지정하여 사용하는게 베스트프랙티스 (이게 안되면 확장자로 구분 abc.json, abc.xml)


//    @Autowired
//    ObjectMapper objectMapper; // json으로 변환하는 클래스

//    @MockBean // @SpringBootTest 환경에서는 불필요함 (실제 환경과 같은 환경을 사용하기 때문에 프로덕코드에서 해당 빈을 생성하고 있다면 그것에 따라 움직임)
//    EventRepository eventRepository; // 이벤트 레파시토리의 테스트용 빈을 생성해줘야함 (@WebMvcTest는 웹과 관련된 기능만 제공하지 빈까지 모두 자동으로 등록해주지 않음)
    // 이 객체는 Mock(가짜) 객체이기 때문에 Save 등의 메서드를 사용해도 Null값이 반환됨 (껍데기만 있는 객체임)
    // 그래서 Mockito를 사용해서 save가 호출될 때 event를 리턴하라고 명시해줘야 함

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountRepository accountRepository;

    // 이벤트 레포시토리를 주입받는다.

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    @Before // Test 증 데이터가 공유되기 떄문에 여러 테스트를 동시에 수행하는 경우 setUp을 이용해서 초기화 작업을 해야함
    // 테스트 메서드 실행 전 초기화를 수행해줌
    public void setUp() { 
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll(); 
    }

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {

//        Event event = Event.builder() // 이상태에서는 id, free등의 입력불가한 값을 입력할 수 있음
        EventDto event = EventDto.builder()
                .name("Srping")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Ganam")
//                .id(100) // 이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다. -> EventController에 EventDto를 생성하여 파라메터로 받게하면 해당 값을 안받음
//                .free(true) // 이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
//                .offline(true) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
//                .eventStatus(EventStatus.PUBLISHED) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
                .build();

//        event.setId(10);
//        Mockito.when(eventRepository.save(event)).thenReturn(event); // 이를 스터빙한다고 표현함 (@SpringWebMvc 애노테이션 환경에서 사용 (슬라이싱 테스트) -> 웹만 테스트하기 때문에 프로젝트 내에 사용되는 Bean을 별도로 등록해주지 않음
        // Save는 EventController 내부에 구현되고있는 eventRepository.save이다.
//        Mockito.when(eventRepository.save(event)).thenReturn(event); // EventController에서 받는 파라메터의 형태가 Event에서 EventDto 형태로 변경되었기 때문에 기존코드는 동작하지 않음
        // 이를 해결하기 위해 슬라이싱 테스트(MVC)를 더이상 진행하지 않고 전체 테스트(모든 빈을 불러오는) 환경으로 바꿔줘야 함
        // @SpringBootTest (테스트를 위해 실제 빈들을 모두 불러와서 IoC Container에 담아줌)
        // @AutoConfigureMockMvc (MockMVC를 사용할 수있음)


        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)  // 요청에 JSON을 담아서 보내고 있다.
                .accept(MediaTypes.HAL_JSON) // HAL_JSON을 응답받기를 원한다. (HAL, Hypertext Application Language)
                .content(objectMapper.writeValueAsString(event)) // 요청 본문에 Event 객체를 json형태로 변환해서 보냄
        ) // post에서 Servlet 생성 (MockMvc)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
//                .andExpect(header().exists("Location")) // 아래는 Type Safe 반식
                .andExpect(header().exists(HttpHeaders.LOCATION))
//                .andExpect(header().string("Content-Type", "application/hal+json")); // 아래는 Type Safe 반식
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
//                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(Matchers.not(EventStatus.DRAFT)))
                .andExpect(jsonPath("_links.self").exists()) // HEATOS
//                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andDo(document("create-event", // 스니펫을 추가하여 link, requestHeader, euqestFileds 등 문서조각을 생성함 Spring REST DOCS
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new event"),
                                fieldWithPath("basePrice").description("basePrice of new event"),
                                fieldWithPath("maxPrice").description("maxPrice of new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location heaser"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content header")
                        ),
//                        responseFields( // relaxedResponseFields는 사용하지 않는것이 좋다 (추후 변경 발생시 누락이 발생됨)
                        relaxedResponseFields(
                                fieldWithPath("id").description("id of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new event"),
                                fieldWithPath("basePrice").description("basePrice of new event"),
                                fieldWithPath("maxPrice").description("maxPrice of new event"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event"),
                                fieldWithPath("free").description("free of new event"),
                                fieldWithPath("offline").description("offline of new event"),
                                fieldWithPath("eventStatus").description("offline of new event"),
                                fieldWithPath("_links.self.href").description("offline of new event"),
                                fieldWithPath("_links.query-events.href").description("offline of new event"),
                                fieldWithPath("_links.update-event.href").description("offline of new event"),
                                fieldWithPath("_links.profile.href").description("profile of new event")
                        )
                ));

        ;
    }

    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }

    private String getAccessToken() throws Exception { // 액세스 토큰을 꺼내온다.

// Given
        Account sh = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        this.accountService.saveAccount(sh);

        ResultActions perfom = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret())) // OAuth 요청 헤더를 만들기 위해 httpBasic을 이용한다 (아래는 OAuth password 타입 인증에 필요한 정보를 헤더에 담아 요청함)
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));// httpBasic 사용시 spring security test를 추가해야 함 (clientId와 Secret을 이용하여 Basic Auth 헤더를 만들었다)

        var responseBody = perfom.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }


    // 원래는 이러한 테스트 코드를 먼저 작성하고 class를 작성해야 올바른 TDD.


    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception { // 위 방법과 다르게 입력값을 제한하는 방법 (Bad request 발생 시킴)
        // 만약 EventController에 파라메터가 EventDto이고 여기에 존재하지않는 필드가 입력값으로 들어올 경우
        // application.properties에 spring.jackson.deserialization.fail-on-unknown-properties=true; 값을 입력하여
        // 존재하지않는 필드(id, free, offline 등)이 들어오면 Bad Radrequest를 날린다..!
        Event event_v1 = Event.builder()
                .name("Srping")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Ganam")
                .id(100) // 이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다. -> EventController에 EventDto를 생성하여 파라메터로 받게하면 해당 값을 안받음
                .free(true) // 이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
                .offline(true) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
                .eventStatus(EventStatus.PUBLISHED) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
                .build();

        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)  // 요청에 JSON을 담아서 보내고 있다.
                .accept(MediaTypes.HAL_JSON) // HAL_JSON을 응답받기를 원한다. (HAL, Hypertext Application Language)
                .content(objectMapper.writeValueAsString(event_v1)) // 요청 본문에 Event 객체를 json형태로 변환해서 보냄
        ) // post에서 Servlet 생성 (MockMvc)
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력 값이 없는 경우 발생하는 에러")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우 발생하는 에러")
    public void createEvent_Bad_Request_Bad_Input() throws Exception { // 아래 테스트는 위의 empty 테스트와 같은 @Vaild + EventDto JSR 303 방식으로 해결이 안된다.
        EventDto eventDto = EventDto.builder()
                .name("Srping")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
                .basePrice(100)
                .maxPrice(20)
                .limitOfEnrollment(100)
                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
                .location("Ganam")
//                .id(100) // 이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다. -> EventController에 EventDto를 생성하여 파라메터로 받게하면 해당 값을 안받음
//                .free(true) // 이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
//                .offline(true) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
//                .eventStatus(EventStatus.PUBLISHED) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON) //Json 형태로 보낸다 (그렇기 때문에 ObjectMapper가 EventDto 객체를 Json으로 변환해서 본문에 넣어보냄
                .content(this.objectMapper.writeValueAsString(eventDto))) //objectMapper 내부에 BeanSerialize를 사용해서 bean 형태의 eventDto를 json으로 변환해줌
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
//                .andExpect(jsonPath("$[0].filed").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
//                .andExpect(jsonPath("$[0].rejectedValue").exists())
                .andExpect(jsonPath("_links.index").exists()) // Error발생시 EventController에서는 ErrorResource를 리턴하는데
        // links.index는 ErrorResource 생성시 추가되는 링크이고 우리는 에러 발생시 해당 링크를 통해 /api로 이동할 수 있다.
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvent() throws Exception {
        // Given
        IntStream.range(0, 30).forEach(i -> {
            this.generateEvent(i); // IntStream.range : for문과 동일, 이벤트 번호를 순회하면서 요청
        });

        // When & Then
        this.mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC")) // 0부터 1페이지 (1은 2페이지)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists()) // _embedded 내부ㅢ eventList 0번째 link가 존재하는지?
                .andExpect(jsonPath("_links.self").exists()) // _links.self가 존재하는지?
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        ;
    }



    @Test
    @TestDescription("없는 이벤트를 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception {
        // When & Then
        this.mockMvc.perform(get("/api/events/111888"))
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @TestDescription("기존 이벤트 하나 조회하기")
    public void getEvent() throws Exception {
        // Given
        Event event = this.generateEvent(100);

        //When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId())) // Path Variable에 event Id값을 넣어주는 의미
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"))
        ;

    }

//    @Test
//    @TestDescription("수정하는 데이터가 정상일때")
//    public void editEvent201() throws Exception {
//
//        EventEditDto event = EventEditDto.builder()
//                .name("Srping Changed Name")
//                .description("REST API Development with Spring")
//                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
//                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
//                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
//                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
//                .basePrice(200)
//                .maxPrice(300)
//                .limitOfEnrollment(100)
//                .location("Ganam")
//                .free(true)
//                .offline(true)
//                .eventStatus(EventStatus.PUBLISHED) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
//                .build();
//
//
//        Event testEvent = Event.builder()
//                .name("Srping")
//                .description("REST API Development with Spring")
//                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
//                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
//                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
//                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
//                .basePrice(200)
//                .maxPrice(300)
//                .limitOfEnrollment(100)
//                .location("Ganam")
//                .free(true)
//                .offline(true)
//                .eventStatus(EventStatus.PUBLISHED)
//                .build();
//
//
//        Event eventResult = this.eventRepository.save(testEvent);
//        System.out.println("TEST : " + eventResult.getId());
//
//        //When & THen
//        mockMvc.perform(put("/api/events/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaTypes.HAL_JSON)
//                .content(objectMapper.writeValueAsString(event)))
//                .andDo(print())
//                .andExpect(status().isOk());
//    }

//    @Test
//    @TestDescription("수정하려는 이벤트가 없는 경우")
//    public void editEvent404() throws Exception {
//        // When & Then
//        this.mockMvc.perform(put("/api/events/11111"))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @TestDescription("입력하는 데이터가 이상한 경우")
//    public void editEvent400() throws Exception {
//
//        EventEditDto event = EventEditDto.builder()
//                .name("Srping")
//                .description("REST API Development with Spring")
//                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
//                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
//                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
//                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
//                .basePrice(200)
//                .maxPrice(100)
//                .limitOfEnrollment(100)
//                .location("Ganam")
//                .free(true)
//                .offline(true)
//                .eventStatus(EventStatus.PUBLISHED) //이값은 계산되어서 백단에서 입력되는 값으로 입력되면 안된다.
//                .build();
//
//
//        Event testEvent = Event.builder()
//                .name("Srping")
//                .description("REST API Development with Spring")
//                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
//                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
//                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
//                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
//                .basePrice(200)
//                .maxPrice(100)
//                .limitOfEnrollment(100)
//                .location("Ganam")
//                .free(true)
//                .offline(true)
//                .eventStatus(EventStatus.PUBLISHED)
//                .build();
//
//
//        Event eventResult = this.eventRepository.save(testEvent);
//        System.out.println("TEST : " + eventResult.getId() );
//
//        //When & THen
//        mockMvc.perform(put("/api/events/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaTypes.HAL_JSON)
//                .content(objectMapper.writeValueAsString(event)))
//                .andDo(print())
//                .andExpect(status().isBadRequest());
//    }



    @Autowired
    ModelMapper modelMapper; // event에서 eventDto를 만들어내기 위함

    @Test
    @TestDescription("이벤트를 수정하기")
    public void updateEvent() throws Exception {
        // Given
        Event event = this.generateEvent(200); // 테스트를 위해 미리 이벤트를 만들어놓기 Generated Id 값을 같는 Event 객체가 생성되어 DB에 저장됨
        EventDto eventDto = modelMapper.map(event, EventDto.class); // modelMapper를 사용해서 event 객체를 EventDto 객체로 변환
        // modelMapper는 Event 객체인 event가 가지고있는 값을 사용해서 EventDto 객체로 만들어준다.
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        // When & then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON) // 요청은 Json 형태이다.
                .content(this.objectMapper.writeValueAsString(eventDto))) // 요청 본문에 eventDto 객체를 json 형태로 변환하여 보낸다.
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName)) // jsonPath에 name의 값이 eventName인지?
                .andExpect(jsonPath("_links.self").exists());
    }


    @Test
    @TestDescription("이벤트 수정 입력값이 비어있는 경우") // 입력값이 잘못되는 경우는 2가지다 일반적인 오류 (값이 없다거나..)  이건 Errors에서 기본적으로 제공해주는 기능으로
    // EventContrller에서 적용할 수 있음
    // 로직상의 오류 (비즈니스 로직에 따라.. max 값이 price보다 작다거나.. 이건
    // EventDto에 Java JSR 표기를 이용하고 @Valid 옵션을 사용해서 적용할수 있음
    public void updateEvent400_Empty() throws Exception {

        // Given
        Event event = generateEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        eventDto.setName("");


        // When & then
        String eventName = "Updated Event";
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("이벤트 수정 입력값이 잘못된 경우") // 입력값이 잘못되는 경우는 2가지다 일반적인 오류 (값이 없다거나..)  이건 Errors에서 기본적으로 제공해주는 기능으로
    // EventContrller에서 적용할 수 있음
    // 로직상의 오류 (비즈니스 로직에 따라.. max 값이 price보다 작다거나.. 이건
    // EventDto에 Java JSR 표기를 이용하고 @Valid 옵션을 사용해서 적용할수 있음
    public void updateEvent400_Wrong() throws Exception {

        // Given
        Event event = generateEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000); // basePrice는 maxPrice를 넘길 수 없음 -> 여기서 로직의 에러 발생생
        eventDto.setMaxPrice(100);


       // When & then
        String eventName = "Updated Event";
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @TestDescription("이벤트 수정 ID가 존재하지 않는 경우")
    public void updateEvent404_NotFound() throws Exception {

        // Given
        Event event = generateEvent(200); // 값은 생성하지만 ID값을 다르게해서 NotFound 에러를 발생시키고자 함
        EventDto eventDto = modelMapper.map(event, EventDto.class);

        // When & then
        String eventName = "Updated Event";
        this.mockMvc.perform(put("/api/events/123124")
                .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("Srping")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2021, 11, 23, 14, 21))
                .closeEnrollmentDateTime(LocalDateTime.of(2021, 11, 24, 14, 21))
                .beginEventDateTime(LocalDateTime.of(2021, 11, 25, 14, 21))
                .endEventDateTime(LocalDateTime.of(2021, 11, 26, 14, 21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("Ganam")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();
        this.eventRepository.save(event);
        return event;
    }
}
