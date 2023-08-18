mvn clean deploy -p oss -Dmaven.test.skip=true -T 1C

# 不要包含 -U，将强制下载最新 SNAPSHOT
# 多线程并行编译-Dmaven.compile.fork=true，-T 指定每核线程数如，-Dmaven.compile.fork=true -T 2C，IDEA 中直接输入线程数即可
# 本工程较大，需要适当增加内存 -Xms1g -Xmx1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256M
# -Dmaven.test.skip=true 跳过测试


# 发布前修改版本号
# <version>0.6</version><!-- shoulder-version -->
# <shoulder.version>0.6</shoulder.version><!-- shoulder-version -->
# 开启部署前签名
# 修改 <phase>verify</phase><!-- gpg phase verify deploy --> 至少提前至 verify 才可以保证发布前签名已经存在，而开发分支为了打包install更快，会设置为deploy跳过
# 放开 gpg 插件的注释

# ------------------
# 新版本时，修改版本号
# 开发阶段，修改签名阶段延迟签名、修改javadoc / attachSource 阶段，加速编译和，本地安装。
