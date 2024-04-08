package org.shoulder.batch.log;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

public interface ShoulderBatchLoggers {

    Logger DEFAULT = LoggerFactory.getLogger("SHOULDER-BATCH");

    Logger ERROR = LoggerFactory.getLogger("SHOULDER-BATCH-ERROR");
}
