package com.tduck.cloud.ai.vo;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tduck.cloud.common.util.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * AI 聊天响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiChatResponse {

    /**
     * 响应唯一标识
     */
    private String id;

    /**
     * 对象类型，如 "chat.completion.chunk"
     */
    private String object;

    /**
     * 创建时间戳
     */
    private Long created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 系统指纹
     */
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    public static AiChatResponse build(String chunk) {
        if (StrUtil.isNotBlank(chunk)) {
            // 处理 SSE 格式：去掉 "data: " 前缀
            String jsonData = chunk;
            if (chunk.startsWith("data: ")) {
                jsonData = chunk.substring(6).trim();
            }
            return JsonUtils.jsonToObj(jsonData, AiChatResponse.class);
        }
        return new AiChatResponse();
    }


    public static AiChatResponse of(String content) {
        Delta delta = new Delta();
        delta.setContent(content);
        Choice choice = new Choice();
        choice.setDelta(delta);
        return AiChatResponse.builder()
                .choices(Collections.singletonList(choice))
                .build();
    }

    @Data
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 增量内容
         */
        private Delta delta;

        /**
         * 日志概率
         */
        private Object logprobs;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    public static class Delta {
        /**
         * 内容
         */
        private String content;

        /**
         * 角色
         */
        private String role;
    }
}
