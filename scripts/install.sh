# install script ==========
export MAVEN_OPTS="-Xmx400m -Xms400m"
return mvn clean install -P release -Dmaven.test.skip=true -B -V -T 1.5C
# install script end ==========