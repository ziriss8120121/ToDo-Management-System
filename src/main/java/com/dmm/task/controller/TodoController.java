package com.dmm.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TasksForm;

@Controller
public class TodoController {
	
	@Autowired
	private TasksRepository tasksRepository;
	@GetMapping("/testcreate")
	public String NewTasks(Model model) {
		
		TasksForm tasksForm = new TasksForm();
		model.addAttribute("tasksForm",tasksForm);
		
		return "create";
	}
	@PostMapping("/create")
	public String createTask(TasksForm tasksForm) {
        Tasks tasks = new Tasks();
        tasks.setDate(tasksForm.getDate());
        tasks.setText(tasksForm.getText());
        tasks.setDone(true);
        tasks.setTitle(tasksForm.getTitle());
        tasksRepository.save(tasks);
        return "redirect:/main"; // タスク作成後はメインページにリダイレクトする
    }


	@RequestMapping("/testedit")
	public String test() {
		return "edit";
	}

	@RequestMapping("/testmain")
	public String main() {
		return "main";
	}

	@RequestMapping("/testlogin")
	public String login() {
		return "login";
	}

}
