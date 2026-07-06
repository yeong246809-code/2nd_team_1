package org.example.k_market.security;

import lombok.Builder;
import lombok.Data;
import org.example.k_market.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class MyUserDetails implements UserDetails {

    private Users user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        // DB의 role 컬럼 값(ADMIN) 앞에 ROLE_ 을 붙여서 반환
        authorityList.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        return authorityList;
    }

    @Override
    public String getPassword() {
        return user.getPass();
    }

    @Override
    public String getUsername() {
        return user.getId();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
