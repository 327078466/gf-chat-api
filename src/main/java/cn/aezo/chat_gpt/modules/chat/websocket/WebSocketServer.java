package cn.aezo.chat_gpt.modules.chat.websocket;

import cn.aezo.chat_gpt.entity.PromptType;
import cn.aezo.chat_gpt.entity.UserMsgLog;
//import cn.aezo.chat_gpt.handler.ImageHandler;
import cn.aezo.chat_gpt.handler.ImageHandler;
import cn.aezo.chat_gpt.handler.ImageHandler3;
import cn.aezo.chat_gpt.modules.chat.ChatService;
import cn.aezo.chat_gpt.service.OssService;
import cn.aezo.chat_gpt.service.PromptTypeService;
import cn.aezo.chat_gpt.util.MiscU;
import cn.aezo.chat_gpt.util.Result;
import cn.aezo.chat_gpt.util.SpringU;
import cn.aezo.chat_gpt.handler.VideoHandler;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.images.Image;
import com.unfbx.chatgpt.entity.images.ImageResponse;
import com.unfbx.chatgpt.entity.images.Item;
import com.unfbx.chatgpt.entity.images.SizeEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.SessionException;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 通过WSS等流式响应时，可选择模型gpt-3.5-turbo<br/>
 * 更多模型参考<br/>
 *
 * @see com.unfbx.chatgpt.entity.chat.ChatCompletion.Model
 */
@Slf4j
@Component
@ServerEndpoint("/tools/chat/user/{uid}/{mode}/{value1}/{value2}")
public class WebSocketServer {

    private static OpenAiStreamClient OpenAiStreamClient;
    private static ChatService ChatService;

    private static PromptTypeService promptTypeService;

    private static OpenAiClient openAiClient;

    private static VideoHandler videoHandler;


    private static OssService ossService;

    @Autowired
    public void setOrderService(OpenAiStreamClient openAiStreamClient, OpenAiClient openAiClient, ChatService chatService, PromptTypeService promptTypeService, VideoHandler videoHandler, OssService ossService) {
        WebSocketServer.OpenAiStreamClient = openAiStreamClient;
        WebSocketServer.ChatService = chatService;
        WebSocketServer.promptTypeService = promptTypeService;
        WebSocketServer.openAiClient = openAiClient;
        WebSocketServer.videoHandler = videoHandler;
        WebSocketServer.ossService = ossService;
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
    private String value1;

    private String value2;


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
     *
     * @param session
     * @param uid
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid, @PathParam("mode") String mode, @PathParam("value1") String value1, @PathParam("value2") String value2) {
        this.session = session;
        this.uid = uid;
        this.mode = mode;
        this.value1 = value1;
        this.value2 = value2;
        // 模式为对话模式 并且 非默认提示词
        if (this.mode.equals("1") && !value2.equals("0")) {
            List<Message> messages = new ArrayList<>();
            PromptType load = promptTypeService.load(Integer.valueOf(value2));
            // 设置当选择后自动加一
            load.setChoiceNum(Integer.valueOf(load.getChoiceNum()) + 1 + "");
            promptTypeService.update(load);
            Message message = new Message();
            message.setContent(load.getContentZh());
            message.setRole(Message.Role.SYSTEM.getName());
            messages.add(message);
            MessageLocalCache.CACHE.put(uid + "-" + mode, JSONUtil.toJsonStr(messages), MessageLocalCache.TIMEOUT);
        } else if (this.mode.equals("5")) { // 证件照
            List<Message> messages = new ArrayList<>();
            Message message = new Message();
            message.setRole(Message.Role.SYSTEM.getName());
            message.setContent("你现在是一名专业的颜色分析师 我会给你一段文本 你告诉我这段文本表达想要的颜色 只需要回复rgb颜色对饮的值 例如 白色255,255,255 你只需要回复我 255,255,255 如果你分析不出来 回复给我两个字 异常");
            messages.add(message);
            MessageLocalCache.CACHE.put(uid + "-" + mode, JSONUtil.toJsonStr(messages), MessageLocalCache.TIMEOUT);
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
     *
     * @param msg
     */
    @SneakyThrows
    @OnMessage
    public void onMessage(String msg) {
        log.info("[连接ID:{}] 收到消息:{}", this.uid, msg);
        if (StrUtil.isBlank(msg)) {
            return;
        }
        if ("PING".equalsIgnoreCase(msg.trim())) {
//            session.getBasicRemote().sendText("PONG");
            return;
        }
        Result result = WebSocketServer.ChatService.checkAndUpdateAsset(this.uid);
        if (Result.isFailure(result)) {
            session.getBasicRemote().sendText(getErrorMsg(result.getMessage(), result.getCodeKey()));
            return;
        }
        Environment environment = SpringU.getBean(Environment.class);
        if ("false".equals(environment.getProperty("sq-mini-tools.openai.enable"))) {
            session.getBasicRemote().sendText("此为模拟返回数据...");
            return;
        }
        String mode = WebSocketServer.WebSocketMap.get(this.uid).mode;
        String value11 = WebSocketServer.WebSocketMap.get(this.uid).value1;
        String value12 = WebSocketServer.WebSocketMap.get(this.uid).value2;

        if(mode.equals("1")  || mode.equals("2")){
            if (SensitiveWordHelper.contains(msg)) {
                session.getBasicRemote().sendText("您的问含有敏感词哦，请换个方式～");
                return;
            }
        }
        //接受参数
        OpenAIWebSocketEventSourceListener eventSourceListener = new OpenAIWebSocketEventSourceListener(this.session);
        String messageContext = (String) MessageLocalCache.CACHE.get(uid + "-" + mode);
        List<Message> messages = new ArrayList<>();
        // 到这一步时 向用户日志中插入数据
        UserMsgLog userMsgLog = new UserMsgLog();
        userMsgLog.setUserId(uid);
        userMsgLog.setMsg(msg);
        userMsgLog.setCreateTime(Timestamp.from(Instant.now()));
        userMsgLog.setMode(mode);
        userMsgLog.setModeValue(value11 + "+" + value12);
        ChatService.saveUserMsgLog(userMsgLog);
        if (StrUtil.isNotBlank(messageContext)) {
            messages = JSONUtil.toList(messageContext, Message.class);
            if (messages.size() >= 10) {
                Message message = messages.get(0); // 第一句话保留 可能是角色定义
                messages = messages.subList(1, 10);
                messages.add(0, message); // 第一位设置
            }
        }
        if (mode.equals("1")) { // 对话模式
            Message currentMessage = Message.builder().content(msg).role(Message.Role.USER).build();
            messages.add(currentMessage);
            handlerChat(messages, value11, eventSourceListener);
        } else if (mode.equals("2")) { // 作图模式
            handlerDraw(value11, value12, msg);
        } else if (mode.equals("3")) { // 处理视频
            // 解析请求信息
            handlerVideo(msg);
        } else if (mode.equals("4")) { // 处理文字录音


        } else if (mode.equals("5")) { // 解析证件照图片
            String[] split = msg.split("----");
            Message currentMessage = Message.builder().content(split[0]).role(Message.Role.USER).build();
            messages.add(currentMessage);
            String content = openAiClient.chatCompletion(messages).getChoices().get(0).getMessage().getContent();
            if (content.equals("异常")) {
                HashMap<String, Object> hashMap1 = new HashMap<>();
                hashMap1.put("content", "解析失败，请重新描述");
                hashMap1.put("role", "assistant");
                JSONObject entries = new JSONObject();
                entries.putAll(hashMap1);
                String dataJson1 = entries.toString();
                session.getBasicRemote().sendText(dataJson1);
            } else {
                MultipartFile multipartFile = ImageHandler3.handleBufferImageBackgroundRGB(split[1], content);
                String s = ossService.uploadFileAvatar(multipartFile);
                HashMap<String, Object> hashMap1 = new HashMap<>();
                List<String> list = new ArrayList<>();
                hashMap1.put("content", "");
                hashMap1.put("role", "assistant");
                JSONObject entries = new JSONObject();
                entries.putAll(hashMap1);
                String dataJson1 = entries.toString();
                list.add(dataJson1);
                HashMap<String, Object> hashMap2 = new HashMap<>();
                hashMap2.put("content", s);
                entries.clear();
                entries.putAll(hashMap2);
                String dataJson2 = entries.toString();
                list.add(dataJson2);
                String dataJson3 = "[DONE]";
                list.add(dataJson3);
                list.forEach(item -> {
                    try {
                        session.getBasicRemote().sendText(item);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                });
            }
        } else if (mode.equals("6")) { // 解析证件照图片

        }
        MessageLocalCache.CACHE.put(uid + "-" + mode, JSONUtil.toJsonStr(messages), MessageLocalCache.TIMEOUT);
    }

    /**
     * 处理对话
     *
     * @param messages
     * @param value11
     * @param eventSourceListener
     */
    public void handlerChat(List<Message> messages, String value11, OpenAIWebSocketEventSourceListener eventSourceListener) {
        ChatCompletion chatCompletion = ChatCompletion.builder().messages(messages).stream(true).model(ChatCompletion.Model.GPT_3_5_TURBO_0613.getName()).build();
        // 设置请求模型
        if (value11.equals("2")) { // GPT4.0
            chatCompletion = ChatCompletion.builder().messages(messages).stream(true).model(ChatCompletion.Model.GPT_4_0314.getName()).build();
        }
        WebSocketServer.OpenAiStreamClient.streamChatCompletion(chatCompletion, eventSourceListener);
    }

    /**
     * 处理绘画
     *
     * @param value11
     * @param value12
     * @param msg
     */
    public void handlerDraw(String value11, String value12, String msg) {
        Image image = new Image();
        image.setN(Integer.valueOf(Integer.valueOf(value11)));
        if (value12.equals("256*256")) {
            image.setSize(SizeEnum.size_256);
        } else if (value12.equals("512*512")) {
            image.setSize(SizeEnum.size_512);
        } else {
            image.setSize(SizeEnum.size_1024);
        }
        image.setPrompt(msg);
        ImageResponse imageResponse = openAiClient.genImages(image);
        List<Item> data = imageResponse.getData();
        HashMap<String, Object> hashMap1 = new HashMap<>();
        List<String> list = new ArrayList<>();
        hashMap1.put("content", "");
        hashMap1.put("role", "assistant");
        JSONObject entries = new JSONObject();
        entries.putAll(hashMap1);
        String dataJson1 = entries.toString();
        list.add(dataJson1);
        data.forEach(item -> {
            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("content", data.get(0).getUrl());
            entries.clear();
            entries.putAll(hashMap2);
            String dataJson2 = entries.toString();
            list.add(dataJson2);
        });
        String dataJson3 = "[DONE]";
        list.add(dataJson3);
        list.forEach(item -> {
            try {
                session.getBasicRemote().sendText(item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void handlerVideo(String msg) throws IOException {
        Map<String, Object> data = new HashMap<>();
        if (msg.contains("pipix")) {
            videoHandler.pipixia(msg);
        } else if (msg.contains("douyin")) {
            data = videoHandler.douyin(msg);
        } else if (msg.contains("huoshan")) {
            data = videoHandler.huoshan(msg);
        } else if (msg.contains("h5.weishi")) {
            data = videoHandler.weishi(msg);
        } else if (msg.contains("isee.weishi")) {
            data = videoHandler.weishi(msg);
        } else if (msg.contains("weibo.com")) {
            data = videoHandler.weibo(msg);
        } else if (msg.contains("oasis.weibo")) {
            data = videoHandler.weibo(msg);
        } else if (msg.contains("zuiyou") || msg.contains("xiaochuankeji")) {
            data = videoHandler.zuiyou(msg);
        } else if (msg.contains("bbq.bilibili")) {
            data = videoHandler.bbq(msg);
        } else if (msg.contains("kuaishou")) {
            data = videoHandler.kuaishou(msg);
        } else if (msg.contains("quanmin")) {
            data = videoHandler.quanmin(msg);
        } else if (msg.contains("moviebase")) {
            data = videoHandler.basai(msg);
        } else if (msg.contains("hanyuhl")) {
            data = videoHandler.before(msg);
        } else if (msg.contains("eyepetizer")) {
            data = videoHandler.kaiyan(msg);
        } else if (msg.contains("immomo")) {
            data = videoHandler.momo(msg);
        } else if (msg.contains("vuevideo")) {
            data = videoHandler.vuevlog(msg);
        } else if (msg.contains("xiaokaxiu")) {
            data = videoHandler.xiaokaxiu(msg);
        } else if (msg.contains("ippzone") || msg.contains("pipigx")) {
            data = videoHandler.pipigaoxiao(msg);
        } else if (msg.contains("qq.com")) {
            data = videoHandler.quanminkge(msg);
        } else if (msg.contains("ixigua.com")) {
            data = videoHandler.xigua(msg);
        } else if (msg.contains("doupai")) {
            data = videoHandler.doupai(msg);
        } else if (msg.contains("6.cn")) {
            data = videoHandler.sixroom(msg);
        } else if (msg.contains("huya.com/play/")) {
            data = videoHandler.huya(msg);
        } else if (msg.contains("pearvideo.com")) {
            data = videoHandler.pear(msg);
        } else if (msg.contains("xinpianchang.com")) {
            data = videoHandler.xinpianchang(msg);
        } else if (msg.contains("acfun.cn")) {
            data = videoHandler.acfan(msg);
        } else if (msg.contains("meipai.com")) {
            data = videoHandler.meipai(msg);
        } else {
            data.put("code", 201);
            data.put("msg", "解析失败");
        }
        sendVideoMessage(data);
    }

    void sendVideoMessage(Map<String, Object> resultData) throws IOException {
        if ((resultData.get("code") + "").equals("200")) {
            HashMap<String, Object> hashMap1 = new HashMap<>();
            List<String> list = new ArrayList<>();
            HashMap data = (HashMap) resultData.get("data");
            hashMap1.put("content", data.get("title"));
            hashMap1.put("role", "assistant");
            JSONObject entries = new JSONObject();
            entries.putAll(hashMap1);
            String dataJson1 = entries.toString();
            list.add(dataJson1);
            hashMap1.clear();
            hashMap1.put("content", data.get("url"));
            entries.clear();
            entries.putAll(hashMap1);
            String dataJson2 = entries.toString();
            list.add(dataJson2);
            String dataJson3 = "[DONE]";
            list.add(dataJson3);
            list.forEach(item -> {
                try {
                    session.getBasicRemote().sendText(item);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            session.getBasicRemote().sendText(getErrorMsg(StrUtil.format("执行出错"), "chat.chat.asset_short"));
        }
    }

    /**
     * 发送错误
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[连接ID:{}]", this.uid, error);
        if (!(error instanceof SessionException)) {
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

