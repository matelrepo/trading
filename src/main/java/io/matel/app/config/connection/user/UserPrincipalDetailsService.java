package io.matel.app.config.connection.user;

import io.matel.app.config.connection.activeuser.ActiveUserEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserPrincipalDetailsService implements UserDetailsService {
    private UserRepository userRepository;
    private static final Logger LOGGER = LogManager.getLogger(ActiveUserEvent.class);


    public UserPrincipalDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(s);
        if(user == null) {
            LOGGER.warn("User " + s + " not found");
            throw new UsernameNotFoundException("User " + s + " not found");
        }
            UserPrincipal userPrincipal = new UserPrincipal(user);
            return userPrincipal;
    }
}
