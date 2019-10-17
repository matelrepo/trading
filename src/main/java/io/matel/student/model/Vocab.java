package io.matel.student.model;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Parameter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Indexed
@AnalyzerDef(name = "customanalyzer2",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = SnowballPorterFilterFactory.class,
                        params = { @Parameter(name = "language", value = "English")
                        })
        })
@Table(name = "vocab")
public class Vocab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Field
    @Analyzer(definition = "customanalyzer2")
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
    public void setVocabName(String name){
        this.name = name;
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

    @Override
    public String toString() {
        return "Vocab{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", definition='" + definition + '\'' +
                ", subject='" + subject + '\'' +
                ", level='" + level + '\'' +
                '}';
    }
}
