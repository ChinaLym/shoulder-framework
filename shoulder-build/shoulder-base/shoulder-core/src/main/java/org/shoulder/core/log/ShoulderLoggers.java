package org.shoulder.core.log;

/**
 * loggerNames
 *
 * @author lym
 */
public interface ShoulderLoggers {

    Logger DEFAULT = LoggerFactory.getLogger("SHOULDER-DEFAULT");

    Logger ERROR = LoggerFactory.getLogger("SHOULDER-ERROR");

    Logger SHOULDER_THREADS = LoggerFactory.getLogger("SHOULDER-THREADS");

    Logger SHOULDER_CONFIG = LoggerFactory.getLogger("SHOULDER-CONFIG");

    Logger SHOULDER_CONVERT = LoggerFactory.getLogger("SHOULDER-CONVERT");

    Logger RPC_CLIENT = LoggerFactory.getLogger("RPC-CLIENT");

    Logger RPC_CLIENT_ERROR = LoggerFactory.getLogger("RPC-CLIENT-ERROR");

    Logger RPC_SERVER = LoggerFactory.getLogger("SHOULDER-SERVER");

    Logger RPC_SERVER_ERROR = LoggerFactory.getLogger("SHOULDER-SERVER-ERROR");

}
