package com.example.demo.rest;

import com.example.demo.jwt.JwtTokenUtil;
import com.example.demo.mail.EmailServiceImpl;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rest/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostViewRepository postViewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping
    @ApiOperation(value = "Get all users", response = User.class, responseContainer = "list")
    public ResponseEntity getAllUsers() {
        if (userRepository.findAll() == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("users not exist");
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity getUserById(@PathVariable(name = "id") int id) {
        User one = userRepository.findOne(id);
        if (one == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).
                    body("User with " + id + " id no found");
        }
        return ResponseEntity.ok(one);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUserById(@PathVariable(name = "id") int id) {
        if (userRepository.findOne(id) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with id "+id+" does not exist");
        }
        userRepository.delete(id);
        return ResponseEntity.ok("deleted");
    }

    @PostMapping()
    public ResponseEntity saveUser(@RequestBody User user) {
        if (userRepository.findOneByEmail(user.getEmail()) != null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with email "+user.getEmail()+" ka");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerify(false);
        userRepository.save(user);
//        String token = jwtTokenUtil.generateToken(new CurrentUser(user));
//        String message = String.format("Hi %s, You are successfully registered to our cool portal. Please visit by <a href=\"http://localhost:8080/rest/users/verify?token=%s\">this</a> link to verify your account", user.getName(), token);
//        emailService.sendSimpleMessage(user.getEmail(), "Welcome", message);
        return ResponseEntity.ok("created");
    }

    @DeleteMapping("/deleteUserById/{id}")
    public ResponseEntity deleteUser(@PathVariable(name = "id") int id){
        if (userRepository.findOne(id) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with id "+id+" does not exist");
        }
        userRepository.delete(id);
        return ResponseEntity.ok("user with id "+id+" succesfully deleted");
    }

    @GetMapping("/getCategories")
    public ResponseEntity getAllCategoriess(){
        List<Category> categoriess=categoryRepository.findAll();
        return ResponseEntity.ok(categoriess);
    }

    @PostMapping("/addCategory")
    public ResponseEntity addCategory(@RequestBody Category category){
        categoryRepository.save(category);
        return ResponseEntity.ok("category with name "+category.getName()+" succesfully added");
    }

    @DeleteMapping("/deleteCategoryById/{id}")
    public ResponseEntity deleteCategory(@PathVariable(name = "id") int id){
        categoryRepository.delete(id);
        return ResponseEntity.ok("category with id "+id+" succesfully deleted");
    }

    @GetMapping("/getCategoryById/{id}")
    public ResponseEntity getCategoryById(@PathVariable(name = "id") int id){
        Category category=categoryRepository.findOne(id);
        if (category==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category with id "+id+" does not exist");
        }
        return ResponseEntity.ok(category);
    }

    @GetMapping("/getPosts")
    public ResponseEntity getAllPosts(){
        List<Post> posts=postRepository.findAll();
        for (Post post : posts) {
            post.setViewCount(postViewRepository.findAllByPost(post).size());
        }
        if (posts==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("posts are null");
        }
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/addPost")
    public ResponseEntity addPost(@RequestBody Post post,@AuthenticationPrincipal UserDetails userDetails){
        post.setUser(userRepository.findOneByEmail(userDetails.getUsername()));
        postRepository.save(post);
        return ResponseEntity.ok("post with name "+post.getTitle()+" succesfully added");
    }

    @DeleteMapping("/deletePostById/{id}")
    public ResponseEntity deletePost(@PathVariable(name = "id") int id){
        postRepository.delete(id);
        return ResponseEntity.ok("post with id "+id+" succesfully deleted");
    }

    @GetMapping("/getPostById/{id}")
    public ResponseEntity getPostById(@PathVariable(name = "id") int id){
        Post post=postRepository.findOne(id);
        if (post==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("post with id "+id+" does not exist");
        }
        PostView postView = new PostView();
        postView.setPost(post);
        postViewRepository.save(postView);
        post.setViewCount(postViewRepository.findAllByPost(post).size());
        return ResponseEntity.ok(post);
    }

    @PostMapping("/addComment/{postId}")
    public ResponseEntity addComment(@RequestBody Comment comment, @PathVariable ("postId") int postId,@AuthenticationPrincipal UserDetails userDetails){
        comment.setUser(userRepository.findOneByEmail(userDetails.getUsername()));
        Post post=postRepository.findOne(postId);
        comment.setPost(post);
        commentRepository.save(comment);
        return ResponseEntity.ok("comment added");
    }

    @DeleteMapping("/deleteComment/{id}")
    public ResponseEntity deleteComment( @PathVariable(name = "postId") int postId,@PathVariable(name = "id") int id){
        commentRepository.delete(commentRepository.findOne(id));
        return ResponseEntity.ok("deleted");
    }

    @GetMapping("/getPostsByCategoryId/{id}")
    public ResponseEntity getPostsByCategoryId(@PathVariable(name = "id")int id){
        Category category=categoryRepository.findOne(id);
        List<Post> posts=postRepository.findAllByCategory(category);
        for (Post post : posts) {
            post.setViewCount(postViewRepository.findAllByPost(post).size());
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/getMyPosts")
    public ResponseEntity getMyPosts(@AuthenticationPrincipal UserDetails userDetails){
        User user=userRepository.findOneByEmail(userDetails.getUsername());
        Post post= (Post) postRepository.findAllByUser(user);
        post.setViewCount(postViewRepository.findAllByPost(post).size());
        return ResponseEntity.ok(postRepository.findAllByUser(user));
    }

}
