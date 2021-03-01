package com.h232ch.restapi.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class EventTest {

    @Test
    public void builder() {
        Event event = Event.builder() // 프로덕 클래스에 @Builder 애노테이션 사용시 builder 사용 가능
                .name("Spring REST API")
                .description("REST API Development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() {
        // Given
        String name = "Event";
        String description = "Spring";

        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);

    }

    @Test // JUnitPrams를 사용하면 아래와 같이 코드를 줄일 수 있음 (Prameters 인자는 testFree 매개변수를 의미하며 테스트시 3개의 경우를 모두 테스트함)
//    @Parameters(method = "parametersForTestFree")
    @Parameters
// 아래는 TypeSafe하지 않은 방법
//            ({
//            "0, 0, true",
//            "100, 0, false",
//            "0, 100, false"
//    })
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // Given
        Event event = Event.builder()
                .basePrice(basePrice) // 매개변수를 받아서 테스트
                .maxPrice(maxPrice)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isEqualTo(isFree); // 세번째 매개변수인 isFree와 동일한지 판단 (isFree가)
    }

    // 아래는 TypeSafe한 방법
    private Object[] parametersForTestFree() { // parametersFor "TestFree" 는 prefix 이름만으로 TestFree를 찾아가줌. (prameter에 method 생략 가능)
        return new Object[] {
                new Object[] {0, 0, true},
                new Object[] {100, 0, false},
                new Object[] {0, 100, false},
                new Object[] {100, 100, false}
        };
    }




    @Test
    @Parameters
    public void testOffline(String location, boolean isOffline) {
        // Given
        Event event = Event.builder()
                .location(location)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isEqualTo(isOffline);
//
//        // Given
//        event = Event.builder()
//                .build();
//
//        // When
//        event.update();
//
//        // Then
//        assertThat(event.isOffline()).isFalse();
    }

    private Object[] parametersForTestOffline() {
        return new Object[]{
                new Object[]{"강남", true},
                new Object[]{null, false},
                new Object[]{"      ", false}
        };
    }

}