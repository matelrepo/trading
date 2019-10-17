package io.matel.student.repository;

import io.matel.student.model.Vocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabRepository extends JpaRepository<Vocab, Long> {
    Vocab findByName(String name);
}
