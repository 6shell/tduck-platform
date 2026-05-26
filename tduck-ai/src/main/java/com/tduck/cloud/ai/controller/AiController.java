package com.tduck.cloud.ai.controller;

import cn.hutool.ai.core.Message;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tduck.cloud.ai.config.AiConfiguration;
import com.tduck.cloud.ai.constant.MessageRoleConstants;
import com.tduck.cloud.ai.request.FormulaGenerateRequest;
import com.tduck.cloud.ai.util.AiChatUtils;
import com.tduck.cloud.ai.vo.AiChatResponse;
import com.tduck.cloud.ai.vo.AiChatResult;
import com.tduck.cloud.common.util.JsonUtils;
import com.tduck.cloud.common.util.Result;
import com.tduck.cloud.form.service.UserFormItemService;
import com.tduck.cloud.form.service.UserFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.security.PermitAll;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class AiController {

    private final UserFormItemService userFormItemService;
    private final UserFormService userFormService;

    /**
     * 生成问卷ai
     *
     * @param msg 用户提示词
     */
    @GetMapping(value = "/ai/gen-form-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AiChatResponse> genFormChat(@RequestParam String msg, @RequestParam String model) {
        // msg为空直接关闭
        if (msg == null || msg.isEmpty()) {
            return Flux.empty();
        }
        String content = AiChatUtils.getPromptContent("form-prompt-deepseek.md");

        List<Message> messages = CollUtil.newArrayList(new Message(MessageRoleConstants.SYSTEM, content), new Message(MessageRoleConstants.USER, msg));

        return AiChatUtils.createChatFlux(messages, true);
    }

    /**
     * 测试生成问卷ai 同步返回
     *
     * @param msg 用户提示词
     */
    @PermitAll
    @GetMapping("/ai/test-gen-form")
    public Result<AiChatResult> testGenForm(@RequestParam String msg) {
        if (StrUtil.isBlank(msg)) {
            return Result.failed("提示词不能为空");
        }
        String content = AiChatUtils.getPromptContent("form-prompt-deepseek.md");
        List<Message> messages = CollUtil.newArrayList(
                new Message(MessageRoleConstants.SYSTEM, content),
                new Message(MessageRoleConstants.USER, msg)
        );
        AiChatResult chatResult = AiChatUtils.createChat(messages, true);
        return Result.success(chatResult);
    }


}
