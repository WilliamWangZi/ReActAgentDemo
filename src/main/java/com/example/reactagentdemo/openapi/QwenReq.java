package com.example.reactagentdemo.openapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QwenReq {

    private String model;

    private List<Message> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {

        private String role;

        private String content;
    }


}
