# shoulder-http

- RestTemplate 自动记录请求响应日志，默认提供两种实现
- RestResult 自动拆箱？
    - MappingJackson2HttpMessageConverter 可在 read 时，拆除 `RestResult`
    - 若 code 不为 0 则抛出 接口调用失败异常 
    - ...

```java
代理先发送
JSONObject jSONObject=restTemplate.postForObject（url，list，JSONObject.class）;
然后返回时将响应转为需要的值
JSONObject  data=jSONObject.getJSONObject("data");
map=JSON.parseObject(data.toString(), new TypeReference<Map<String,Object>>(){});


restTemplate
```
