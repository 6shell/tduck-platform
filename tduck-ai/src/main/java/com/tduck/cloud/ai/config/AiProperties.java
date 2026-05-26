package com.tduck.cloud.ai.config;

import lombok.Data;

/**
 * ai配置
 *
 * @author tduck
 * @since 2025-3-31 17:52:01
 */
@Data
public class AiProperties {
    /**
     * 基础地址
     */
    public String baseUrl = "https://api.deepseek.com/v1/";
    /**
     * api key
     */
    public String apiKey;


    /**
     * 模型名称
     */
    public String modelName;

    public boolean enabled;


}
