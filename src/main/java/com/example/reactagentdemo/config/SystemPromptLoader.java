package com.example.reactagentdemo.config;

import com.example.reactagentdemo.aitool.Tool;
import jakarta.annotation.PostConstruct;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemPromptLoader {

    private static final String SYSTEM_PROMPT_FILE_PATH = "./SystemPrompt";

    @Autowired
    private List<Tool> toolList;

    private String systemPrompt;

    @PostConstruct
    public void loadSystemPrompt() {
        String systemPromptTemplate = readSystemPromptFile(SYSTEM_PROMPT_FILE_PATH);
        StringBuilder sb = new StringBuilder();
        for (Tool tool : toolList) {
            sb.append(tool.getToolPromptDefine()).append(";");
        }
        Map<String, String> variables = new HashMap<>();
        variables.put("tool_list", sb.toString());

        // 4. 替换模板变量（类似Python String.Template.substitute）
        StringSubstitutor substitutor = new StringSubstitutor(variables);
        systemPrompt = substitutor.replace(systemPromptTemplate);
    }


    public String getSystemPrompt() {
        return systemPrompt;
    }

    public static String readSystemPromptFile(String filePath) {
        try {
            // 获取classpath下文件的绝对路径（解决打包后路径问题）
            String classpathPath = SystemPromptLoader.class.getClassLoader().getResource(filePath).getPath();
            // 读取整个文件，指定UTF-8编码
            return Files.readString(Paths.get(classpathPath), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("读取systemPrompt文件失败", e);
        }
    }
}


