package com.feng.subject.domain.strategy;

import com.feng.subject.common.enums.SubjectInfoTypeEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 题目类型工厂
 * @author: txf
 * @date: 2023/10/5
 */

/*
InitializingBean 初始化bean对象
 */
@Component
public class SubjectTypeHandlerFactory implements InitializingBean {
    // 自动装配，容器中有四个实现类了！！！
    @Resource
    private List<SubjectTypeHandler> subjectTypeHandlerList;

    private final Map<SubjectInfoTypeEnum, SubjectTypeHandler> handlerMap = new HashMap<>();

    public SubjectTypeHandler getHandler(int subjectType) {
        // 根据前端传递过来的subjectType字段得到题目类型
        return handlerMap.get(SubjectInfoTypeEnum.getByCode(subjectType));
    }

    /*
    bean加载完成之后，会做这个方法
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // SubjectTypeHandlerFactory被spring容器装载后初始化，往map中添加数据
        for (SubjectTypeHandler subjectTypeHandler : subjectTypeHandlerList) {
            handlerMap.put(subjectTypeHandler.getHandlerType(), subjectTypeHandler);
        }
    }

}
