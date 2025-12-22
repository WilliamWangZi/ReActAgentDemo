package com.example.reactagentdemo.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class QwenAiApi {
    // 替换为自己的阿里云 API Key（从阿里云百炼控制台获取）
    private static String DASHSCOPE_API_KEY;
    // 通义千问聊天接口地址
    private static final String QWEN_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    static {

        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        System.out.println("读取到apiLey：" + apiKey);
        DASHSCOPE_API_KEY = apiKey;
    }


    /**
     * 调用通义千问聊天接口
     * @param messages 大模型输入内容
     * @return 大模型回复内容
     */
    public static String callQwen(List<QwenReq.Message> messages) throws Exception {

        // 构建完整请求对象
        QwenReq qwenReq = QwenReq.builder().model("qwen3-coder-plus")
                .messages(messages)
                .build();
        // 2. 构建 HTTP 请求（核心：header 中携带 API Key）
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(QWEN_URL))
                .POST(HttpRequest.BodyPublishers.ofString(
                        OBJECT_MAPPER.writeValueAsString(qwenReq),
                        StandardCharsets.UTF_8
                ))
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + DASHSCOPE_API_KEY) // 通义千问的鉴权方式
                .build();

        // 3. 发送请求并解析响应
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        QwenResp result = OBJECT_MAPPER.readValue(response.body(), QwenResp.class);

        // 4. 解析回复内容
        List<QwenResp.Choice> choices = Optional.ofNullable(result)
                .map(QwenResp::getChoices).orElse(null);
        if (choices != null && choices.size() != 0) {
            return choices.get(0).getMessage().getContent();
        }
        throw new RuntimeException("调用失败：" + result);
    }

//    // 测试主方法
//    public static void main(String[] args) {
//        try {
//            String prompt = "用Java实现一个简单的HTTP GET请求";
//            String reply = callQwen(prompt);
//            System.out.println("通义千问回复：\n" + reply);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("调用出错：" + e.getMessage());
//        }
//    }

    //{"write_to_file": {"description":"写入内容到文件","param": {"path": "", "content": ""}}}
}