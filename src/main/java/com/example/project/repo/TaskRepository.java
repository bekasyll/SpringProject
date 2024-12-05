package com.example.project.repo;

import com.example.project.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    List<Task> findByCategoryId(Long categoryId);
    List<Task> findByTitleContainingIgnoreCase(String title);
    Page<Task> findByUserId(Long userId, PageRequest pageRequest);
    Page<Task> findByUserIdAndTitleContaining(Long userId, String title, Pageable pageable);
}

