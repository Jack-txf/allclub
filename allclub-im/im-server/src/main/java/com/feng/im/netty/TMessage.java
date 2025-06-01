package com.feng.im.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 消息类
 * @Author: txf
 * @Date: 2025/5/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public TMessage( String systemMsg ) {
        this.type = "1";
        this.from = "system";
        this.content = systemMsg;
    }

    private String type; // 消息类型 1:系统消息 2:聊天消息
    private String from; // 消息发送者的id[ 如果是用户就是用户ID,如果是系统消息,那么就是"system" ]
    private List<String> to; // 消息接收者的id
    private Long time; // 消息发送的时间戳
    // 如果是聊天消息-------------------------------------
    private String content; // 消息内容 --- 【聊天消息】
    // 如果是系统消息-------------------------------------

}
