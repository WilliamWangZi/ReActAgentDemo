package com.example.reactagentdemo.aitool;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class WriteFileTool implements Tool {

    private static final String PATH = "path";
    private static final String CONTENT = "content";

    @Override
    public String getToolName() {
        return "write_to_file";
    }

    @Override
    public String getToolPromptDefine() {
        return "{\"write_to_file\": {\"description\":\"写入内容到文件\",\"param\": {\"path\": \"\", \"content\": \"\"}}}";
    }

    @Override
    public String execute(Map<String, Object> params) {
        if (params.isEmpty()) {
            return "写入失败，没有找到参数";
        }
        if (!params.containsKey(PATH)) {
            return "写入失败，缺少path参数";
        }
        if (!params.containsKey(CONTENT)) {
            return "写入失败，缺少content参数";
        }
        try {
            // 核心：写入文件（覆盖模式，默认UTF-8编码）
            // StandardCharsets.UTF_8 必须指定，避免系统默认编码（如GBK）导致乱码
            Files.writeString(
                    Paths.get(String.valueOf(params.get(PATH))),
                    String.valueOf(params.get(CONTENT)),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return "写入成功";
    }
}
