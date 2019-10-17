package io.matel.repository;

import io.matel.model.User;
import io.matel.model.Vocab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabRepository extends JpaRepository<Vocab, Long> {
    Vocab findByName(String name);
}
