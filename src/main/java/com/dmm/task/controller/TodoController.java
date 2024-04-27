package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
		tasks.setDone(false);
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
		task.setDone(tasksForm.isDone());
		LocalDateTime dateTime = tasksForm.getDate().atTime(LocalTime.MIN);
		task.setDate(dateTime); 
		tasksRepository.save(task);
		return "redirect:/main"; 
	}


	@GetMapping("/main")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	public String main(Model model, 
			@AuthenticationPrincipal UserDetails userDetails,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {


		System.out.println(date);//デバッグ
		//2次元リスト
		List<List<LocalDate>> calendarMatrix = new ArrayList<>();
		//当月の初日の日付
		LocalDate firstDayOfMonth;
		if (date == null) {
			firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
		} else {
			firstDayOfMonth = date;

		}

		//先月の初日の日付を計算（prev）
		LocalDate prevMonth = firstDayOfMonth.minusMonths(1).withDayOfMonth(1);

		// 来月の初日の日付を計算（next）
		LocalDate nextMonth = firstDayOfMonth.plusMonths(1).withDayOfMonth(1);
		// 月の日本語表記を取得する
		String japaneseMonth = firstDayOfMonth.getMonth().getDisplayName(
				TextStyle.FULL_STANDALONE,
				Locale.JAPAN
				);

		int year = firstDayOfMonth.getYear();

		String yearAndMonth = year + "年" + japaneseMonth;
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
		//来月分の日付
		LocalDate nextMonthDate = firstDayOfMonth.plusMonths(1).withDayOfMonth(1);
		DayOfWeek lastDayOfWeek = nextMonthDate.minusDays(1).getDayOfWeek();
		for (int i = 0; i < (6 - lastDayOfWeek.getValue() % 7) % 7; i++) {
			weekDates.add(nextMonthDate.plusDays(i));
		}
		calendarMatrix.add(new ArrayList<>(weekDates));

		
		model.addAttribute("matrix", calendarMatrix);
		model.addAttribute("month", yearAndMonth);
		model.addAttribute("prev", prevMonth);
		model.addAttribute("next", nextMonth);



		List<Tasks> list;
		if (userDetails != null && userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			// 管理者の場合はすべてのユーザーの当月のタスクを取得
			LocalDateTime startOfMonth = prevMonthDate.atStartOfDay();
			LocalDateTime endOfMonth = nextMonthDate.plusDays((6 - lastDayOfWeek.getValue() % 7) - 1 ).atStartOfDay();
			System.out.println("★★startOfMonth=" + startOfMonth);
			System.out.println("★★endOfMonth=" + endOfMonth);
			list = tasksRepository.findAllTasksForMonth(startOfMonth, endOfMonth);
		} else  {
			// 一般ユーザーの場合は自分の当月のタスクのみを取得
			String username = userDetails.getUsername();
			LocalDateTime startOfMonth = prevMonthDate.atStartOfDay();
			LocalDateTime endOfMonth = nextMonthDate.plusDays(6 - lastDayOfWeek.getValue() % 7).atStartOfDay();
			System.out.println("★★startOfMonth=" + startOfMonth);
			System.out.println("★★endOfMonth=" + endOfMonth);
			list = tasksRepository.findByDateBetweenAndName(startOfMonth, endOfMonth, username);
		} 
		MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();
		for (Tasks task : list) {
			LocalDate dates = task.getDate().toLocalDate();
			tasks.add(dates, task);
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
