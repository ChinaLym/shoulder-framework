mvn clean deploy -p oss -Dmaven.test.skip=true

# 不要包含 -U，将强制下载最新 SNAPSHOT
# 多线程并行编译-Dmaven.compile.fork=true，-T 指定每核线程数如，-Dmaven.compile.fork=true -T 4C，IDEA 中直接输入线程数即可
# 适当增加内存 -Xms1g -Xmx1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256M
# -Dmaven.test.skip=true 跳过测试
# 发布前修改版本号
# <shoulder.version>0.4</shoulder.version><!-- shoulder-version -->
# <shoulder.version>0.4</shoulder.version><!-- shoulder-version -->
# 开启部署前签名
# 修改 <phase>verify</phase><!-- gpg phase verify deploy --> 至少提前至 verify 才可以保证发布前签名已经存在

# ------------------
# 新版本时，修改版本号
# 修改签名阶段
