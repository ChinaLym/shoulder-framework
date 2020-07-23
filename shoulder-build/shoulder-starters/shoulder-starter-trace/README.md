# shoulder-starter-trace（调用链）

分布式链路追踪

[谷歌Dapper论文-中文](http://bigbully.github.io/Dapper-translation/)


开源技术方案：

- [Twitter-Zipkin](https://zipkin.io/)
- [Skywalking](http://skywalking.apache.org/)
- [美团点评-CAT](https://zipkin.io/)
- [Pinpoint](https://github.com/naver/pinpoint)


注意事项：

涉及 websocket 时会产生一些问题

- [github](https://github.com/spring-cloud/spring-cloud-sleuth/issues/276) 
- [github](https://github.com/spring-cloud/spring-cloud-sleuth/issues/276) 
- [spring WebSocket 指南](https://spring.io/guides/gs/messaging-stomp-websocket/)
    - 最好指定下 `webjars-locator-core` 包的版本，不要使用`spring-boot-parent`管理的该版本，否则会出现 `http://localhost:8080/webjars/jquery/jquery.min.js` `404`问题。 
