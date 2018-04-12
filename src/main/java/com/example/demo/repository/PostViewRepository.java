package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostViewRepository extends JpaRepository<PostView,Integer>{
    List<PostView> findAllByPost(Post post);
}
