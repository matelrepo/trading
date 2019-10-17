package io.matel.assistant.model;


import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Parameter;


import javax.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Indexed
@AnalyzerDef(name = "customanalyzer1",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = SnowballPorterFilterFactory.class,
                        params = { @Parameter(name = "language", value = "English")
                })
        })
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Field
    @Analyzer(definition = "customanalyzer1")
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
