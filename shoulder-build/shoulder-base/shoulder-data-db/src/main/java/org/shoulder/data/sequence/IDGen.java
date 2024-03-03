package org.shoulder.data.sequence;

public interface IDGen {
    Result get(String key);

    boolean init();
}
