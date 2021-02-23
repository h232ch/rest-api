package com.h232ch.restapi.events;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE) // 이 클래스 내부 모든 핸들러의 응답은 HAL_JSON Content Type으로 응답을 보냄
public class EventController {

//    @Autowired
//    EventRepository eventRepository; // DI로 주입받거나 아래처럼 생성자로 생성 가능

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper; // @bean으로 등록한 ModelMapper 빈을 불러옴

    private final EventValidator eventValidator;

    @Autowired // 생성자가 1개이고 생성자로 받아올 파라메터가 빈으로 등록되어있다면 Autowired는 생략할수 있다. 스프링 4.3부터
    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }



    @PostMapping
//    public ResponseEntity createEvent(@RequestBody Event event) { // ResponseEntity는 응답 상태코드, 헤더 본문을 리턴할 때 사용
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) { // 본문에 들어갈 객체를 eventDto로 변경 (Id, Free등 입력받지 않아아할 필드는 제외된 Dto로 해당 값은 입력불가)
        // @RequestBody : 요청본문에서 메세지를 읽어드림, @ResponseBody : 응답본문에서 메세지를 읽어드림
        // @Vaild 읽어들인 값을 검증함 -> EventDto에 조건에 따라 (@NotNull.....)

//        Event event = Event.builder() // eventDto로 파라메터를 받고 이후 Event 객체로 컨버팅해줘야 함 그러나 이는 번거롭기 때문에 modelMapper를 사용해서 컨버팅 작업 수행
//                .name(eventDto.getName())
//                .description(eventDto.getDescription())
//                .build();

        if (errors.hasErrors()) { // @Vaild 검증시 에러가 발생한다면?
            return ResponseEntity.badRequest().build(); // ResponseEntity에 BadRequest를 담아 리턴
        }

        eventValidator.vaildate(eventDto, errors);

        if (errors.hasErrors()) { // @Vaild 검증시 에러가 발생한다면?
            return ResponseEntity.badRequest().build(); // ResponseEntity에 BadRequest를 담아 리턴
        }

        Event event = modelMapper.map(eventDto, Event.class); // eventDto를 Event 객체로 컨버팅
        Event newEvent = this.eventRepository.save(event);
//        URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri(); // PostMapping에 URL을 생성할 떄
//        URI createdUri = linkTo(EventController.class).slash("{id}").toUri(); // @RequestMapping에 URL을 생성할 떄
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri(); // newEvent에서 getID로 id 값을 가져옴 (id는 자동으로 generated된 값이 불러와짐)
        //  위의 헤더를 가지고  201 응답이 생성됨
        return ResponseEntity.created(createdUri).body(event); // 헤더에는 LinkTO에 입력된 값이 들어가고, 응답 본문에는 Event 객체가 Json 형태로 보여진다.
    }
}
