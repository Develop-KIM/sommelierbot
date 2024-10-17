package com.developkim.sommelierbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatClient chatClient;

    // 첫 번째 질문을 생성하는 메서드
    String generateFirstQuestion() {
        // AI에게 와인 선호도 관련 첫 질문을 생성하라는 요청을 보냄
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        requestSpec.user("평소 어떤 음식을 좋아하는지 한 문장으로 물어봐");
        // 첫 번째 질문을 생성하고 반환
        return requestSpec.call().content();
    }

    // 첫 번째 질문을 사용자에게 전달하는 엔드포인트
    @GetMapping("/chat")
    public String getChatPage(Model model) {
        // 첫 번째 질문 생성

        // 모델에 첫 번째 질문을 담아서 반환
        model.addAttribute("question", generateFirstQuestion());

        // chat.html 페이지로 이동
        return "chat";  // chat.html을 반환
    }

    // 사용자 대답을 받고, AI가 다음 질문을 생성하는 엔드포인트
//    @PostMapping("/chat")
//    public ResponseEntity<String> postChat(@RequestBody Map<String, String> request) {
//        String message = request.get("message");
//
//        // 사용자의 대답을 바탕으로 AI가 다음 질문을 생성하도록 요청
//        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
//        String prompt = "사용자의 대답: '" + message + "'에 따라 다음 와인 선호도 질문을 생성해 주세요.";  // 대답에 맞는 질문 생성 요청
//
//        requestSpec.user(prompt);  // 사용자 대답을 바탕으로 질문을 설정
//
//        // AI의 응답을 받아서 반환
//        String nextQuestion = requestSpec.call().content();
//
//        // 생성된 다음 질문을 반환
//        return ResponseEntity.ok(nextQuestion);  // 예시: "Do you prefer sweet or dry wines?"
//    }
//    @PostMapping("/chat")
//    public String chat(@RequestBody Map<String, String> request) {
//        String message = request.get("message");
//
//        // 사용자 메시지를 설정하고, 챗봇 응답을 받아오는 과정
//        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
//        requestSpec.user(message);  // 사용자 메시지 설정
//
//        // API 호출 후 응답 받기
//        return requestSpec.call().content();
//    }
}
