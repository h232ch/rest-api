package com.h232ch.restapi.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE) // 이 클래스 내부 모든 핸들러의 응답은 HAL_JSON Content Type으로 응답을 보냄
public class EventController {

//    @Autowired
//    EventRepository eventRepository; // DI로 주입받거나 아래처럼 생성자로 생성 가능

    private final EventRepository eventRepository;

    @Autowired // 생성자가 1개이고 생성자로 받아올 파라메터가 빈으로 등록되어있다면 Autowired는 생략할수 있다. 스프링 4.3부터
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }


    @PostMapping
    public ResponseEntity createEvent(@RequestBody Event event) { // ResponseEntity는 응답 상태코드, 헤더 본문을 리턴할 때 사용
        Event newEvent = this.eventRepository.save(event);
//        URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri(); // PostMapping에 URL을 생성할 떄
//        URI createdUri = linkTo(EventController.class).slash("{id}").toUri(); // @RequestMapping에 URL을 생성할 떄
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri(); // newEvent에서 getID로 id 값을 가져옴 (id는 자동으로 generated된 값이 불러와짐)
        //  위의 헤더를 가지고  201 응답이 생성됨
        return ResponseEntity.created(createdUri).body(event); // 헤더에는 LinkTO에 입력된 값이 들어가고, 응답 본문에는 Event 객체가 Json 형태로 보여진다.
    }
}
