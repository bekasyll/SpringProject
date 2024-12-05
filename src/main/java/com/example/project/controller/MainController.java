package com.example.project.controller;

import com.example.project.model.User;
import com.example.project.model.Category;
import com.example.project.model.Task;
import com.example.project.services.CategoryService;
import com.example.project.services.TaskService;
import com.example.project.services.UserService;
import com.example.project.repo.TaskRepository;
import com.example.project.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {
    private final UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final TaskRepository taskRepository;

    @GetMapping("/auth/register")
    public String showRegisterForm(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("user", new User());
        if (error != null) {
            model.addAttribute("errorMessage", "Email уже используется. Попробуйте другой.");
        }
        return "register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@ModelAttribute("user") User user) {
        if (userService.findByEmail(user.getEmail()) != null) {
            return "redirect:/auth/register?error=email_exists";
        }
        userService.saveUser(user);
        return "redirect:/auth/login";
    }

    @GetMapping("/auth/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails currentUser,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "query", required = false) String query,
                       Model model) {
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        String username = currentUser.getUsername();
        User user = userService.findByUsername(username);

        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("currentUser", user);

        try {
            PageRequest pageRequest = PageRequest.of(page, 3);

            Page<Task> tasksPage;

            if (query != null && !query.isEmpty()) {
                tasksPage = taskService.findTasksByUserAndTitleContaining(user.getId(), query, pageRequest);
            } else {
                tasksPage = taskService.findTasksByUser(user.getId(), pageRequest);
            }

            model.addAttribute("tasksPage", tasksPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", tasksPage.getTotalPages());
            model.addAttribute("query", query);
        } catch (Exception e) {
            System.err.println("Ошибка при чтении задач: " + e.getMessage());
            model.addAttribute("errorMessage", "Не удалось загрузить задачи. Обратитесь к администратору.");
            return "error-page";
        }

        return "home";
    }

    @Autowired
    public MainController(TaskRepository taskRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/search")
    public String searchTasks(@RequestParam("query") String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("tasks", taskRepository.findAll());
        } else {
            model.addAttribute("tasks", taskRepository.findByTitleContainingIgnoreCase(query.trim()));
        }
        return "home";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories";
    }

    @GetMapping("/tasks/{categoryId}")
    public String tasks(@PathVariable Long categoryId, Model model) {
        List<Task> tasks = taskService.findTasksByCategory(categoryId);
        model.addAttribute("tasks", tasks);
        return "tasks";
    }

    @GetMapping("/task/new")
    public String newTask(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", Task.Status.values());
        model.addAttribute("priorities", Task.Priority.values());
        return "new-task";
    }

    @PostMapping("/task")
    public String addTask(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("dueDate") String dueDate,
            @RequestParam(value = "status", required = false, defaultValue = "PENDING") String status,
            @RequestParam("priority") String priority,
            @RequestParam("categoryId") Long categoryId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        User user = userService.findByUsername(currentUser.getUsername());
        Category category = categoryService.getCategoryById(categoryId);

        if (user != null && category != null) {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate.isEmpty() ? null : LocalDate.parse(dueDate));
            task.setStatus(Task.Status.valueOf(status.toUpperCase()));
            task.setPriority(Task.Priority.valueOf(priority.toUpperCase()));
            task.setCategory(category);
            task.setUser(user);

            taskService.addTask(task);
        }

        return "redirect:/home";
    }

    @GetMapping("/task/{id}")
    public String viewTaskDetails(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        if (task == null) {
            return "redirect:/home";
        }
        model.addAttribute("task", task);
        return "task-details";
    }

    @GetMapping("/task/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTaskById(id);
        return "redirect:/home";
    }
}