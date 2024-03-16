package org.shoulder.data.dal.sequence.old;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 根据数据库里的sequence记录来初始化成sequence的factory
 * 利用sequence name作为key在factory里获取对应的multipleSequence对象
 * 然后在 multipleSequence对象上通过接口nextValue()获取sequence值
 *
 * @author lym
 */
public class MultipleSequenceFactory {

    private static final Logger logger = LoggerFactory.getLogger("SEQUENCE");

    private final Lock lock = new ReentrantLock();
    /**
     * 存放sequence，key为sequence名称，value为对应的MultipleSequence对象
     */
    private Map<String, MultipleSequence> multipleSequenceMap = new ConcurrentHashMap<>();
    /**
     * 获取sequence的DAO对象，在所有的factory里共用同一个DAO
     */
    @Getter
    @Setter
    private MultipleSequenceDao multipleSequenceDao;

    /**
     * 初始化multipleSequence的工厂
     * 从数据源里获取sequence的记录，对每一条记录进行处理，生成对应的multipleSequence对象，加载到内存中
     *
     * @throws Exception e
     */
    public void init() {
        if (multipleSequenceDao == null) {
            throw new IllegalArgumentException("The sequenceDao is null!");
        }
        initMultipleSequenceMap();
    }

    /**
     * 初始化 multipleSequenceMap,将db里的记录初始化到multipleSequenceMap里
     */
    private void initMultipleSequenceMap() {
        Map<String, Map<String, Object>> sequenceRecords = null;
        //获取全部的sequence记录
        try {
            sequenceRecords = multipleSequenceDao.getAllSequenceNameRecord();
            if (sequenceRecords == null) {
                throw new IllegalArgumentException("ERROR ## The sequenceRecord is null!");
            }
            for (Map.Entry<String, Map<String, Object>> sequenceRecord : sequenceRecords.entrySet()) {
                String seqName = sequenceRecord.getKey().trim();
                Map<String, Object> sequeceRecordvalue = sequenceRecord.getValue();
                long min = (Long) sequeceRecordvalue.get(multipleSequenceDao
                    .getMinValueColumnName());
                long max = (Long) sequeceRecordvalue.get(multipleSequenceDao
                    .getMaxValueColumnName());
                int step = (Integer) sequeceRecordvalue.get(multipleSequenceDao
                    .getInnerStepColumnName());
                MultipleSequence multipleSequence = new MultipleSequence(multipleSequenceDao,
                    seqName, min, max, step);
                try {
                    multipleSequence.init();
                    multipleSequenceMap.put(seqName, multipleSequence);
                } catch (Exception e) {
                    logger.error("ERROR ## init the sequenceName = " + seqName + " has an error:",
                        e);
                }
            }
        } catch (Exception e) {
            logger.error("ERROR ## init the multiple-Sequence-Map failed!", e);
        }
    }

    /**
     * 根据sequence name初始化单条记录到multipleSequenceMap
     *
     * @param sequenceName sequence name
     * @throws Exception
     */
    private void initOneMultipleSequenceRecord(String sequenceName) throws Exception {
        Map<String, Map<String, Object>> sequenceRecords = null;
        //获取全部的sequence记录
        try {
            sequenceRecords = multipleSequenceDao.getSequenceRecordByName(sequenceName);
            if (sequenceRecords == null) {
                throw new IllegalArgumentException("The sequenceRecord is null,sequenceName="
                    + sequenceName);
            }
            for (Map.Entry<String, Map<String, Object>> sequenceRecord : sequenceRecords.entrySet()) {
                String seqName = sequenceRecord.getKey().trim();
                Map<String, Object> sequeceRecordvalue = sequenceRecord.getValue();
                long min = (Long) sequeceRecordvalue.get(multipleSequenceDao
                    .getMinValueColumnName());
                long max = (Long) sequeceRecordvalue.get(multipleSequenceDao
                    .getMaxValueColumnName());
                int step = (Integer) sequeceRecordvalue.get(multipleSequenceDao
                    .getInnerStepColumnName());
                MultipleSequence multipleSequence = new MultipleSequence(multipleSequenceDao,
                    seqName, min, max, step);
                multipleSequence.init();
                multipleSequenceMap.put(seqName, multipleSequence);
            }

        } catch (Exception e) {
            logger.error("init the multipleSequenceMap failed!", e);
            throw e;
        }
    }

    /**
     * 外部调用接口，根据sequence name 获取sequence value
     * 如果该sequence在multipleSequenceMap里不存在，则去db里查一下是否存在，
     * 如果存在就生成对应的multipleSequence对象并加载到内存，否则报错；
     *
     * @param sequenceName
     * @return
     * @throws Exception
     */
    public long getNextValue(String sequenceName) throws Exception {
        if (StringUtils.isBlank(sequenceName)) {
            throw new IllegalArgumentException("The sequence name can not be null!");
        }
        MultipleSequence multipleSequence = multipleSequenceMap.get(sequenceName);
        if (multipleSequence != null) {
            return multipleSequence.nextValue();
        } else {
            try {
                lock.lock();
                if (multipleSequenceMap.get(sequenceName) == null) {
                    initOneMultipleSequenceRecord(sequenceName);
                }
                return multipleSequenceMap.get(sequenceName).nextValue();
            } finally {
                lock.unlock();
            }
        }
    }
}
