package com.example.reactagentdemo.aitool;

import java.util.Map;

public interface Tool {

    String getToolName();

    String getToolPromptDefine();

    String execute(Map<String, Object> params);

}
