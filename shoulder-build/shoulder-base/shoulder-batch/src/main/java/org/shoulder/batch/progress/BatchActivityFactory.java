package org.shoulder.batch.progress;

import org.shoulder.batch.model.BatchActivityBlock;
import org.shoulder.batch.dto.BatchActivityDTO;
import org.shoulder.batch.model.BatchActivityRoot;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author lym
 */
public class BatchActivityFactory {

    public static BatchActivityRoot parseActivity(Class<? extends BatchActivityEnum<?>> activityEnumClass) {
        String enumName = activityEnumClass.getSimpleName();
        BatchActivityRoot activityRoot = new BatchActivityRoot();
        activityRoot.setId(enumName);
        activityRoot.setOriginalClass(activityEnumClass);
        TreeMap<Integer, TreeMap<Integer, List<BatchActivityDTO>>> tempStructure = new TreeMap<>();
        BatchActivityEnum<?>[] activities = activityEnumClass.getEnumConstants();
        for (BatchActivityEnum<?> activity : activities) {
            int blockNum = activity.displayBlockNum();
            int columnNum = activity.getDisplayColumnNum();

            // first add block
            TreeMap<Integer, List<BatchActivityDTO>> subTree = tempStructure.computeIfAbsent(blockNum, k -> new TreeMap<>());
            List<BatchActivityDTO> activityDTOList = subTree.computeIfAbsent(columnNum, k -> new LinkedList<>());
            BatchActivityDTO activityDTO = new BatchActivityDTO();
            activityDTO.setId(activity.getKey());
            activityDTO.setIcon(activity.getDisplayEmoji());
            activityDTO.setTitle(activity.getDisplayName());
            activityDTO.setEndStep(activity.isEndStep());
            activityDTOList.add(activityDTO);
            if (activity.isEndStep()) {
                activityRoot.setEndStepId(activity.getKey());
            }
        }
        // 压缩结构
        List<BatchActivityBlock> progressBlocks = tempStructure.values().stream()
                .map(map -> map.values().stream().toList())
                .map(l -> l.size() == 1 ?
                        new BatchActivityBlock(BatchActivityBlock.SERIAL, l.get(0))
                        : new BatchActivityBlock(BatchActivityBlock.PARALLEL, l)
                ).toList();
        activityRoot.setActivityBlocks(progressBlocks);
        return activityRoot;
    }
}