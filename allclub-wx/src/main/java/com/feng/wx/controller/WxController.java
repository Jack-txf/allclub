package com.feng.wx.controller;

import com.feng.wx.handler.WxChatMsgFactory;
import com.feng.wx.handler.WxChatMsgHandler;
import com.feng.wx.utils.MessageUtil;
import com.feng.wx.utils.SHA1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/*
 这个需要开启内网穿透，一定要记得哦。！！！
 微信的测试号：
 https://mp.weixin.qq.com/debug/cgi-bin/sandboxinfo?action=showinfo&t=sandbox/index
 */
@RestController
@RequestMapping("/wx/")
@Slf4j
public class WxController {
    @Resource
    private WxChatMsgFactory wxChatMsgFactory;
    private static final String token = "txfnbnbnbnb";
    /*
     * 回调消息校验 wx配置！！！！！！
     */
    @GetMapping("callback")
    public String callback(@RequestParam("signature") String signature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestParam("echostr") String echostr) {
        log.info("get验签请求参数：signature:{}，timestamp:{}，nonce:{}，echostr:{}",
                signature, timestamp, nonce, echostr);
        String shaStr = SHA1.getSHA1(token, timestamp, nonce, "");
        if (signature.equals(shaStr)) {
            return echostr;
        }
        return "unknown";
    }


    /*
     事件处理
     */
    @PostMapping(value = "callback", produces = "application/xml;charset=UTF-8")
    public String callback(
            @RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam(value = "msg_signature", required = false) String msgSignature) {
        log.info("接收到微信消息：requestBody：{}", requestBody);
        Map<String, String> messageMap = MessageUtil.parseXml(requestBody); // 请求体是xml格式的
        String msgType = messageMap.get("MsgType"); // 消息有类型
        String event = messageMap.get("Event") == null ? "" : messageMap.get("Event");
        log.info("msgType:{},event:{}", msgType, event);

        StringBuilder sb = new StringBuilder();
        sb.append(msgType);
        if (!event.isEmpty()) { // 事件不为空
            sb.append(".");
            sb.append(event);
        }
        String msgTypeKey = sb.toString();
        // 策略+工厂模式处理消息类型
        WxChatMsgHandler wxChatMsgHandler = wxChatMsgFactory.getHandlerByMsgType(msgTypeKey);
        if (Objects.isNull(wxChatMsgHandler)) {
            return "unknown";
        }
        String replyContent = wxChatMsgHandler.dealMsg(messageMap);
        log.info("replyContent:{}", replyContent);
        return replyContent;
    }
}
