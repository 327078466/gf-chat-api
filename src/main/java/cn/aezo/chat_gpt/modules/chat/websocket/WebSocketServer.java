package cn.aezo.chat_gpt.modules.chat.websocket;

import cn.aezo.chat_gpt.entity.PromptType;
import cn.aezo.chat_gpt.modules.chat.ChatService;
import cn.aezo.chat_gpt.service.PromptTypeService;
import cn.aezo.chat_gpt.util.MiscU;
import cn.aezo.chat_gpt.util.Result;
import cn.aezo.chat_gpt.util.SpringU;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.SessionException;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 通过WSS等流式响应时，可选择模型gpt-3.5-turbo<br/>
 * 更多模型参考<br/>
 * @see com.unfbx.chatgpt.entity.chat.ChatCompletion.Model
 */
@Slf4j
@Component
@ServerEndpoint("/tools/chat/user/{uid}/{mode}/{prompt}")
public class WebSocketServer {

    private static OpenAiStreamClient OpenAiStreamClient;
    private static ChatService ChatService;

    private static PromptTypeService promptTypeService;

    @Autowired
    public void setOrderService(OpenAiStreamClient openAiStreamClient, ChatService chatService,PromptTypeService promptTypeService) {
        WebSocketServer.OpenAiStreamClient = openAiStreamClient;
        WebSocketServer.ChatService = chatService;
        WebSocketServer.promptTypeService = promptTypeService;
    }

    //在线总数
    private static int OnlineCount;
    //当前会话
    private Session session;
    //用户id
    private String uid;
    // 请求模式（M1 GPT3.5  M2 GPT3.5高级 M3 GPT4.0）
    private String mode;
    // 提示词限定角色
    private String prompt;

    private static CopyOnWriteArraySet<WebSocketServer> WebSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 用来存放每个客户端对应的WebSocketServer对象
     */
    private static ConcurrentHashMap<String, WebSocketServer> WebSocketMap = new ConcurrentHashMap<>();

    /**
     * 为了保存在线用户信息，在方法中新建一个list存储一下【实际项目依据复杂度，可以存储到数据库或者缓存】
     */
    private final static List<Session> SESSIONS = Collections.synchronizedList(new ArrayList<>());

    /**
     * 建立连接
     * @param session
     * @param uid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid,@PathParam("mode") String mode,@PathParam("prompt") String prompt) {
        this.session = session;
        this.uid = uid;
        this.mode = mode;
        if(!prompt.equals("0")){
            List<Message> messages = new ArrayList<>();
            PromptType load = promptTypeService.load(Integer.valueOf(prompt));
            // 设置当选择后自动加一
            load.setChoiceNum(Integer.valueOf(load.getChoiceNum()) + 1 +"");
            promptTypeService.update(load);
            Message message = new Message();
            message.setContent(load.getContentZh());
            message.setRole(Message.Role.SYSTEM.getName());
            messages.add(message);
            MessageLocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), MessageLocalCache.TIMEOUT);
        }

        WebSocketServer.WebSocketSet.add(this);
        WebSocketServer.SESSIONS.add(session);
        if (WebSocketServer.WebSocketMap.containsKey(uid)) {
            WebSocketServer.WebSocketMap.remove(uid);
            WebSocketServer.WebSocketMap.put(uid, this);
        } else {
            WebSocketServer.WebSocketMap.put(uid, this);
            addOnlineCount();
        }
        log.info("[连接ID:{}] 建立连接, 当前连接数:{}", this.uid, getOnlineCount());
    }

    /**
     * 断开连接
     */
    @OnClose
    public void onClose() {
        WebSocketServer.WebSocketSet.remove(this);
        if (WebSocketServer.WebSocketMap.containsKey(uid)) {
            WebSocketServer.WebSocketMap.remove(uid);
            subOnlineCount();
        }
        log.info("[连接ID:{}] 断开连接, 当前连接数:{}", uid, getOnlineCount());
    }

    /**
     * 接收到客户端消息
     * @param msg
     */
    @SneakyThrows
    @OnMessage
    public void onMessage(String msg) {
        log.info("[连接ID:{}] 收到消息:{}", this.uid, msg);
        if(StrUtil.isBlank(msg)) {
            return;
        }
        if("PING".equalsIgnoreCase(msg.trim())) {
            session.getBasicRemote().sendText("PONG");
            return;
        }
//        过滤敏感词
//        if(SensitiveWordHelper.contains(msg)){
//            session.getBasicRemote().sendText("问题中出现敏感词，请重新输入");
//            return;
//        }
        Result result = WebSocketServer.ChatService.checkAndUpdateAsset(this.uid);
        if(Result.isFailure(result)) {
            session.getBasicRemote().sendText(getErrorMsg(result.getMessage(), result.getCodeKey()));
            return;
        }
        Environment environment = SpringU.getBean(Environment.class);
        if("false".equals(environment.getProperty("sq-mini-tools.openai.enable"))) {
            session.getBasicRemote().sendText("此为模拟返回数据...");
            return;
        }
        String mode = WebSocketServer.WebSocketMap.get(this.uid).mode;
        //接受参数
        OpenAIWebSocketEventSourceListener eventSourceListener = new OpenAIWebSocketEventSourceListener(this.session);
        String messageContext = (String) MessageLocalCache.CACHE.get(uid);
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                Message message = messages.get(0); // 第一句话保留 可能是角色定义
                messages = messages.subList(1, 10);
                messages.add(0,message); // 第一位设置
            }
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
        } else {
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
        }
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(messages).stream(true).model(ChatCompletion.Model.GPT_3_5_TURBO_0613.getName()).build();
        // 设置请求模型
        if(mode.equals("2")){
             chatCompletion = ChatCompletion.builder().messages(messages).stream(true).model(ChatCompletion.Model.GPT_4_0314.getName()).build();
        }else if(mode.equals("3")){
             chatCompletion = ChatCompletion.builder().messages(messages).stream(true).model(ChatCompletion.Model.GPT_4_32K_0613.getName()).build();
        }
        WebSocketServer.OpenAiStreamClient.streamChatCompletion(chatCompletion, eventSourceListener);
        MessageLocalCache.CACHE.put(uid, JSONUtil.toJsonStr(messages), MessageLocalCache.TIMEOUT);
    }

    /**
     * 发送错误
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[连接ID:{}]", this.uid, error);
        if(!(error instanceof SessionException)) {
            try {
                session.getBasicRemote().sendText(getErrorMsg(StrUtil.format("执行出错"), "chat.ws_error"));
            } catch (Exception e) {
                log.error("发送消息出错", e);
            }
        }
    }

    /**
     * 获取当前连接数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return WebSocketServer.OnlineCount;
    }

    /**
     * 当前连接数加一
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.OnlineCount++;
    }

    /**
     * 当前连接数减一
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.OnlineCount--;
    }

    private String getErrorMsg(String msg, String codeKey) {
        return JSONUtil.toJsonStr(MiscU.Instance.toMap(
                "role", "sqchat", "content", msg,
                "codeKey", codeKey));
    }

}

