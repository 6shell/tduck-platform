package com.tduck.cloud.ai.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI 非流式聊天响应结果
 *
 * @author tduck
 */
@Data
public class AiChatResult {

    /**
     * 响应唯一标识
     */
    private String id;

    /**
     * 对象类型
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
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * 使用情况
     */
    private Usage usage;

    /**
     * 系统指纹
     */
    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    /**
     * 快速获取第一个消息
     *
     * @return Message
     */
    public Message getFirstMessage() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).getMessage();
        }
        return null;
    }

    @Data
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息内容
         */
        private Message message;

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
    public static class Message {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }

    @Data
    public static class Usage {
        /**
         * 提示词Token数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成Token数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总Token数
         */
        /**
         * 总Token数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;

        /**
         * 提示词Token详情
         */
        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;

        /**
         * 提示词缓存命中Token数
         */
        @JsonProperty("prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;

        /**
         * 提示词缓存未命中Token数
         */
        @JsonProperty("prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;
    }

    @Data
    public static class PromptTokensDetails {
        /**
         * 缓存Token数
         */
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;
    }
}
