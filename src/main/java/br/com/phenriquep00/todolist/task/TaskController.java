package br.com.phenriquep00.todolist.task;

import br.com.phenriquep00.todolist.utils.Utils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController
{
    @Autowired
    private ITaskRepository taskRepository;



    // create a task
    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request)
    {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        if(!isValidDate(taskModel.getStartAt(), taskModel.getEndAt()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date cannot be in the past and end date cannot be before the start date");
        }


        return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.save(taskModel));
    }

    // get all tasks from a user
    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request)
    {
        return this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
    }

    // update a task
    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id)
    {
        var idUser = (UUID) request.getAttribute("idUser");

        var task = this.taskRepository.findById(id).orElse(null);

        if(task == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        if (!task.getIdUser().equals(idUser))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Utils.copyNonNullProperties(taskModel, task);

        if(!isValidDate(task.getStartAt(), task.getEndAt()))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date cannot be in the past and end date cannot be before the start date");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.save(task));
    }

    private boolean isValidDate(LocalDateTime startDate, LocalDateTime endDate)
    {
        var currentDate = LocalDateTime.now();

        // validates if the start date is in the past
        if(currentDate.isAfter(startDate))
        {
            return false;
        }

        // validates if the end date is in the past
        if(currentDate.isAfter(endDate))
        {
            return false;
        }

        // validates if the end date is before the start date
        if(endDate.isBefore(startDate))
        {
            return false;
        }

        // validates if the start date is after the end date
        if(startDate.isAfter(endDate))
        {
            return false;
        }

        return true;
    }
}
