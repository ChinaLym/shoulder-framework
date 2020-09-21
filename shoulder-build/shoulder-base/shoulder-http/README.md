# shoulder-http

- BaseResponse 自动拆箱？
    - MappingJackson2HttpMessageConverter 可在 read 时，拆除 `BaseResponse`
    - 若 code 不为 0 则抛出 接口调用失败异常 
    - ...
