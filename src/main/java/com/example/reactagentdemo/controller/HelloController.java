package com.example.reactagentdemo.controller;

import com.example.reactagentdemo.aitool.Tool;
import com.example.reactagentdemo.config.ApiCommunicate;
import com.example.reactagentdemo.config.SystemPromptLoader;
import com.example.reactagentdemo.openapi.PromptConvert;
import com.example.reactagentdemo.openapi.QwenAiApi;
import com.example.reactagentdemo.openapi.QwenReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class HelloController {

    @Autowired
    private SystemPromptLoader systemPromptLoader;

    @Autowired
    private List<Tool> toolList;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();




    @GetMapping("/hello")
    public String hello(@RequestParam(value = "input") String userInput) throws Exception {

        String sysmtePrompt = systemPromptLoader.getSystemPrompt();
        ApiCommunicate question = new ApiCommunicate();
        question.setQuestion(userInput);
        List<QwenReq.Message> messageList = Lists.newArrayList(
                PromptConvert.createSystemPromptObj(sysmtePrompt),
                PromptConvert.createUserPromptObj(OBJECT_MAPPER.writeValueAsString(question))
        );
        while (true) {
            String aiOutput = QwenAiApi.callQwen(messageList);
            System.out.println(aiOutput);
            ApiCommunicate result = OBJECT_MAPPER.readValue(aiOutput, ApiCommunicate.class);
            if (StringUtils.isNotBlank(result.getFinal_answer())) {
                return result.getFinal_answer();
            }
            ApiCommunicate.Action action = result.getAction();
            if (action == null) {
                return "缺失action";
            }
            messageList.add(PromptConvert.createAssistantPromptObj(aiOutput));
            for (Tool tool : toolList) {
                if (tool.getToolName().equals(action.getFunctionName())) {
                    String toolResult = tool.execute(action.getParam());
                    ApiCommunicate apiCommunicate = new ApiCommunicate();
                    apiCommunicate.setObservation(toolResult);
                    String observation = OBJECT_MAPPER.writeValueAsString(apiCommunicate);
                    System.out.println(observation);
                    messageList.add(PromptConvert.createUserPromptObj(observation));
                }
            }
        }
    }
}