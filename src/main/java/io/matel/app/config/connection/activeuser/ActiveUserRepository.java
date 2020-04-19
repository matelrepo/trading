package io.matel.app.config.connection.activeuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveUserRepository extends JpaRepository<ActiveUserEvent, Long> {

//    private Map<String, ActiveUserEvent> activeUsers = new ConcurrentHashMap<>();
//
//    public void add(String sessionId, ActiveUserEvent event) {
//        activeUsers.put(sessionId, event);
//    }
//
//    public ActiveUserEvent getParticipant(String sessionId) {
//        return activeUsers.get(sessionId);
//    }
//
//    public void removeParticipant(String sessionId) {
//        activeUsers.remove(sessionId);
//    }
//
//    public Map<String, ActiveUserEvent> getActiveUsers() {
//        return activeUsers;
//    }

}