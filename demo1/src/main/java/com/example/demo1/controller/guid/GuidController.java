package com.example.demo1.controller.guid;

import org.shoulder.autoconfigure.guid.GuidAutoConfiguration;
import org.shoulder.autoconfigure.guid.InstanceIdProviderAutoConfiguration;
import org.shoulder.core.guid.InstanceIdProvider;
import org.shoulder.core.guid.LongGuidGenerator;
import org.shoulder.core.guid.StringGuidGenerator;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * guid
 *
 * @author lym
 */
@SkipResponseWrap
@RestController
@RequestMapping("guid")
public class GuidController {

    /**
     * id provider
     *
     * @see InstanceIdProviderAutoConfiguration
     */
    @Autowired
    private InstanceIdProvider instanceIdProvider;

    /**
     * global unique id generator [String]
     *
     * @see GuidAutoConfiguration#stringGuidGenerator
     */
    @Autowired
    private StringGuidGenerator stringGuidGenerator;

    /**
     * global unique id generator [long]
     *
     * @see GuidAutoConfiguration#longGuidGenerator
     */
    @Autowired
    private LongGuidGenerator longGuidGenerator;

    /**
     * <a href="http://localhost:8080/guid/instanceId"/>
     */
    @GetMapping("instanceId")
    public Long instanceId() {
        return instanceIdProvider.getCurrentInstanceId();
    }

    /**
     * <a href="http://localhost:8080/guid/long/1"/>
     */
    @GetMapping("long/1")
    public Long long1() {
        return longGuidGenerator.nextId();
    }

    /**
     * <a href="http://localhost:8080/guid/long/2?num=5"/>
     */
    @GetMapping("long/2")
    public long[] long2(@RequestParam(name = "num", defaultValue = "5") Integer num) {
        return longGuidGenerator.nextIds(num);
    }

    /**
     * <a href="http://localhost:8080/guid/string/1"/>
     */
    @GetMapping("string/1")
    public String string1() {
        return stringGuidGenerator.nextId();
    }

    /**
     * <a href="http://localhost:8080/guid/string/2?num=5"/>
     */
    @GetMapping("string/2")
    public String[] string2(@RequestParam(name = "num", defaultValue = "5") Integer num) {
        return stringGuidGenerator.nextIds(num);
    }

}
