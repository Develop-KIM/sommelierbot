package com.developkim.sommelierbot.controller;

import com.developkim.sommelierbot.service.ChatService;
import com.developkim.sommelierbot.util.Wine;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatGptController {

    private final ChatService chatService;

    @GetMapping("/chat/{step}")
    public String getChatPage(@PathVariable int step, Model model, HttpSession session) {
        String question = (step == 1)
                ? chatService.generateFirstQuestion()
                : chatService.generateNextQuestion((String) session.getAttribute("userAnswer" + (step - 1)), session);

        model.addAttribute("question", question);
        model.addAttribute("step", step);
        return "chat"; // chat.html 템플릿으로 이동
    }

    @PostMapping("/chat/{step}")
    public String postChat(@PathVariable int step, @RequestParam String answer, HttpSession session) {
        session.setAttribute("userAnswer" + step, answer);
        log.info("User answer for step {}: {}", step, answer); // 사용자 답변 로그

        if (++step > 7) {
            try {
                chatService.generateWineRecommendations(session); // 추천 와인 생성
            } catch (IOException e) {
                log.error("Error generating wine recommendations", e); // 에러 로그
            }
            return "redirect:/result"; // 결과 페이지로 리다이렉트
        }

        return "redirect:/chat/" + step; // 다음 단계로 리다이렉트
    }

    @GetMapping("/result")
    public String getResultPage(Model model, HttpSession session) {
        // 세션에서 추천 와인 정보 가져오기
        List<Wine> recommendations = (List<Wine>) session.getAttribute("recommendedWines");

        if (recommendations == null || recommendations.isEmpty()) {
            log.warn("추천 와인 정보가 세션에 없습니다."); // 추천 와인이 없을 경우 경고 로그
        } else {
            log.info("Recommendations: {}", recommendations); // 추천 와인 로그
        }

        // 모델에 추천 와인 정보를 추가
        model.addAttribute("recommendations", recommendations != null ? recommendations : new ArrayList<>());

        return "result"; // result.html 템플릿으로 이동
    }


}
