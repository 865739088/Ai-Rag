package net.topikachu.rag.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话评估持久化实体。
 *
 * @author AI.Coding
 */
@Data
@TableName("chat_evaluation")
public class ChatEvaluationEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String conversationId;
    private String userId;
    private String question;
    private String answer;
    private String model;
    private String mode;
    private String rating;
    private String failureMode;
    private String traceId;
    private String contextSnippets;
    private String usedSources;
    private String reference;
    private LocalDateTime createDate;
}
