package com.example.reactagentdemo.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class QwenResp {

    // 响应选项列表
    private List<Choice> choices;
    // 响应类型
    private String object;
    // 令牌使用统计
    private Usage usage;
    // 创建时间戳
    private Long created;
    // 系统指纹
    private String system_fingerprint;
    // 模型名称
    private String model;
    // 响应ID
    private String id;

    // 嵌套：Choices数组元素类
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Message message;
        private String finish_reason;
        private Integer index;
        private Object logprobs;

        // 嵌套：Message消息类
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {
            private String role;
            private String content;
        }
    }

    // 嵌套：令牌使用统计类
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
        private PromptTokensDetails prompt_tokens_details;

        // 嵌套：Prompt令牌详情类
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PromptTokensDetails {
            private Integer cached_tokens;
        }
    }
}
