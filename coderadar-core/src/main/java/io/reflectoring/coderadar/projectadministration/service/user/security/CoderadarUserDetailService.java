package io.reflectoring.coderadar.projectadministration.service.user.security;

import io.reflectoring.coderadar.projectadministration.domain.User;
import io.reflectoring.coderadar.projectadministration.port.driven.user.LoadUserPort;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CoderadarUserDetailService implements UserDetailsService {

  private final LoadUserPort loadUserPort;

  @Autowired
  public CoderadarUserDetailService(LoadUserPort loadUserPort) {
    this.loadUserPort = loadUserPort;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = loadUserPort.loadUserByUsername(username);
    // TODO add authorities to user
    List<SimpleGrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("USER"));
    return new CoderadarUserDetails(
        user.getUsername(), user.getPassword(), authorities, true, true, true, true);
  }
}
