package com.developkim.sommelierbot.service;

import com.developkim.sommelierbot.util.Wine;
import com.developkim.sommelierbot.util.WineCrawler;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final WineCrawler wineCrawler; // WineCrawler 추가

    public String generateFirstQuestion() {
        return chatClient.prompt().user("첫 번째 질문을 간단히 해 주세요.").call().content().trim();
    }

    public String generateNextQuestion(String userAnswer, HttpSession session) {
        StringBuilder previousQuestions = new StringBuilder();
        for (int i = 1; i <= 7; i++) {
            String question = (String) session.getAttribute("question" + i);
            if (question != null) {
                previousQuestions.append("질문 ").append(i).append(": ").append(question).append(". ");
            }
        }

        String newQuestion;
        do {
            newQuestion = chatClient.prompt()
                    .user("사용자의 대답: '" + userAnswer + "'. 간단한 다음 질문을 생성해 주세요. 이전 질문: " + previousQuestions.toString())
                    .call().content().trim();
        } while (isQuestionAlreadyAsked(session, newQuestion));

        int questionCount = (int) (session.getAttribute("questionCount") == null ? 0 : session.getAttribute("questionCount"));
        session.setAttribute("question" + (questionCount + 1), newQuestion);
        session.setAttribute("questionCount", questionCount + 1);

        return newQuestion;
    }

    private boolean isQuestionAlreadyAsked(HttpSession session, String question) {
        for (int i = 1; i <= 7; i++) {
            String existingQuestion = (String) session.getAttribute("question" + i);
            if (question.equals(existingQuestion)) {
                return true;
            }
        }
        return false;
    }

    public void generateWineRecommendations(HttpSession session) throws IOException {
        StringBuilder userPreferences = new StringBuilder();
        for (int i = 1; i <= 7; i++) {
            String userAnswer = (String) session.getAttribute("userAnswer" + i);
            if (userAnswer != null) {
                userPreferences.append(userAnswer).append(", ");
            }
        }

        String response = chatClient.prompt()
                .user("사용자의 답변을 바탕으로 추천할 와인 3가지를 아래와 같은 형식으로 알려줘\n" +
                        "와인이름(품종)," +
                        "와인이름(품종)," +
                        "와인이름(품종)," +
                        "와인 이름과 품종만 한글로 간단하게 작성해줘 질문하지마 " + userPreferences)
                .call().content();

        String[] wineNames = response != null
                ? response.replaceAll(".*?\\d+\\. \\*\\*(.*?)\\*\\*.*", "$1").trim().split(", ")
                : new String[0];
        log.info(Arrays.toString(wineNames));
        // 크롤링하여 와인 정보를 가져오기
        List<Wine> recommendedWines = wineCrawler.crawlWinesForNames(wineNames);
        session.setAttribute("recommendedWines", recommendedWines);
    }

}
