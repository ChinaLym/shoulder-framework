package org.shoulder.batch.spec;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@XStreamAlias("shop")
public class ShopBO {

    String id;

    String name;

    /**
     * 属性名不同需要在 @Mapping 中描述
     */
    String addr;

    @XStreamAlias("boss")
    Owner boss;

    /**
     * 超市拥有者，演示复杂类型转换
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Owner {

        String name;

        int age;
    }

}
