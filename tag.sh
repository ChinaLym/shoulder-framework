# 执行前先修改版本号
# 以及 parent 工程中包含的 shoulder 版本号。<shoulder.version>0.3</shoulder.version><!-- shoulder-version -->
# 开启部署前签名
# 修改 <phase>deploy</phase><!-- gpg phase verify deploy --> 至少提前至 verify 才可以保证发布前签名已经存在
git tag -a v0.3 -m "对应 `0.2` 版本，第2个预览发布版，内容见 CHANGELOG.md"
git push origin --tags
