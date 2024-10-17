package com.developkim.sommelierbot.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatClient chatClient;

    private String generateNextQuestion(String userAnswer, int step) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        requestSpec.user("사용자의 이전 답변: '" + userAnswer + "'에 따라 다음 질문을 생성해 주세요.");
        return requestSpec.call().content();
    }

    // 첫 번째 질문을 생성하는 메서드
    private String generateFirstQuestion() {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        requestSpec.user("저는 AI 소믈리에입니다. 사용자의 음식 취향, 와인 취향, 기분 등을 분석하여 와인을 추천해드리는 서비스입니다. " +
                "지금부터, 각 질문에 대해 한 문장씩 대답해주세요. 만약 대답이 와인 추천에 도움이 되지 않는다면, 예외를 처리하여 와인을 추천하는 데 어려움이 있다고 안내할 것입니다. " +
                "그리고 매번 질문은 한 문장으로만 할 것입니다.");

        return requestSpec.call().content();
    }

    // 첫 번째 질문을 사용자에게 전달하는 엔드포인트
    @GetMapping("/chat/{step}")
    public String getChatPage(@PathVariable int step, Model model, HttpSession session) {
        String question;

        if (step == 1) {
            question = generateFirstQuestion();
        } else {
            // 사용자가 이전에 입력한 답변을 세션에서 가져옴
            String userAnswer = (String) session.getAttribute("userAnswer" + (step - 1));
            // 이전 답변을 바탕으로 다음 질문을 생성
            question = generateNextQuestion(userAnswer, step);
        }

        model.addAttribute("question", question);
        model.addAttribute("step", step);

        return "chat";  // chat.html 페이지로 이동
    }

    @PostMapping("/chat/{step}")
    public String postChat(@PathVariable int step, @RequestParam String answer, Model model, HttpSession session) {
        // 사용자의 답변을 세션에 저장
        session.setAttribute("userAnswer" + step, answer);

        // 다음 단계로 이동할 step 계산
        int nextStep = step + 1;

        // 모델에 다음 질문을 위한 데이터를 추가
        model.addAttribute("answer", answer);
        model.addAttribute("step", nextStep);

        // 다음 단계로 이동 (redirect)
        return "redirect:/chat/" + nextStep;
    }
}
