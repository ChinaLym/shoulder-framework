package org.shoulder.batch.service;

/*

public class BatchValidator implements Runnable {




    public BatchValidator() {
        String progressCode = generateProgressCode();
        DataReadRecord<Map<String, String>> record = new DataReadRecord<>(progressCode, csvData.getMetaRows().size());
        DataReadRecordCollection.put(record.getProgressCode(), record);
        record.setRunStatus(ParseRunStatus.READY);
    }

    @Override
    public void run() {
        record.setRunStatus(ParseRunStatus.RUNNING);//运行状态
        try {
            List<String[]> allLines = csvData.getMetaRows();

            // 先把所有的行读取到，再进行下一步的校验，方便全局校验
            record.setHeaders(csvData.getHeaders());
            record.setAllRows(csvData.getRows());

            // 正常结束，但是有非法行标志位
            boolean doneWithInValid = false;

            // 执行数据校验
            // 因为header包含label行，所以rowNo是header+1
            for (int i = 0, rowNo = csvData.getHeaders().size() + 1; i < record.getAllRows().size(); i++, rowNo++) {
                try {
                    RowParseResult<Map<String, String>> rowParseResult = new RowParseResult<>(rowNo, allLines.get(i), csvData.getRows().get(i));
                    // 是否需要再次添加
                    for (DataValidator<Map<String, String>> dataValidator : dataDataValidators) {
                        if (dataValidator.validate(rowParseResult, record)) {
//                                record.addSuccessResult(rowParseResult);
                        } else {
                            // 该row数据验证非法
                            doneWithInValid = true;
//                                rowParseResult.getResultType()==
//                                record.addFailResult(rowParseResult);
                        }
                    }
                    record.addResult(rowParseResult);
                } catch (Exception e) {
                    // 异常终止
                    record.addFailResult(new RowParseResult<>(rowNo, allLines.get(i), ValidFailType.DESERIALIZE_ERROR, e.getMessage()));
                    record.setRunStatus(ParseRunStatus.DONE_WITH_EXCEPTION);
                    record.finishProgressBar();
                    record.setMsg(e.getMessage());
                    return;
                }
            }
            // 数据有非法行时，返回DONE_WITH_VALID
            if (doneWithInValid) {
                record.setRunStatus(ParseRunStatus.DONE_WITH_VALID);
            } else {
                record.setRunStatus(ParseRunStatus.DONE);
            }
        } catch (Exception e) {
            record.setRunStatus(ParseRunStatus.DONE_WITH_EXCEPTION);
            record.setException(e);
        }
    }


    protected String generateProgressCode() {
        return UUID.randomUUID().toString();
    }

}
*/
