package com.feng.subject.domain.entity;

import com.feng.subject.common.eneity.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 题目dto
 *
 * @author: 田小锋
 * @date: 2023/10/5
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SubjectInfoBO extends PageInfo implements Serializable {

    /**
     * 主键
     */
    private Long id;
    /**
     * 题目名称
     */
    private String subjectName;
    /**
     * 题目难度
     */
    private Integer subjectDifficult;
    /**
     * 出题人名
     */
    private String settleName;
    /**
     * 题目类型 1单选 2多选 3判断题目 4简答题目
     */
    private Integer subjectType;
    /**
     * 题目分数
     */
    private Integer subjectScore;
    /**
     * 题目解析
     */
    private String subjectParse;

    /**
     * 题目答案
     */
    private String subjectAnswer;

    /**
     * 分类id
     */
    private List<Integer> categoryIds;

    /**
     * 标签id
     */
    private List<Integer> labelIds;
    /**
     * 标签name
     */
    private List<String> labelName;

    /**
     * 答案选项
     */
    private List<SubjectAnswerBO> optionList;

    // ===额外信息--------
    private Long categoryId;
    private Long labelId;
    private String keyWord;

    //=============排行榜
    /**
     * 创建人昵称
     */
    private String createUser;
    /**
     * 创建人头像
     */
    private String createUserAvatar;
    /**
     * 题目数量
     */
    private Integer subjectCount;
    //=======================================end

    //==========点赞
    /**
     * 是否被当前用户点赞
     */
    private Boolean liked;
    /**
     * 当前题目点赞的数量
     */
    private Integer likedCount;
    //====================end
    /**
     * 下一个题
     */
    private Long nextSubjectId;
    /**
     * 上一个题
     */
    private Long lastSubjectId;

}

