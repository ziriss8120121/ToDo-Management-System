package com.dmm.task.data.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dmm.task.data.entity.Tasks;

@Repository
public interface TasksRepository extends JpaRepository<Tasks, Long> {

	@Query("SELECT t FROM Tasks t WHERE t.date BETWEEN :from AND :to AND t.name = :name")
    List<Tasks> findByDateBetweenAndName(@Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to,
                                         @Param("name") String name);	
	
	
}
