package com.example.reactagentdemo.openapi;

public class PromptConvert {


    private static final String SYSTEM_PROMPT_KEY = "system";
    private static final String USER_PROMPT_KEY = "user";
    private static final String ASSISTANT_PROMPT_KEY = "assistant";


    public static QwenReq.Message createSystemPromptObj(String prompt) {
        return QwenReq.Message.builder().role(SYSTEM_PROMPT_KEY).content(prompt).build();
    }

    public static QwenReq.Message createUserPromptObj(String prompt) {
        return QwenReq.Message.builder().role(USER_PROMPT_KEY).content(prompt).build();
    }

    public static QwenReq.Message createAssistantPromptObj(String prompt) {
        return QwenReq.Message.builder().role(ASSISTANT_PROMPT_KEY).content(prompt).build();
    }
}
