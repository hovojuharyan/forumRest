package com.example.demo.repository;

import com.example.demo.model.Category;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Integer> {
     public List<Post> findAllByUser(User user);
     List<Post> findAllByCategory(Category category);
}
