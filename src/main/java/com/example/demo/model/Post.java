package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "post")
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private int id;
    @Column
    private String title;
    @Column
    private String text;
    @ManyToOne
    private Category category;
    @ManyToOne
    private User user;
    @Column(name = "pic_url")
    private String picUrl;
    @Column
    private String timestamp;
    @Transient
    private int commentCount;
    @Transient
    private int viewCount;
}
