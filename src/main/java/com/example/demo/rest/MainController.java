package com.example.demo.rest;

import com.example.demo.jwt.JwtTokenUtil;
import com.example.demo.mail.EmailServiceImpl;
import com.example.demo.model.JwtAuthenticationRequest;
import com.example.demo.model.User;
import com.example.demo.model.UserType;
import com.example.demo.repository.*;
import com.example.demo.security.CurrentUser;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/rest/main")
public class MainController {

    @Value("${forumRest.post.upload.path}")
    private String imageUploadPath;

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


    @PostMapping(value = "/auth")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {
        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails);

        // Return the token
        return ResponseEntity.ok(token);
    }


    @PostMapping("/saveUser")
    public ResponseEntity saveUser(@RequestBody User user) {
        if (userRepository.findOneByEmail(user.getEmail()) != null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with email "+user.getEmail()+" ka");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserType(UserType.USER);
        user.setVerify(false);

        String token = jwtTokenUtil.generateToken(new CurrentUser(user));
        user.setToken(token);
        userRepository.save(user);
        String message = String.format("Hi %s, You are successfully registered to our cool portal. Please visit by <a href=\"http://localhost:8080/rest/main/verify?token=%s\">this</a> link to verify your account", user.getName(), token);
        emailService.sendSimpleMessage(user.getEmail(), "Welcome", message);
        return ResponseEntity.ok("created");
    }


    @GetMapping("/verify")
    public ResponseEntity verifyUser(@RequestParam("token") String token) {
        String email = jwtTokenUtil.getUsernameFromToken(token);
        User oneByEmail = userRepository.findOneByEmail(email);
        oneByEmail.setVerify(true);
        userRepository.save(oneByEmail);
        return ResponseEntity.ok("verified");
    }


}
