package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TasksForm;

@Controller
public class TodoController {
	
	@Autowired
	private TasksRepository tasksRepository;
	@GetMapping("/main/create/{date}")
	public String NewTasks(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime date, Model model) {
		
		TasksForm tasksForm = new TasksForm();
		tasksForm.setDate(date);
		
		model.addAttribute("tasksForm",tasksForm);
		
		return "create";
	}
	@PostMapping("main/create")
	public String createTask(TasksForm tasksForm) {
        Tasks tasks = new Tasks();
        tasks.setDate(tasksForm.getDate());
        tasks.setText(tasksForm.getText());
        tasks.setDone(true);
        tasks.setTitle(tasksForm.getTitle());
        tasksRepository.save(tasks);
        return "redirect:/main"; // タスク作成後はメインページにリダイレクトする
    }


	@RequestMapping("/edit")
	public String test() {
		return "edit";
	}

	@GetMapping("/main")
	public String main(Model model) {
    //2次元リスト
    List<List<LocalDate>> calendarMatrix = new ArrayList<>();
    //当月の初日の日付
    LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
    //当月の日数
    int daysInMonth = firstDayOfMonth.lengthOfMonth();
    //最初の曜日
    DayOfWeek firstDayOfWeek = firstDayOfMonth.getDayOfWeek();
    //1週間分の日付
    List<LocalDate> weekDates = new ArrayList<>();
    //前月分の日付
    LocalDate prevMonthDate = firstDayOfMonth.minusDays(firstDayOfWeek.getValue() % 7);
    for (int i = 0; i < firstDayOfWeek.getValue() % 7; i++) {
        weekDates.add(prevMonthDate.plusDays(i));
    }
    //当月の日付
    for (int i = 1; i <= daysInMonth; i++) {
        LocalDate currentDate = firstDayOfMonth.withDayOfMonth(i);
        weekDates.add(currentDate);
        if (weekDates.size() == 7) {
            calendarMatrix.add(new ArrayList<>(weekDates));
            weekDates.clear();
        }
    }
    LocalDate nextMonthDate = firstDayOfMonth.plusMonths(1).withDayOfMonth(1);
    DayOfWeek lastDayOfWeek = nextMonthDate.minusDays(1).getDayOfWeek();
    for (int i = 0; i < (6 - lastDayOfWeek.getValue() % 7) % 7; i++) {
        weekDates.add(nextMonthDate.plusDays(i));
    }
    calendarMatrix.add(new ArrayList<>(weekDates));
    
    model.addAttribute("matrix", calendarMatrix);
    model.addAttribute("month", firstDayOfMonth.getMonth());
    
 // 仮のタスクリストを追加
    Map<LocalDateTime, List<Tasks>> tasks = new HashMap<>();
    model.addAttribute("tasks", tasks);  // tasksをモデルに追加する
     return "main";
    }

    

  
	@RequestMapping("/login")
	public String login() {
		return "login";
	}

}
