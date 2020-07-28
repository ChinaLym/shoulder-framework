# shoulder-validate

参数校验

提供了几个常用的校验注解（基于 `JSR 303` ）。实际中用法和遇到的问题参见 `Spring Boot 参数校验` 或 `JSR 303 教程`


| **验证注解** | **验证的数据类型** | **说明** |
|--|--|--|
| = = = = = | **shoulder 专属** | = = = = = |

| @MimeTypeValidate | MultipartFile | 校验上传的文件是否是允许类型 |
| @NoForbiddenChar | CharSequence | 校验字符中是否出现非法字符（@Pattern）的反义 |
| @Enum | CharSequence | 枚举值 |
| = = = = = | **JSR 303** | = = = = = |
| @AssertFalse | Boolean,boolean | 值是false |
| @AssertTrue | Boolean,boolean | 值是true |
| @NotNull | 任意类型 | 值不是null |
| @Null | 任意类型 | 值是null |
| @Min(value=值) | 数字 | 值大于等于@Min指定的value值 |
| @Max（value=值） | 数字 | 值小于等于@Max指定的value值 |
| @DecimalMin(value=值) | 数字 | 值大于等于@ DecimalMin指定的value值 |
| @DecimalMax(value=值) | 数字 | 值小于等于@ DecimalMax指定的value值 |
| @Digits(integer=整数位数, fraction=小数位数) | 数字 | 值的整数位数和小数位数上限 |
| @Size(min=下限, max=上限) | 字符串、Collection、Map、数组等 | 值的在min和max（包含）指定区间之内，如字符长度、集合大小 |
| @Past | Date、Calendar、Joda Time类库的日期类型 | 值（日期类型）比当前时间早 |
| @Future | 与@Past要求一样 | 值（日期类型）比当前时间晚 |
| @NotBlank | CharSequence | 值不为空（不为null、去除首位空格后长度为0），不同于@NotEmpty，@NotBlank只应用于字符串且在比较时会去除字符串的首位空格 |
| @Length(min=下限, max=上限) | CharSequence | 值长度在min和max区间内 |
| @NotEmpty | CharSequence、Collection、Map、数组 | 值不为null且不为空（字符串长度不为0、集合大小不为0） |
| @Range(min=最小值, max=最大值) | 数字 | 值在最小值和最大值之间 |
| @Email(regexp=正则表达式, flag=标志的模式) | CharSequence | 值是Email，也可以通过regexp和flag指定自定义的email格式 |
| @Pattern(regexp=正则表达式, flag=标志的模式) | CharSequence | 值与指定的正则表达式匹配 |
| @URL(protocol=,host,port)| CharSequence | 检查是否是一个有效的URL |
| @Valid | 对象 | 用于类内部对象类型属性 |
| - - - - - | **另外推荐将常用@Pattern以更显而易见的名称自己实现下列注解（可以使用 core 包里的 RegexUtils）** | - - - - - |
| @LicensePlate | CharSequence | 合法的车牌号（中国大陆） |
| @IP | CharSequence | 合法的IP地址 |
| @Port | CharSequence | 合法的端口号 |
| @QQ | CharSequence | 合法的QQ号 |
| @IdCard | CharSequence | 合法的身份证（中国大陆） |
| @PostCde | CharSequence | 合法的邮编（中国邮政） |
| @Phone | CharSequence | 手机号码（中国大陆） |


常用正则表达式参见开源的 `any-rule`。
