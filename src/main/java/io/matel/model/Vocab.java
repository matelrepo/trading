package io.matel.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vocab")
public class Vocab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String definition;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String level;

    public Vocab(){}

    public Vocab(String name, String definition, String subject, String level) {
        this.name = name;
        this.definition = definition;
        this.subject = subject;
        this.level = level;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    public String getSubject() {
        return subject;
    }

    public String getLevel() {
        return level;
    }
}
