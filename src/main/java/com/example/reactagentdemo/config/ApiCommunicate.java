package com.example.reactagentdemo.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiCommunicate {

    private String question;
    private String thought;
    private Action action;
    private String observation;
    private String final_answer;

    @Data
    public static class Action {
        private String functionName;

        private Map<String, Object> param;
    }

}
