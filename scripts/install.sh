# install script ==========
export MAVEN_OPTS="-Xmx400m -Xms400m"
mvn clean install -P release -Dmaven.test.skip=true -B -V -T 1.5C
echo 'FINISHED TO [install] !!'
# install script end ==========