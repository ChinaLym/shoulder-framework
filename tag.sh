# 执行前先修改版本号、发布到 oss
# <shoulder.version>0.3</shoulder.version><!-- shoulder-version -->
git tag -a v0.3 -m "对应 `0.2` 版本，第2个预览发布版，内容见 CHANGELOG.md"
git push origin --tags
