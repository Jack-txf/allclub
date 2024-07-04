package com.feng.subject.infra.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.feng.subject.infra.basic.entity.SubjectLiked;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目点赞表 表数据库访问层
 *
 * @author 田小锋
 * @since 2024-01-07 23:08:45
 */
@Repository
public interface SubjectLikedDao extends BaseMapper<SubjectLiked> {

    int insertBatch(@Param("entities") List<SubjectLiked> entities);

    int countByCondition(SubjectLiked subjectLiked);

    List<SubjectLiked> queryPage(@Param("entity") SubjectLiked subjectLiked,
                                 @Param("start") int start,
                                 @Param("pageSize") Integer pageSize);

    void batchInsertOrUpdate(@Param("entities") List<SubjectLiked> subjectLikedList);

}

