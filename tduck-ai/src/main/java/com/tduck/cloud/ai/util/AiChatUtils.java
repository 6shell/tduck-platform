package com.tduck.cloud.ai.util;

import cn.hutool.ai.core.AIService;
import cn.hutool.ai.core.Message;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.ai.config.AiConfiguration;
import com.tduck.cloud.ai.vo.AiChatResponse;
import com.tduck.cloud.ai.vo.AiChatResult;
import com.tduck.cloud.common.util.JsonUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * AI聊天工具类
 *
 * @author tduck
 */
@UtilityClass
@Slf4j
public class AiChatUtils {

    /**
     * 获取提示词内容
     *
     * @param fileName 提示词文件名（相对于 classpath）
     * @return 提示词内容
     */
    public String getPromptContent(String fileName) {
        try {
            ClassPathResource classPathResource =
                    new ClassPathResource(fileName);
            return StreamUtils.copyToString(
                    classPathResource.getStream(),
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            log.error("AiChatUtils getPromptContent exception, fileName: {}", fileName, e);
            throw new RuntimeException("读取提示词文件失败: " + fileName, e);
        }
    }

    /**
     * 创建AI聊天流式响应
     *
     * @param messages 消息列表
     * @return Flux流式响应
     */
    /**
     * 创建AI聊天流式响应
     *
     * @param messages 消息列表
     * @return Flux流式响应
     */
    public Flux<AiChatResponse> createChatFlux(List<Message> messages) {
        return createChatFlux(messages, true);
    }

    /**
     * 创建AI聊天流式响应
     *
     * @param messages 消息列表
     * @param json     是否强制json
     * @return Flux流式响应
     */
    public Flux<AiChatResponse> createChatFlux(List<Message> messages, boolean json) {
        return Flux.create(sink -> {
            try {
                AIService aiService = json ? AiConfiguration.getAiServiceJson() : AiConfiguration.getAiServiceText();
                StringBuilder fullContent = new StringBuilder();
                aiService.chat(messages, chunk -> {
                    log.debug("AiChatUtils createChatFlux chunk: {}", chunk);
                    if ("data: [DONE]".equals(chunk)) {
                        log.info("AiChatUtils createChatFlux full content: {}", fullContent.toString());
                        sink.complete();
                    } else {
                        AiChatResponse response = AiChatResponse.build(chunk);
                        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                            AiChatResponse.Choice choice = response.getChoices().get(0);
                            if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                                fullContent.append(choice.getDelta().getContent());
                            }
                        }
                        sink.next(response);
                    }
                });
            } catch (Exception e) {
                log.error("AiChatUtils createChatFlux exception", e);
                sink.error(e);
            }
        });
    }

    /**
     * 创建AI聊天流式响应
     *
     * @param messages 消息列表
     * @return Flux流式响应
     */
    public AiChatResult createChat(List<Message> messages) {
        return createChat(messages, true);
    }

    /**
     * 创建AI聊天流式响应
     *
     * @param messages 消息列表
     * @param json     是否强制json
     * @return Flux流式响应
     */
    public AiChatResult createChat(List<Message> messages, boolean json) {
        AIService aiService = json ? AiConfiguration.getAiServiceJson() : AiConfiguration.getAiServiceText();
        String res = aiService.chat(messages);
        if (StrUtil.isBlank(res)) {
            return new AiChatResult();
        }
        log.debug(res);
        return JsonUtils.jsonToObj(res, AiChatResult.class);
    }


}
