package com.h232ch.restapi.events;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.linkTo;

//public class EventResource extends RepresentationModel {
public class EventResource extends EntityModel<Event> {

//    @JsonUnwrapped // EventResource를 리턴할 때 HttpMessageConverter의 ObjectMapping을 통해 Json 형태로 시리얼라이제이션(객체를 Json 변환)이 이뤄지는데
//    // 이때 사용하는 게 BeanSerialization이다. BeanSerializer는 기본적으로 컴포직 객체(오브젝트형 객체)를 변환하는 경우 객체 이름으로 랩핑하여 변환이 이뤄짐
//    // {"event":{"id":1,"name":"Srping","description":"REST.... 이런식으로, 이것을 언랩퍼해주는 애노테이션이 JsonUnwrapped이다.
//    private Event event;
//
//    public EventResource(Event event) {
//        this.event = event;
//    }
//
//    public Event getEvent() {
//        return event;
//    }

    // 아래의 방법으로 진행하면 더 간단하게 가능하다.


    public EventResource(Event event, Link... links) {
        super(event, links);
//        add(new Link("http://loalhost:8080/api/events" + event.getId())); // 이것과 동일함(아래) 하지만 타입세이프하지않고 Controller의 RequestMapping 주소 변경시 일일히 변경해야함
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}
