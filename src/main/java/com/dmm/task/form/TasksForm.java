package com.dmm.task.form;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TasksForm {
    private String title;
	
	private String text;
	
	private LocalDateTime date;
	
	private boolean done;

}
