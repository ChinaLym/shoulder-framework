/**
 * 标签系统
 * 本系统标签、标签映射表设计时都选择了相对通用的方案，适用于大部分场景，小部分特殊场景可能需要考虑更换模型
 * 标签模型：存在继承关系，但适用于有固定深度的标签体系
 * 标签映射模型：标准关系型数据库设计，扩展性好，锁粒度小
 * <p>
 * 想要更好的性能，在维护标签关系时：
 * 1. 标签总量少，扩展需求低，标签基本为单选：  直接放原表作为基础字段，不需要标签表、标签关系表等，搜索时前端写死【场景很少，业务变化少才行】
 * 2. 标签总量少，扩展需求低，有一定多选诉求：  多个标签,拼接，模糊搜索 【弊端相对明显，业务变化少 && 数据增长慢才行】
 * 3. 标签总量变化频繁，需要搜索的标签下关联的内容少：    标签表+标签搜索表（倒排索引）+原表扩展字段存tag信息【在上述约束下性能高、成本低，扩展性好】
 * 4. 标签表、标签映射表，相对通用，扩展性好，整体适应性广，本项目采取
 * 5. 根据特定存储，定制数据结构，暂不讨论，模型与数据库绑定追求极致性能，非特定场景不建议
 */
package org.shoulder.web.template.tag;