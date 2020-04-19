package io.matel.app.config.connection.activeuser;

import io.matel.app.AppController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Service
public class ActiveUserMonitoring {

    private ActiveUserRepository activeUserRepository;

    @Autowired
    private AppController appController;

    public ActiveUserMonitoring(ActiveUserRepository activeUserRepository) {
        this.activeUserRepository = activeUserRepository;
    }

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String username = headers.getUser().getName();
        ActiveUserEvent activeUserEvent = new ActiveUserEvent("login", username, headers.getSessionId());

        // We store the session as we need to be idempotent in the disconnect event processing
        appController.getActiveUsers().put(headers.getSessionId(), activeUserEvent);
        activeUserRepository.save(activeUserEvent);
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        Optional.ofNullable(appController.getActiveUsers().get(event.getSessionId()))
                .ifPresent(login -> {
                    ActiveUserEvent activeUserEvent = new ActiveUserEvent("logout", login.getUsername(), event.getSessionId());
                    appController.getActiveUsers().remove(event.getSessionId());
                    activeUserRepository.save(activeUserEvent);

                });
    }

}