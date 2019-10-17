package io.matel.repository;

import io.matel.model.Task;
import io.matel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Task findByTaskName(String name);

}
