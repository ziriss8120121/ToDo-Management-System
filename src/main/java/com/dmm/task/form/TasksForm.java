package com.dmm.task.form;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TasksForm {
    private String title;
	
	private String text;
	
	private LocalDate date;
	
	private boolean done;

}
