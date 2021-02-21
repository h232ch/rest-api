package com.h232ch.restapi;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApiApplication.class, args);
    }

    @Bean // 스프링 IoC에 빈으로 직접 등록해줌 (Bean으로 직접 등록하는 방법)
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
