package com.tduck.cloud.ai.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 公式生成请求参数
 *
 * @author tduck
 */
@Data
public class FormulaGenerateRequest {

    /**
     * 表单Key
     */
    @NotBlank(message = "表单Key不能为空")
    private String formKey;

    /**
     * 参与公式的题目ID数组
     */
    @NotEmpty(message = "题目ID数组不能为空")
    private List<String> formItemIds;

    /**
     * 用户描述的需求
     */
    @NotBlank(message = "需求描述不能为空")
    private String description;

    /**
     * 公式类型（用于选择不同的提示词模板）
     * 可选值：calculation（计算公式）、validation（验证公式）、formatting（格式化公式）、logic（逻辑公式）
     * 默认为 calculation
     */
    private String type = "calculation";

    /**
     * 自定义函数描述
     */
    private String customFunctions;
}
