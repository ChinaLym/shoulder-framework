package com.example.demo1.bo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.shoulder.core.model.Operable;

/**
 * 用户
 *
 * @author lym
 */
@Data
@Accessors(chain = true)
public class UserInfo implements Operable {

    String id;

    String name;

    @Override
    public String getObjectId() {
        return id;
    }

    @Override
    public String getObjectName() {
        return name;
    }

    @Override
    public String getObjectType() {
        return "user";
    }

}
