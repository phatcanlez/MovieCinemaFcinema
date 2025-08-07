package com.example.projectwebmovie.dto;


import com.example.projectwebmovie.model.Account;
import lombok.Data;

@Data
public class EmailDetail {
    Account receiver;
    String subject;
    String link;
    String content;
    String button;
}
