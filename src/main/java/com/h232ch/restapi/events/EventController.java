package com.h232ch.restapi.events;

import com.h232ch.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE) // 이 클래스 내부 모든 핸들러의 응답은 HAL_JSON Content type으로 응답을 보냄
public class EventController {

//    @Autowired
//    EventRepository eventRepository; // DI로 주입받거나 아래처럼 생성자로 생성 가능

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper; // @bean으로 등록한 ModelMapper 빈을 불러옴 * ModelMapper의 역할은? 메세지 컨버팅 역할로 json을 객체로 deserialize
    private final EventValidator eventValidator;

    @Autowired // 생성자가 1개이고 생성자로 받아올 파라메터가 빈으로 등록되어있다면 Autowired는 생략할수 있다. 스프링 4.3부터
    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository; // IoC를 구현하기 위해 DI가 사용됨 (객체는 외부(Spring Context에)에 존재하고 주입받아 사용
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
//            return ResponseEntity.badRequest().body(errors); // ResponseEntity에 BadRequest를 담아 리턴 (Error를 그대로 받아 리턴)
            return badRequest(errors); // ErrorResource에 Error를 담아서 리턴 (ErrorResource에는 /api로 가는 index 링크가 추가되어 있음
        }

        eventValidator.vaildate(eventDto, errors);

        if (errors.hasErrors()) { // @Vaild 검증시 에러가 발생한다면?
//            return ResponseEntity.badRequest().body(errors); // ResponseEntity에 BadRequest를 담아 리턴
            return badRequest(errors); // badRequest 메서드를 생성하여 리턴 (ResourceError
        }

        Event event = modelMapper.map(eventDto, Event.class); // eventDto를 Event 객체로 컨버팅 (json -> 객체 Deserialize, 객체 -> json serialize)
        event.update(); // 유무료 여부를 확인해서 변경해줘야 함 (이 코드는 서비스 코드에 가깝다) 

        Event newEvent = this.eventRepository.save(event); // 요청본문의 eventDto를 event객체로 변환하고, event를 eventRepository를 통해 저장함
//        URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri(); // PostMapping에 URL을 생성할 떄
//        URI createdUri = linkTo(EventController.class).slash("{id}").toUri(); // @RequestMapping에 URL을 생성할 떄
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri(); // newEvent에서 getID로 id 값을 가져옴 (id는 자동으로 generated된 값이 불러와짐)
        //  위의 헤더를 가지고  201 응답이 생성됨
//        return ResponseEntity.created(createdUri).body(event); // 헤더에는 LinkTO에 입력된 값이 들어가고, 응답 본문에는 Event 객체가 Json 형태로 보여진다.
        // EventResource를 사용하는 hateaos 예제이다. (헤이토스를 이용해서 linkTo 기능을 사용해 응답하기 위해 EventResource 클래스에 RepresentationModel을 상속받고 Event 객체를 만들어
        // 아래와 같이 LinkTo 기능을 이용해서 응답값에 릴레이션과 링크를 추가해주는 기능을 사용한다.
        EventResource eventResource = new EventResource(event); // event 객체를 json으로 리턴하기위해 serialization함
        eventResource.add(linkTo(EventController.class).withRel("query-events")); // eventResource에 이벤트 쿼리 링크 추가
//        eventResource.add(selfLinkBuilder.withSelfRel()); // 셀프 링크는 EventResource에서 추가해주는게 편하다 (컨트롤로에 구성시 매번 추가해야 함)
        // linkTo로 추가되는 항목은 웬만하면 EventResource에 추가해주는게 좋음
        eventResource.add(selfLinkBuilder.withRel("update-event")); // eventResource에 수정 쿼리 링크 추가 (Put 요청)
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile")); // profile 추가

        return ResponseEntity.created(createdUri).body(eventResource); // 헤더에는 LinkTO에 입력된 값이 들어가고, 응답 본문에는 Event 객체가 Json 형태로 보여진다.
    }

    @GetMapping
    public ResponseEntity queryEvent(Pageable pageable, PagedResourcesAssembler<Event> assembler) { // pagealbe은 페이지 사이즈, 솔트 등의 정보들을 입력받을 수 있음
        Page<Event> page = this.eventRepository.findAll(pageable); // 리파시토리에서 가져온 pageable 데이터를
        var pagedModel = assembler.toModel(page, e -> new EventResource(e)); // 모델로 변경하여 넘겨줌 (변경시 각 페이지에 링크정보를 넘겨줌 (다음페이지, 이전페이지 등)
        // e -> new EventResource(e)를 붙이면 각 응답에 대한 링크도 넘겨줌
        pagedModel.add(new Link("/docs/index.html#resources-events-list").withRel("profile")); // 프로파일 링크를 추가함
        // 프로파일 링크가 추가되고 테스트에서 REST 문서를 생성할 수 있음
        return ResponseEntity.ok(pagedModel); // ResponseEntity의 바디에 넣어줌 (pageable을 모두 찾는다?)
        // 모델형태의 응답은 뷰에서 응답할 때 json 객체로 다시 변환한다.

    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) { // errors를 받아서 본문에 넣어주는데 이를 리소스로 변환하고
        return ResponseEntity.badRequest().body(new ErrorsResource(errors)); // 리소스를 변환할 때 인덱스를 추가하도록 함 (ErrorResource 생성자)
    }


    @GetMapping("{id}") // Id에 대한 Path variable을 받는다.
    public ResponseEntity getEvent(@PathVariable Integer id) { // PathVariable 변수를 id 파라메터로 받는다.
        Optional<Event> optionalEvent = this.eventRepository.findById(id); // 레파시토리에서 id에 해당하는 객체를 가저와서 Optional 객체에 넣는다.
        if (optionalEvent.isEmpty()) { // 만약 객체가 존재하지 않는다면?
            return ResponseEntity.notFound().build(); // 404 응답
        }

        Event event = optionalEvent.get(); // 객체가 존재한다면 객체를 가져와서 Event 객체에 넣어준다.
        EventResource eventResource = new EventResource(event); // 가져온 이벤트 객체를 EventResource 객체로 만들어 Self 링크를 추가하도록 한다.
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile")); // 더해서 Profile 링크도 추가하고 테스트 코드에서 document를 만들어준다.
        return ResponseEntity.ok(eventResource); // 마지막으로 본문에 eventResource를 담아서 전달한다. (객체는 json 형태로 변환되어 응답됨)
    }

//    @PutMapping("{id}")
//    public ResponseEntity editEvent(@PathVariable Integer id, @RequestBody @Valid EventEditDto eventEditDto, Errors errors) {
//        Optional<Event> optionalEvent = eventRepository.findById(id);
//        if (optionalEvent.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        if (errors.hasErrors()) {
//            return badRequest(errors);
//        }
//
//        eventValidator.vaildateEdit(eventEditDto, errors);
//
//        if (errors.hasErrors()) {
//            return badRequest(errors);
//        }
//
//        Event event = modelMapper.map(eventEditDto, Event.class);
//        event.update();
//
//        optionalEvent.ifPresent(selectEvent -> {
//            selectEvent.setName(event.getName());
//            Event newEvent = eventRepository.save(selectEvent);
//        });
//
//
//
//        Optional<Event> editEvent = this.eventRepository.findById(id);
//        Event newEvent = editEvent.get();
//
//
//        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
//        URI createUri = selfLinkBuilder.toUri();
//
//        EventResource eventResource = new EventResource(newEvent);
//        eventResource.add(selfLinkBuilder.withRel("update-event"));
//
//        return ResponseEntity.ok(eventResource);
//    }

    @PutMapping("{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) { // 여기서는 EventDto에 명시된 조건에 부합하지 않는지를 찾아냄 (@Valid)
            return ResponseEntity.badRequest().build();
        }

        this.eventValidator.vaildate(eventDto, errors); // 여기서는 EventVaildator에 명ㅅ한 비즈니스 로직이 잘 수행되는지 확인함

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Event existingEvent = optionalEvent.get();
        this.modelMapper.map(eventDto, existingEvent); // 이미 존재하는 event 객체에 eventDto 객체의 값을 옮겨 닮아줌
        Event savedEvent = this.eventRepository.save(existingEvent);// 옮겨담은 객체를 저장한다 (ID값이 유니크하여 덮어씌워짐)

        EventResource eventResource = new EventResource(savedEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

}
