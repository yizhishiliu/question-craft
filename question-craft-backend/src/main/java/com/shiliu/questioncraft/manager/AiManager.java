package com.shiliu.questioncraft.manager;

import com.shiliu.questioncraft.common.ErrorCode;
import com.shiliu.questioncraft.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 * 通用AI 调用能力
 */
@Component
public class AiManager {

    @Resource
    private ClientV4 clientV4;

    // 稳定随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 通用请求
     *
     * @param messages    请求内容
     * @param stream      是否流式返回
     * @param temperature 随机性
     * @return 返回结果
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 通用请求（简化消息传递）
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @param stream        是否流式返回
     * @param temperature   随机性
     * @return 返回结果
     */
    public String doRequest(String systemMessage, String userMessage, Boolean stream, Float temperature) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        // 系统消息
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);
        // 用户消息
        ChatMessage chatChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        chatMessageList.add(chatChatMessage);

        return doRequest(chatMessageList, stream, temperature);
    }

    /**
     * 同步请求
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @param temperature   随机性
     * @return 返回结果
     */
    public String doSyncRequest(String systemMessage, String userMessage, Float temperature) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, temperature);
    }

    /**
     * 同步请求，答案稳定
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @return 返回结果
     */
    public String doSyncStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, STABLE_TEMPERATURE);
    }

    /**
     * 同步请求，答案不稳定
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @return 返回结果
     */
    public String doSyncUnStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, UNSTABLE_TEMPERATURE);
    }

    /**
     * 通用流式请求
     *
     * @param messages    请求内容
     * @param temperature 随机性
     * @return 返回结果
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getFlowable();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 通用流式请求（简化消息传递）
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @param temperature   随机性
     * @return 返回结果
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        // 系统消息
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);
        // 用户消息
        ChatMessage chatChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        chatMessageList.add(chatChatMessage);

        return doStreamRequest(chatMessageList, temperature);
    }
}
