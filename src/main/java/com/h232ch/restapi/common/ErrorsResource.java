package com.h232ch.restapi.common;

import com.h232ch.restapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsResource extends EntityModel<Errors> { // 헤이토스의 EntityModel을 상속받고 생성자에 Link IndexController의 인텍스를 추가함
    public ErrorsResource(Errors content, Link... links) { // EntityModel에 넣는 정보는 Errors 타입으로 에러시 해당 내용을 리턴함
        super(content, links);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index")); // Index로 가는 링크를 추가함 (link 정보 생성)
        // 에러 발생시 컨트롤러에서 ErrorResource를 생성하여 에러 정보를 담고 본문에 넣어서 리턴함 -> 에러발생시 index 링크 확인 가능
    }
}
