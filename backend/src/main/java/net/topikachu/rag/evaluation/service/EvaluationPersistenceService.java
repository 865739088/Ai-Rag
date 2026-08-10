package net.topikachu.rag.evaluation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.topikachu.rag.evaluation.ContextNode;
import net.topikachu.rag.evaluation.entity.ChatEvaluationEntity;
import net.topikachu.rag.evaluation.mapper.ChatEvaluationMapper;
import net.topikachu.rag.service.chat.UsedSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 保存聊天结果及其检索证据，供人工反馈和自动评估使用。
 *
 * @author AI.Coding
 */
@Service
public class EvaluationPersistenceService {

    private final ChatEvaluationMapper evaluationMapper;
    private final ObjectMapper objectMapper;

    public EvaluationPersistenceService(ChatEvaluationMapper evaluationMapper, ObjectMapper objectMapper) {
        this.evaluationMapper = evaluationMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步保存一次已完成的对话及评估上下文。
     */
    public Mono<Void> saveConversation(String msgId,
                                       String conversationId,
                                       String userId,
                                       String question,
                                       String answer,
                                       String modelId,
                                       String mode,
                                       List<ContextNode> contextNodes,
                                       List<UsedSource> usedSources,
                                       String traceId) {
        return Mono.fromRunnable(() -> {
                    ChatEvaluationEntity entity = new ChatEvaluationEntity();
                    entity.setId(msgId);
                    entity.setConversationId(conversationId);
                    entity.setUserId(userId);
                    entity.setQuestion(question);
                    entity.setAnswer(answer);
                    entity.setModel(modelId);
                    entity.setMode(mode);
                    entity.setTraceId(traceId);
                    // 数据库字段保存 JSON，确保自动评估可以稳定还原检索片段。
                    entity.setContextSnippets(writeJson(contextNodes));
                    entity.setUsedSources(writeJson(usedSources));
                    evaluationMapper.insert(entity);
                })
                // MyBatis/JDBC 是阻塞调用，必须移出 Reactor 事件循环。
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * 将评估上下文序列化为数据库可存储的 JSON。
     */
    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize evaluation context", e);
        }
    }
}
