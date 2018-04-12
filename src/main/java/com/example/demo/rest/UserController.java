package com.example.demo.rest;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import io.swagger.annotations.ApiOperation;
import com.example.demo.jwt.JwtTokenUtil;
import com.example.demo.mail.EmailServiceImpl;
import com.example.demo.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping(value = "/rest/user")
public class UserController {


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


    @GetMapping("/getPostById/{id}")
    public ResponseEntity getPostById(@PathVariable(name = "id") int id) {
        Post post = postRepository.findOne(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("post with id " + id + " does not exist");
        }
        PostView postView = new PostView();
        postView.setPost(post);
        postViewRepository.save(postView);
        post.setViewCount(postViewRepository.findAllByPost(post).size());
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/deleteCommentById/{id}")
    public ResponseEntity deleteCommentById(@PathVariable(name = "id") int id) {
        commentRepository.delete(id);
        return ResponseEntity.ok("comment with id " + id + " succesfuly deleted");
    }

    @PostMapping("/addComment/{postId}")
    public ResponseEntity addComment(@RequestBody Comment comment, @PathVariable("postId") int postId, @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postRepository.findOne(postId);
        post.setViewCount(postViewRepository.findAllByPost(post).size());
        comment.setUser(userRepository.findOneByEmail(userDetails.getUsername()));
        comment.setPost(post);
        commentRepository.save(comment);
        return ResponseEntity.ok("comment added");
    }


    @GetMapping("/getAllUsers")
    public ResponseEntity getAllUsers() {
        if (userRepository.findAll() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("users not exist");
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    //
//
//
//
//
    @DeleteMapping("/deleteMyPost/{id}")
    public ResponseEntity deleteMyPost(@PathVariable(name = "id") int id, @AuthenticationPrincipal UserDetails userDetail) {
        Post post = postRepository.findOne(id);
//        if (post.getUser()!=userRepository.findOneByEmail(userDetail.getUsername())){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("you can not delete other posts");
//        }
        postRepository.delete(id);
        return ResponseEntity.ok("post succesfuly deleted");
    }

    @PostMapping("/addPost")
    public ResponseEntity addPost(@RequestBody Post post, @AuthenticationPrincipal UserDetails userDetails) {
        post.setUser(userRepository.findOneByEmail(userDetails.getUsername()));
        postRepository.save(post);
        return ResponseEntity.ok("post with name " + post.getTitle() + " succesfully added");
    }

    @GetMapping("/getPosts")
    public ResponseEntity getAllPosts() {
        List<Post> posts = postRepository.findAll();
        for (Post post : posts) {
            post.setViewCount(postViewRepository.findAllByPost(post).size());
        }
        if (posts == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("posts are null");
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/getMyPosts")
    public ResponseEntity getMyPosts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findOneByEmail(userDetails.getUsername());
        List<Post> posts = postRepository.findAllByUser(user);
        for (Post post : posts) {
            post.setViewCount(postViewRepository.findAllByPost(post).size());
        }
        return ResponseEntity.ok(postRepository.findAllByUser(user));
    }

    @GetMapping("/getAllCategories")
    public ResponseEntity getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/getCategoryById/{id}")
    public ResponseEntity getCategoryById(@PathVariable(name = "id") int id) {
        Category category = categoryRepository.findOne(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("category with id " + id + " does not exist");
        }
        return ResponseEntity.ok(category);
    }

    @GetMapping("/getPostsByCategoryId/{id}")
    public ResponseEntity getPostsByCategoryId(@PathVariable(name = "id") int id) {
        Category category = categoryRepository.findOne(id);
        List<Post> posts = postRepository.findAllByCategory(category);
        for (Post post : posts) {
            post.setViewCount(postViewRepository.findAllByPost(post).size());
        }
        return ResponseEntity.ok(posts);
    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.POST)
    public ResponseEntity changePassword(@RequestParam(name = "old") String oldPassword, @RequestParam(name = "new") String newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user;
        if(userDetails != null){
            user = ((CurrentUser) userDetails).getUser();
        }else {
            user = null;
        }

        if (passwordEncoder.matches(oldPassword,user.getPassword() )) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }else{
            String message= "password change failed";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        userRepository.save(user);
        return ResponseEntity.ok("password succesfuly chnged");
    }

    @RequestMapping(value = "/user/updateUser", method = RequestMethod.POST)
    public ResponseEntity updateUser(@RequestParam(name = "name") String name, @RequestParam(name = "surname") String surname,
                              @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user;
        if(userDetails != null){
            user = ((CurrentUser) userDetails).getUser();
        }else {
            user = null;
        }

        if(name == null || name.equals("") || surname == null || surname.equals("")){
            String message= "name or surname can not by empty";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        user.setName(name);
        user.setSurname(surname);
        userRepository.save(user);
        return ResponseEntity.ok("succesfuly changed");
    }
}