package io.matel.app.config.connection.activeuser;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users_activity")
public class ActiveUserEvent {

    private static final Logger LOGGER = LogManager.getLogger(ActiveUserEvent.class);

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String type;
    private String username;
    private String eventSessionId;
    private ZonedDateTime timestamp;

    public ActiveUserEvent(String type, String username, String eventSessionId) {
        this.type = type;
        this.username = username;
        this.eventSessionId = eventSessionId;
        timestamp= ZonedDateTime.now();
        LOGGER.log(Level.INFO,this.toString());
    }

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public String getEventSessionId() {
        return eventSessionId;
    }

    public long getId() {
        return id;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "UserActivityEvent{" +
                "type='" + type + '\'' +
                ", username='" + username + '\'' +
                ", eventSessionId='" + eventSessionId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }


}