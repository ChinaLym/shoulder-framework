package org.shoulder.crypto.log;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;

public interface ShoulderCryptoLoggers {

    Logger DEFAULT = LoggerFactory.getLogger("SHOULDER-CRYPTO");

    Logger ERROR = LoggerFactory.getLogger("SHOULDER-CRYPTO-ERROR");
}
