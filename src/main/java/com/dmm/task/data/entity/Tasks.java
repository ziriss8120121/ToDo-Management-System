package com.dmm.task.data.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "tasks")
@Data
public class Tasks {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private Long id;
	
	private String name;
	
	private String title;
	
	private String text;
	
	private LocalDateTime date;
	
	private boolean done;
	
	
	
	
	

}
