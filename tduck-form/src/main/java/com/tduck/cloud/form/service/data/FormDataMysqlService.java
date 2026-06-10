package com.tduck.cloud.form.service.data;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tduck.cloud.common.entity.BaseEntity;
import com.tduck.cloud.common.entity.SysBaseEntity;
import com.tduck.cloud.common.util.SecurityUtils;
import com.tduck.cloud.form.entity.UserFormDataEntity;
import com.tduck.cloud.form.entity.struct.FormDataFilterStruct;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tduck.cloud.form.mapper.UserFormDataMapper;
import com.tduck.cloud.form.request.QueryFormResultRequest;
import com.tduck.cloud.form.vo.FormDataTableVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author : wangqing
 * @description : 表单数据基础服务
 * @create :  2022/07/04 14:25
 **/
@Service
@Slf4j
public class FormDataMysqlService extends FormDataBaseService {

    @Autowired
    private UserFormDataMapper userFormDataMapper;

    @Autowired(required = false)
    private TduckMongoTemplate mongoTemplate;


    @Override
    public Boolean valueExist(String formKey, String formItemId, Object value) {
        return userFormDataMapper.selectOriginalDataValueCount(formKey, formItemId, value) > 0;
    }

    @Override
    public Boolean syncSaveData(UserFormDataEntity result) {
        if (null == mongoTemplate) {
            return true;
        }
        mongoTemplate.save(convertDocument(result), result.getFormKey());
        return true;
    }


    @Override
    public Boolean asyncUpdateData(UserFormDataEntity result) {
        if (null == mongoTemplate) {
            return true;
        }
        mongoTemplate.updateById(convertDocument(result), result.getId(), result.getFormKey());
        return true;
    }

    @Override
    public void asyncDeleteData(List<String> idList, String formKey) {
        if (null == mongoTemplate) {
            return;
        }
        mongoTemplate.deleteByIds(idList, formKey);
    }


    @Override
    public FormDataTableVO search(QueryFormResultRequest request) {
        request.validateSqlInjection();
        // 校验formKey只允许存在字符串和数字
        if (StrUtil.isBlank(request.getFormKey()) || !request.getFormKey().matches("^[a-zA-Z0-9]+$")) {
            return new FormDataTableVO();
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFormDataEntity> wrapper = Wrappers.<UserFormDataEntity>lambdaQuery()
                .eq(UserFormDataEntity::getFormKey, request.getFormKey());

        // 查询指定id数据
        if (ObjectUtil.isNotNull(request.getDataIds()) && !request.getDataIds().isEmpty()) {
            wrapper.in(UserFormDataEntity::getId, request.getDataIds());
        }

        // 查询总数
        Long total = userFormDataMapper.selectCount(wrapper);

        wrapper.orderByDesc(UserFormDataEntity::getId);

        List<UserFormDataEntity> userFormDataEntities;
        // 分页
        if (ObjectUtil.isNotNull(request.getCurrent()) && ObjectUtil.isNotNull(request.getSize())) {
            Page<UserFormDataEntity> page = new Page<>(request.getCurrent() + 1, request.getSize(), false);
            userFormDataEntities = userFormDataMapper.selectPage(page, wrapper).getRecords();
        } else {
            userFormDataEntities = userFormDataMapper.selectList(wrapper);
        }

        // 过滤指定字段
        List<Map> maps = expandData(userFormDataEntities, request.getFilterFields());
        return new FormDataTableVO(maps, total);
    }


    /**
     * 判断sql是否合法
     *
     * @param str the string
     * @return true 不合法 false 合法
     */
    protected static boolean sqlValidate(String str) {
        String badStr = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|" + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute|\\>|\\<)\\b)";
        Pattern compile = Pattern.compile(badStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compile.matcher(str);
        //使用正则表达式进行匹配
        return matcher.find();
    }


    @Override
    public List<Map> searchAll(QueryFormResultRequest request) {
        request.validateSqlInjection();
        // 校验formKey只允许存在字符串和数字
        if (StrUtil.isBlank(request.getFormKey()) || !request.getFormKey().matches("^[a-zA-Z0-9]+$")) {
            return new ArrayList<>();
        }
        List<UserFormDataEntity> userFormDataEntities = userFormDataMapper.selectList(
                Wrappers.<UserFormDataEntity>lambdaQuery()
                        .eq(UserFormDataEntity::getFormKey, request.getFormKey())
        );
        return expandData(userFormDataEntities, null);
    }


    /**
     * 展开数据为一级
     */
    public List<Map> expandData(List<UserFormDataEntity> userFormDataEntities, String[] filterFields) {
        return userFormDataEntities.stream().map(item -> {
            Map<String, Object> processData = item.getOriginalData();
            Map<String, Object> resultMap = BeanUtil.beanToMap(item);
            resultMap.remove(UserFormDataEntity.Fields.originalData);
            resultMap.put(BaseEntity.Fields.createTime, LocalDateTimeUtil.formatNormal(item.getCreateTime()));
            resultMap.put(BaseEntity.Fields.updateTime, LocalDateTimeUtil.formatNormal(item.getUpdateTime()));
            processData.putAll(resultMap);
            // 只过滤指定字段
            if (filterFields != null) {
                Map<String, Object> filterMap = MapUtil.newHashMap();
                for (String filterField : filterFields) {
                    filterMap.put(filterField, processData.get(filterField));
                }
                return filterMap;
            }
            return processData;
        }).collect(Collectors.toList());
    }


}
