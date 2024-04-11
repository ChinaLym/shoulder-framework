package org.shoulder.web.template.oplog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.oplog.model.OperationLogEntity;

/**
 * oplog 存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {

}
