package com.h232ch.restapi.events;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE) // 이 클래스 내부 모든 핸들러의 응답은 HAL_JSON Content type으로 응답을 보냄
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
            return ResponseEntity.badRequest().body(errors); // ResponseEntity에 BadRequest를 담아 리턴
        }

        eventValidator.vaildate(eventDto, errors);

        if (errors.hasErrors()) { // @Vaild 검증시 에러가 발생한다면?
            return ResponseEntity.badRequest().body(errors); // ResponseEntity에 BadRequest를 담아 리턴
        }

        Event event = modelMapper.map(eventDto, Event.class); // eventDto를 Event 객체로 컨버팅

        event.update(); // 유무료 여부를 확인해서 변경해줘야 함 (이 코드는 서비스 코드에 가깝다)

        Event newEvent = this.eventRepository.save(event);
//        URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri(); // PostMapping에 URL을 생성할 떄
//        URI createdUri = linkTo(EventController.class).slash("{id}").toUri(); // @RequestMapping에 URL을 생성할 떄
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri(); // newEvent에서 getID로 id 값을 가져옴 (id는 자동으로 generated된 값이 불러와짐)
        //  위의 헤더를 가지고  201 응답이 생성됨
//        return ResponseEntity.created(createdUri).body(event); // 헤더에는 LinkTO에 입력된 값이 들어가고, 응답 본문에는 Event 객체가 Json 형태로 보여진다.
        // EventResource를 사용하는 hateaos 예제이다. (헤이토스를 이용해서 linkTo 기능을 사용해 응답하기 위해 EventResource 클래스에 RepresentationModel을 상속받고 Event 객체를 만들어
        // 아래와 같이 LinkTo 기능을 이용해서 응답값에 릴레이션과 링크를 추가해주는 기능을 사용한다.
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events")); // 이벤트 쿼리 링크
        eventResource.add(selfLinkBuilder.withSelfRel()); // 셀프 링크
        eventResource.add(selfLinkBuilder.withRel("update-event")); // 수정 쿼리 링크 (Put 요청)

        return ResponseEntity.created(createdUri).body(eventResource); // 헤더에는 LinkTO에 입력된 값이 들어가고, 응답 본문에는 Event 객체가 Json 형태로 보여진다.
    }
}
