package io.matel.common.db;

import io.matel.assistant.model.Task;
import io.matel.student.model.Vocab;
import io.matel.assistant.repository.TaskRepository;
import io.matel.common.repository.UserRepository;
import io.matel.student.repository.VocabRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import io.matel.common.model.User;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DbInit implements CommandLineRunner {
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private VocabRepository vocabRepository;

    private PasswordEncoder passwordEncoder;

    public DbInit(UserRepository userRepository, TaskRepository taskRepository, VocabRepository vocabRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.vocabRepository = vocabRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        this.userRepository.deleteAll();
        User dan = new User("dan",passwordEncoder.encode("dan123"),"USER","READ");
        User admin = new User("admin",passwordEncoder.encode("admin123"),"ADMIN","READ, WRITE, ACCESS_TEST1,ACCESS_TEST2");
        User manager = new User("manager",passwordEncoder.encode("manager123"),"MANAGER","READ, WRITE, ACCESS_TEST1");
        List<User> users = Arrays.asList(dan,admin,manager);
        this.userRepository.saveAll(users);

        this.taskRepository.deleteAll();

        Task task1 = new Task("Refactoring: Improving the Design of Existing Code",  OffsetDateTime.now(), 1);
        Task task2 = new Task("Study thai",  OffsetDateTime.now(), 1);
        Task task3 = new Task("Maths papers",  OffsetDateTime.now(), 1);
        List<Task> tasks = Arrays.asList(task1, task2, task3);
        this.taskRepository.saveAll(tasks);

        this.vocabRepository.deleteAll();
        Vocab vocab1 = new Vocab("def1", "This is a definition 1", "Business", "IGCSE");
        Vocab vocab2 = new Vocab("def2", "This is a definition 2", "Business", "IB");
        Vocab vocab3 = new Vocab("def3", "This is a definition 3", "Business", "IGCSE");
        List<Vocab> vocabs = Arrays.asList(vocab1, vocab2, vocab3);
        this.vocabRepository.saveAll(vocabs);
    }
}
