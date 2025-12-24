package com.example.reactagentdemo.controller;

import com.example.reactagentdemo.aitool.Tool;
import com.example.reactagentdemo.config.ApiCommunicate;
import com.example.reactagentdemo.config.SystemPromptLoader;
import com.example.reactagentdemo.infra.LangChain4jChromaQwenRAG;
import com.example.reactagentdemo.openapi.PromptConvert;
import com.example.reactagentdemo.openapi.QwenAiApi;
import com.example.reactagentdemo.openapi.QwenReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
public class HelloController {

    @Autowired
    private SystemPromptLoader systemPromptLoader;

    @Autowired
    private List<Tool> toolList;

    @Autowired
    private LangChain4jChromaQwenRAG langChain4jChromaQwenRAG;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();




    @GetMapping("/hello")
    public String hello(@RequestParam(value = "input") String userInput) throws Exception {

        List<EmbeddingMatch<TextSegment>> matches = langChain4jChromaQwenRAG.retrieve(userInput, 5);
        List<String> contexts = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());
        String sysmtePrompt = systemPromptLoader.getSystemPrompt();
        ApiCommunicate question = new ApiCommunicate();
        question.setQuestion("可参考信息:" + OBJECT_MAPPER.writeValueAsString(contexts) + "\n" + userInput);
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