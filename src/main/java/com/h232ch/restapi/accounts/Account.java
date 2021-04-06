package com.h232ch.restapi.accounts;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity // 엔티티 객체로 사용하곘다는 의미
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue // @Id의 의미는 JPA의 Primary Key로 사용하겠다는 의미
    private Integer id;
    private String email;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // Set 객체에 대한 Fetch를 어떻게 할것인지 정한다.
    @Enumerated(EnumType.STRING)
    private Set<AccountRole> roles; // Enum 객체를 Set 자료구조로 받는다. (Set은 집합 자료구조)
}