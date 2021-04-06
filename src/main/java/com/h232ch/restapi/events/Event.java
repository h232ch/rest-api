package com.h232ch.restapi.events;

import com.h232ch.restapi.accounts.Account;
import lombok.*;

import javax.persistence.*;
import java.security.cert.CertPathBuilder;
import java.time.LocalDateTime;

@Builder // 롬복 애노테이션 (컴파일시 builder에 대한 메서드를 생성해줌, 원래라면 builder 메서드를 만들어야 함)
@AllArgsConstructor // 모든 필드를 갖는 생성자
@NoArgsConstructor // 아무 필드도 갖지않는 생성자
@Getter @Setter // 모든 필드에 게터, 세터를 생성 (자바빈 형태)
@EqualsAndHashCode(of = "id") // equals(두 객체의 내용이 같은지?), hashcode(두 객체가 같은 객체인지?) 자동 생성
// 자바 Bean에서 동등성 비교를 위해 equals와 hashcode 메소드를 오버라이딩해서 사용하는데 위 애노테이션을 쓰면 자동으로 해당 메서드가 생성되어 사용 가능
// of = "id" 라면 id 필드로 객체를 비교하는 메서드 생성
// Lombok 애노테이션은 메타애노테이션으로 묶는것이 아직은 어려움 (스프링 기본 애노테이션은 묶을수 있음)
@Entity
public class Event {

    @Id @GeneratedValue // 엔티티의 Primary Key 역할을 id으로 정해주고 자동으로 생성되도록 함
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;
    @Enumerated(EnumType.STRING) // 기본값이 ODINAL인데 이는 Enum 요소의 숫자값으로 표현함 -> 추후 데이터 변경시 꼬일수있기에 STRING을 추천함
    private EventStatus eventStatus = EventStatus.DRAFT;

    @ManyToOne
    private Account manager; // 이벤트에서만 account를 참조할 수 있도록 만들어줌


    public void update() {
        // Update free
        if (this.basePrice == 0 && this.maxPrice == 0) {
            this.free = true;
        } else {
            this.free = false;
        }

        if (this.location == null || this.location.isBlank()) { // Blank는 자바 11에 추가됨 (Location이 비어있는지?) 공백까지 확인해줌
            this.offline = false;
        } else {
            this.offline = true;
        }
    }
}
