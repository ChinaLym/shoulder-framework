sed -i 's#-SNAPSHOT</shoulder.version>#</shoulder.version>#g' pom.xml
sed -i 's#-SNAPSHOT</shoulder.version>#</shoulder.version>#g' shoulder-archetype-simple/src/main/resources/archetype-resources/pom.xml
find . -name "pom.xml" | xargs sed -i 's#-SNAPSHOT</version><!-- shoulder-version -->#</shoulder.version>#g'
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_package -->#<phase>package</phase><!-- ACTIVE_WITH_package -->#g' pom.xml
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_package -->#<phase>package</phase><!-- ACTIVE_WITH_package -->#g' shoulder-parent/pom.xml
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_verify -->#<phase>verify</phase><!-- ACTIVE_WITH_verify -->#g' pom.xml
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_verify -->#<phase>verify</phase><!-- ACTIVE_WITH_verify -->#g' shoulder-parent/pom.xml
# 2C 每个可用 C 2线程；deploy 主要是发布，与网络相关，-T 4 4个线程 -B batch 批量处理 -V Display version information
mvn clean deploy -Dmaven.test.skip=true -pl shoulder-dependencies -am
mvn clean deploy -Dmaven.test.skip=true -pl shoulder-parent -am
export MAVEN_OPTS="-Xmx400m -Xms400m"

mvn clean deploy -P release -Dmaven.test.skip=true -B -V -T 1.5C
echo 'FINISHED deploy!!'