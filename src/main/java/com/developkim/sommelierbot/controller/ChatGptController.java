package com.developkim.sommelierbot.controller;

import com.developkim.sommelierbot.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatService chatService;

    @GetMapping("/chat/{step}")
    public String getChatPage(@PathVariable int step, Model model, HttpSession session) {
        String question;
        if (step == 1) {
            question = chatService.generateFirstQuestion(); // 첫 질문 생성
        } else {
            String userAnswer = (String) session.getAttribute("userAnswer" + (step - 1));
            question = chatService.generateNextQuestion(userAnswer, session); // 다음 질문 생성
        }

        model.addAttribute("question", question);
        model.addAttribute("step", step);
        return "chat";
    }

    @PostMapping("/chat/{step}")
    public String postChat(@PathVariable int step, @RequestParam String answer, Model model, HttpSession session) {
        session.setAttribute("userAnswer" + step, answer);

        int nextStep = step + 1;
        if (nextStep > 7) { // 7개 질문이 끝나면 추천 페이지로 이동
            String[] recommendations = chatService.generateWineRecommendations(session);
            session.setAttribute("recommendations", recommendations);
            return "redirect:/result";
        }

        return "redirect:/chat/" + nextStep;
    }

    @GetMapping("/result")
    public String getResultPage(Model model, HttpSession session) {
        String[] recommendations = (String[]) session.getAttribute("recommendations");
        model.addAttribute("recommendations", recommendations != null ? recommendations : new String[0]);
        return "result";
    }
}
