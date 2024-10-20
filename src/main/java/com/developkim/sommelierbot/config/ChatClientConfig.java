package com.developkim.sommelierbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("너는 와인을 추천해주는 AI야. 사용자에게 질문을 할 때, 간결하고 다양하게 질문해. 사용자의 대답에 따라 다른 질문을 해줘.")
                .build();
    }
}
