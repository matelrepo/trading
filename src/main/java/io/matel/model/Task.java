package io.matel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String taskName;

    @Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expiration;

    @Column(nullable = false)
    private int idowner;

    public Task(){}

    public Task(String taskName, OffsetDateTime expiration, int idowner) {
        this.taskName = taskName;
        this.expiration = expiration;
        this.idowner = idowner;
    }

    public long getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String name){
        this.taskName = name;
    }

    public OffsetDateTime getExpiration() {
        return expiration;
    }

    public int getIdowner() {
        return idowner;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", expiration=" + expiration +
                ", idowner=" + idowner +
                '}';
    }
}
