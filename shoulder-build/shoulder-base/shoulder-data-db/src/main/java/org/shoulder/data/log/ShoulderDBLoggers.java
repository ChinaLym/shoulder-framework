package org.shoulder.data.log;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

public interface ShoulderDBLoggers {

    Logger DEFAULT = LoggerFactory.getLogger("SHOULDER-DB");

    Logger SEQUENCE = LoggerFactory.getLogger("SHOULDER-SEQUENCE");

    Logger SEQUENCE_MONITOR = LoggerFactory.getLogger("SHOULDER-SEQUENCE-MONITOR");

    Logger ERROR = LoggerFactory.getLogger("SHOULDER-DB-ERROR");
}
