package com.example.demo.repository;

import com.example.demo.model.Category;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Integer> {
    Category getCategoryByName(JsonNode name);
}
