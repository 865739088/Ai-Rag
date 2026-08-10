package net.topikachu.rag.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.topikachu.rag.evaluation.entity.ChatEvaluationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话评估数据访问接口。
 *
 * @author AI.Coding
 */
@Mapper
public interface ChatEvaluationMapper extends BaseMapper<ChatEvaluationEntity> {
}
