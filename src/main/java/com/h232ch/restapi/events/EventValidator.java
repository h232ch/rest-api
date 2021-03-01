package com.h232ch.restapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Component
public class EventValidator {

    public void vaildate(EventDto eventDto, Errors errors) {
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() != 0) {
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");  // 이 에러는 errors 필드 에러에 입력됨
            errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
            errors.reject("wrongPrices","Prices is wrong"); // 이 에러는 errors 글로벌 에러에 입력됨
        }

        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        if (endEventDateTime.isBefore(eventDto.getEndEventDateTime()) ||
        endEventDateTime.isBefore(eventDto.getCloseEnrollmentDateTime()) ||
        endEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("endEventDateTime", "wrongValue", "EndEventDateTime is wrong");
        }

        // TODO: 2/23/2021 나머지 데이터들도 검증해야 함
    }
}
