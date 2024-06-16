package com.feng.subject.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目dto
 *
 * @author: txf
 * @date: 2023/10/5
 */
@Data
public class SubjectOptionBO implements Serializable {

    /**
     * 题目答案  -----------
     */
    //  简答题专有
    private String subjectAnswer;

    /**
     * 答案选项
     */
    // 选择题，判断题专有
    private List<SubjectAnswerBO> optionList;

}

