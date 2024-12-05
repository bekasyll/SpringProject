package com.example.project.data;

import com.example.project.model.Category;
import com.example.project.repo.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Добавляем категории, если они еще не существуют
        if (categoryRepository.count() == 0) {
            Category category1 = new Category();
            category1.setName("Home Work");
            categoryRepository.save(category1);

            Category category2 = new Category();
            category2.setName("Class Work");
            categoryRepository.save(category2);

            Category category3 = new Category();
            category3.setName("For Yourself");
            categoryRepository.save(category3);

            System.out.println("Categories added to the database.");
        }
    }
}
