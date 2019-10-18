package io.matel.assistant;


import io.matel.common.SearchService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cal")
@CrossOrigin
public class TaskController {
    private TaskRepository taskRepository;
    private SearchService searchService;


    public TaskController(TaskRepository taskRepository, SearchService searchService) {
        this.taskRepository = taskRepository;
        this.searchService = searchService;
    }

    @GetMapping("tasks")
    public List<Task> getTasks() {
        List<Task> list =  this.taskRepository.findAll();
        list.sort((Task e1, Task e2) -> e1.getExpiration().compareTo(e2.getExpiration()));
        return list;
    }

    @PostMapping("tasks")
    public Task addTask(@RequestBody Task task) {
        task.setTaskName(Character.toUpperCase(task.getTaskName().charAt(0))+task.getTaskName().substring(1));
        System.out.println(task.toString());
        return this.taskRepository.save(task);
    }

    @DeleteMapping("tasks/delete")
    public boolean deleteTask(@RequestParam String id){
        long id_ = Long.valueOf(id);
        if(this.taskRepository.findById(id_).isPresent()) {
            this.taskRepository.delete(this.taskRepository.findById(id_).get());
            return true;
        }else{
            return false;
        }
    }

    @PutMapping("tasks/edit")
    public void editTask(@RequestBody Task task){
        this.taskRepository.save(task);
    }

    @GetMapping("tasks/search")
    public List<Task> searchTasks(@RequestParam String request) {
        if(request.equals("")){
            List<Task> list =  this.taskRepository.findAll();
            list.sort((Task e1, Task e2) -> e1.getExpiration().compareTo(e2.getExpiration()));
//            System.out.println("list");
//            list.forEach(System.out::println);
            return list;
        }else{
            List<Task> results = this.searchService.search(request, "taskName", Task.class);
//            System.out.println("result");
//            results.forEach(System.out::println);
            return results;
        }
        }

}
