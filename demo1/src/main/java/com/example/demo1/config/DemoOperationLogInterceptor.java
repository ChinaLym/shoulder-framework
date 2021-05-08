package com.example.demo1.config;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.dto.sample.MultiOperableDecorator;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志拦截器
 * <p>
 * 如果开启了异步记录，这里的所有方法都是异步的~ 放心查数据库吧
 *
 * @author lym
 */
@Slf4j
@Component
public class DemoOperationLogInterceptor implements OperationLoggerInterceptor {

    //@Autowired
    //JdbcTemplate jdbcTemplate;
    // *************************** 批量场景的拦截 ******************************

    /**
     * 在组装批量日志前，可以根据自己的业务调整组装方式
     */
    @Override
    public List<? extends Operable> beforeAssembleBatchLogs(OperationLogDTO template, List<? extends Operable> operableList) {

        // 批量导入业务中，被操作对象可能有多个，假设操作了 100 条，记成一条可能某些字段超出最大长度限制，记成 100 条又不利于整体查看，这里可以对其进行自定义分隔
        if ("batchImportXxx".equals(template.getOperation())) {
            // 5条操作合成一条操作日志，
            List<? extends List<? extends Operable>> partOperableList = CollectionUtil.split(operableList, 5);
            List<Operable> result = new ArrayList<>(partOperableList.size());
            partOperableList.forEach(operables -> result.add(new MultiOperableDecorator(operables)));
            return result;
        }
        return operableList;
    }


    // *************************** 可以拦截每条日志记录 ***************************

    @Override
    public void beforeLog(OperationLogDTO opLog) {
        log.info("记录操作日志前回调钩子： 可在这里进行校验格式 / 继续填充一些值。可在 " + getClass().getSimpleName() + "#beforeLog 关闭该输出~ ");

        // --------------- 这里可以做的事情举例 --------------------
        if (StringUtils.isEmpty(opLog.getObjectName())) {
            // 执行某个业务可能不会拿到被操作对象的全部信息，但希望在展示操作日志时，展示更全的信息
            // 以删除用户业务为例：删除用户接口的参数可能只有 userId 而没有 userName 。但希望在展示操作日志时，显示被删除的用户昵称、真实姓名，因此需要再查一次数据库补充
            // 由于再从数据库查一次用户信息仅是为了操作日志展示方便，与 '删除用户' 这个主要业务没关系，不应该阻塞原业务，因此放在这里异步去做，不影响原业务性能和结果
            // 比如这里我根据实体类类型，动态去查一次，

            //fillMoreInfoFromDB(opLog);
        }
    }

    @Override
    public void afterLog(OperationLogDTO opLog) {
        log.info("记录操作日志后回调钩子：可在 " + getClass().getSimpleName() + "#afterLog 关闭该输出~ ");

        // 这里可以做的事情举例：
        // 
        // 1. 清理一些由于 beforeValidate 引入的变量或者垃圾

        // 2. 统计各个业务操作的频次，看看哪些业务比较热门 / 受欢迎 / 重要

        // 3. 审计调用参数

        // 4. ...

    }

    private void fillMoreInfoFromDB(OperationLogDTO opLog) {
        String tableName = null;
        String objectId = null;
        String objectName = null;
        switch (opLog.getObjectType()) {
            case "USER":
                tableName = "user";
                objectId = "user_id";
                objectName = "user_name";
                break;

            case "USER_GROUP":
                tableName = "user_group";
                objectId = "user_group_id";
                objectName = "user_group_name";

            default:
                break;
        }
        if (tableName == null) {
            return;
        }
        // 拼接sql
        String sql = "select " + objectId + " as objectId," + objectName + "as objectName" +
                " from " + tableName +
                " where " + objectId + "=" + opLog.getObjectId();
        // 从数据库里动态查出来，填充一下
        // OperableObject dbInfo = jdbcTemplate.queryForObject(sql, OperableObject.class);
        // opLog.setObjectName(dbInfo.getObjectName());

    }

}
