package com.developkim.sommelierbot.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;

    // 첫 번째 질문을 생성하는 메서드
    public String generateFirstQuestion() {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        requestSpec.user("첫 번째 질문을 간단히 해 주세요.");
        return requestSpec.call().content().trim();
    }

    // 다음 질문을 생성하는 메서드
    public String generateNextQuestion(String userAnswer, HttpSession session) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();

        // 이전 질문들 구성
        StringBuilder previousQuestions = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            String question = (String) session.getAttribute("question" + i);
            if (question != null) {
                previousQuestions.append("질문 ").append(i).append(": ").append(question).append(". ");
            }
        }

        requestSpec.user("사용자의 대답: '" + userAnswer + "'. 간단한 다음 질문을 생성해 주세요. "
                + "이전 질문: " + previousQuestions);

        String newQuestion;
        do {
            newQuestion = requestSpec.call().content().trim();
        } while (sessionContains(session, newQuestion));

        int questionCount = session.getAttribute("questionCount") == null ? 1 : (int) session.getAttribute("questionCount") + 1;
        session.setAttribute("question" + questionCount, newQuestion);
        session.setAttribute("questionCount", questionCount);

        return newQuestion;
    }

    // 세션에 중복 질문이 있는지 확인
    private boolean sessionContains(HttpSession session, String question) {
        for (int i = 1; i <= 10; i++) {
            String q = (String) session.getAttribute("question" + i);
            if (question.equals(q)) {
                return true;
            }
        }
        return false;
    }

    // 와인 추천 생성 메서드
    public String[] generateWineRecommendations(HttpSession session) {
        StringBuilder userPreferences = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            String userAnswer = (String) session.getAttribute("userAnswer" + i);
            if (userAnswer != null) {
                userPreferences.append(userAnswer).append(", ");
            }
        }

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        requestSpec.user("사용자의 대답: " + userPreferences + "에 따라 3가지 와인을 추천해 주세요.");

        String response = requestSpec.call().content();
        return response != null ? response.split(",") : new String[0];
    }
}
