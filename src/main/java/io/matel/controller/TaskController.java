package io.matel.controller;


import io.matel.model.Task;
import io.matel.repository.TaskRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cal")
@CrossOrigin
public class TaskController {
    private TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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


}
