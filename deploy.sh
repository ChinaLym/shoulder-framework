mvn clean deploy -p oss -Dmaven.test.skip=true

# 不要包含 -U，将强制下载最新 SNAPSHOT
# 多线程并行编译-Dmaven.compile.fork=true，-T 指定每核线程数如，-Dmaven.compile.fork=true -T 2C
# 适当增加内存 -Xms1g -Xmx1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256M
# -Dmaven.test.skip=true 跳过测试
