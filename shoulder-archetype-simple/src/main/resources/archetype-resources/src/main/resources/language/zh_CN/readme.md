# Shoulder 国际化功能-小提示（可删除）

# 模块化

`Shoulder` 对 `多模块工程` 友好，允许多语言文件分模块维护

# 灵活命名，更少配置

`Shoulder` 不限制多语言文件命名，不需要像 `Spring` 原生用法：还要为每个翻译文件配置具体的文件名，shoulder
会自动解析所有 `language/<languageId>/*.properties` 文件

# 智能加载

`Shoulder` 会自动识别多语言翻译文件，不会加载与多语言不相干的文件（如本文件）

您可以在多语言资源文件下添加自己的说明文件，就像本文件这样，shoulder 会自动忽略
