package io.matel.common.service;

import io.matel.assistant.model.Task;
import io.matel.student.model.Vocab;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;


@Service
public class SearchService {

    private final EntityManager entityManager;

    @PersistenceContext
    private EntityManager em;

    public SearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public List searchConfig(String request, String field, Class myClass){
        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // create native Lucene query using the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though
        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(myClass).get();
        org.apache.lucene.search.Query luceneQuery = qb
                .keyword()
                .onFields(field)
                .matching(request)
                .createQuery();

// wrap Lucene query in a javax.persistence.Query
        javax.persistence.Query jpaQuery =
                fullTextEntityManager.createFullTextQuery(luceneQuery, myClass);

// execute search
        List result = jpaQuery.getResultList();

        return result;
    }

    @Transactional
    public List search(String request, String field, Class myClass) {
           return searchConfig(request, field, myClass);
    }

    }

