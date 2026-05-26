package com.tduck.cloud.ai.config;

import cn.hutool.ai.AIServiceFactory;
import cn.hutool.ai.ModelName;
import cn.hutool.ai.Models;
import cn.hutool.ai.core.AIConfig;
import cn.hutool.ai.core.AIConfigBuilder;
import cn.hutool.ai.core.AIService;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.common.util.JsonUtils;
import com.tduck.cloud.common.util.SpringContextUtils;
import com.tduck.cloud.envconfig.constant.ConfigConstants;
import com.tduck.cloud.envconfig.service.SysEnvConfigService;
import lombok.Getter;

/**
 * api配置
 *
 * @author : tduck
 * @since 2025-3-31 17:50:14
 **/
public class AiConfiguration {


    @Getter
    private static AiProperties aiProperties;

    @Getter
    private static AIService aiServiceJson;

    @Getter
    private static AIService aiServiceText;

    static {
        buildDeepSeek();
    }

    public static synchronized void buildDeepSeek() {
        String configJson = SpringContextUtils.getBean(SysEnvConfigService.class).getValueByKey(ConfigConstants.DEEP_SEEK_ENV_CONFIG);
        if (StrUtil.isBlank(configJson)) {
            return;
        }
        aiProperties = JsonUtils.jsonToObj(configJson, AiProperties.class);
        if (ObjectUtil.isNull(aiProperties)) {
            return;
        }
        // json
        AIConfigBuilder jsonConfigBuilder = new AIConfigBuilder(ModelName.DEEPSEEK.getValue())
                //设置apiKey
                .setApiKey(aiProperties.getApiKey())
                .setApiUrl(aiProperties.getBaseUrl())
                //指定具体模型
                .setModel(StrUtil.emptyToDefault(aiProperties.getModelName(), Models.DeepSeek.DEEPSEEK_CHAT.getModel()));

        // 如果是 DeepSeek 官方域名才开启严格的 JSON 模式，非官方/第三方代理则关闭以保证兼容性
        boolean isDeepSeekOfficial = StrUtil.isNotBlank(aiProperties.getBaseUrl())
                && StrUtil.containsIgnoreCase(aiProperties.getBaseUrl(), "api.deepseek.com");
        if (isDeepSeekOfficial) {
            jsonConfigBuilder.putAdditionalConfig("response_format", MapUtil.of("type", "json_object"));
        }

        AIConfig config = jsonConfigBuilder.build();
        aiServiceJson = AIServiceFactory.getAIService(config);

        // text
        AIConfig configText = new AIConfigBuilder(ModelName.DEEPSEEK.getValue())
                //设置apiKey
                .setApiKey(aiProperties.getApiKey())
                .setApiUrl(aiProperties.getBaseUrl())
                //指定具体模型
                .setModel(StrUtil.emptyToDefault(aiProperties.getModelName(), Models.DeepSeek.DEEPSEEK_CHAT.getModel()))
                .build();
        aiServiceText = AIServiceFactory.getAIService(configText);
    }


}
