package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
	public String NewTasks(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
		
		TasksForm tasksForm = new TasksForm();
		tasksForm.setDate(date);
		
		model.addAttribute("tasksForm",tasksForm);
		
		return "create";
	}
	@PostMapping("main/create")
	public String createTask(@AuthenticationPrincipal UserDetails userDetails, @DateTimeFormat(pattern = "yyyy-MM-dd") TasksForm tasksForm) {
        Tasks tasks = new Tasks();
        tasks.setName(userDetails.getUsername());
        LocalDateTime dateTime = tasksForm.getDate().atTime(LocalTime.MIN);
        tasks.setDate(dateTime); 
        tasks.setText(tasksForm.getText());
        tasks.setDone(true);
        tasks.setTitle(tasksForm.getTitle());
        tasksRepository.save(tasks);
        
        return "redirect:/main"; // タスク作成後はメインページにリダイレクトする
    }


	@GetMapping("main/edit/{id}")
	public String editTask(@PathVariable Long id, Model model) {
	
    Tasks task = tasksRepository.findById(id).orElse(null);
    
    model.addAttribute("task", task);
		
		return "edit";
	}
	
	@PostMapping("/main/edit/{id}")
	public String updateTask(@PathVariable Long id, @DateTimeFormat(pattern = "yyyy-MM-dd") TasksForm tasksForm) {
		Tasks task = tasksRepository.findById(id).orElse(null);
		task.setTitle(tasksForm.getTitle());
        task.setText(tasksForm.getText());
        LocalDateTime dateTime = tasksForm.getDate().atTime(LocalTime.MIN);
        task.setDate(dateTime); 
        tasksRepository.save(task);
		return "redirect:/main"; 
	}
  

	@GetMapping("/main")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	public String main(Model model, @AuthenticationPrincipal UserDetails userDetails ) {
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
    
 
    List<Tasks> list;
    if (userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        // 管理者の場合はすべてのユーザーの当月のタスクを取得
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = firstDayOfMonth.plusMonths(1).atStartOfDay().minusSeconds(1);
        list = tasksRepository.findAllTasksForMonth(startOfMonth, endOfMonth);
    } else if (userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
        // 一般ユーザーの場合は自分の当月のタスクのみを取得
        String username = userDetails.getUsername();
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = firstDayOfMonth.plusMonths(1).atStartOfDay().minusSeconds(1);
        list = tasksRepository.findByDateBetweenAndName(endOfMonth, startOfMonth, username);
    } 
    MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();
    for (Tasks task : list) {
        LocalDate date = task.getDate().toLocalDate();
        tasks.add(date, task);
    }
    model.addAttribute("tasks", tasks);  // tasksをモデルに追加する
     return "main";
    }

	@PostMapping("/main/delete/{id}")
	// 処理の中でidを使えるように、引数にidを追加
	public String deleteTask(@PathVariable Long id) {
		// 指定したIDのユーザーを削除
		tasksRepository.deleteById(id);
		return "redirect:/main";
	}
    
	
	@RequestMapping("/login")
	public String login() {
		return "login";
	}

}
