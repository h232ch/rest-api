package com.h232ch.restapi.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Account saveAccount(Account account) {
        account.setPassword(this.passwordEncoder.encode(account.getPassword()));
        return this.accountRepository.save(account);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // 사용자 인증을 수행하는 UserDetailsService를 구현
        // Account 객체를 Optional로 가져와서 orElseThrow 설정을 해줌
        // Spring Security의 User 객체를 이용해서 우리가 만든 Account 정보를 넣어준다. 단 Role 정보는 Collection<? extends GrantedAutority>로 받아와야 하기 때문에
        // 별도의 메서드를 생성하여 Set에 넣어둔 Roels를 map 형태로 불러온 뒤 ROLE_ADMIN, ROME_USER 문구를 생성하여 다시 Set 형태로 넣어서 반환한다.
        // 아래의 기능으로 테스트를 하면 테스트 코드에서 저장한 email, password를 UserDetail 객체에서 확인 가능함

        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new User(account.getEmail(), account.getPassword(), authorities(account.getRoles()));
    }

    private Collection<? extends GrantedAuthority> authorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                .collect(Collectors.toSet());
    }
}
