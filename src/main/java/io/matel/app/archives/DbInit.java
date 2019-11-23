//package io.matel.app;
//
//import io.matel.app.repo.ContractRepository;
//import io.matel.app.connection.users.security.config.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class DbInit implements CommandLineRunner {
//    private UserRepository userRepository;
////    private TaskRepository taskRepository;
////    private VocabRepository vocabRepository;
//    private ContractRepository contractRepository;
//
//    private PasswordEncoder passwordEncoder;
//
//    public DbInit(UserRepository userRepository, ContractRepository contractRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
////        this.taskRepository = taskRepository;
////        this.vocabRepository = vocabRepository;
//        this.contractRepository = contractRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(String... args) {
//
////        ContractBasic contract = new ContractBasic( 1,  "SPX",  "FUT",  "GLOBEX",  "USD",  "ES",  0.25,  2,  "50",
////                 "2019",  "2019",  true, "TRADES", 0);
////        this.contractRepository.save(contract);
//
////        User trader = new User("trader",passwordEncoder.encode("trader123"),"TRADER","");
////        User student = new User("student",passwordEncoder.encode("student123"),"STUDENT","");
////        User assistant = new User("assistant",passwordEncoder.encode("assistant123"),"ASSISTANT","");
////        User matel = new User("matel",passwordEncoder.encode("mat"),"TRADER, STUDENT, ASSISTANT","");
////        List<User> users = Arrays.asList(trader,student,assistant,matel);
////        this.userRepository.saveAll(users);
//
//
////
////        Task task1 = new Task("Refactoring: Improving the Design of Existing Code",  OffsetDateTime.now(), 1);
////        Task task2 = new Task("Study thai",  OffsetDateTime.now(), 1);
////        Task task3 = new Task("Maths papers",  OffsetDateTime.now(), 1);
////        List<Task> tasks = Arrays.asList(task1, task2, task3);
////        this.taskRepository.saveAll(tasks);
//
////        Vocab vocab1 = new Vocab("def1", "This is a definition 1", "Business", "IGCSE");
////        Vocab vocab2 = new Vocab("def2", "This is a definition 2", "Business", "IB");
////        Vocab vocab3 = new Vocab("def3", "This is a definition 3", "Business", "IGCSE");
////        List<Vocab> vocabs = Arrays.asList(vocab1, vocab2, vocab3);
////        this.vocabRepository.saveAll(vocabs);
//    }
//}
