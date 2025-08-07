package com.example.projectwebmovie.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    @PostMapping("/chat")
    public ResponseEntity<String> chatWithBot(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String flaskUrl = "https://movie-chatbot-2o7m.onrender.com/api/chat";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("message", userMessage), headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(flaskUrl, entity, Map.class);
            String reply = response.getBody().get("reply").toString();
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Chatbot lỗi: " + e.getMessage());
        }
    }
}
