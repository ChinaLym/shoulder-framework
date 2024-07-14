#
rm -rf /root/.m2/repository/cn/itlym
# remove -SNAPSHOT
sed -i 's#-SNAPSHOT</shoulder.version>#-M1</shoulder.version>#g' shoulder-dependencies/pom.xml
sed -i 's#-SNAPSHOT</shoulder.version>#-M1</shoulder.version>#g' shoulder-archetype-simple/src/main/resources/archetype-resources/pom.xml
find . -name "pom.xml" | xargs sed -i 's#-SNAPSHOT</version><!-- shoulder-version -->#-M1</version><!-- shoulder-version -->#g'
find . -name "*.md" | xargs sed -i 's#DarchetypeVersion=0.8.1#DarchetypeVersion=1.0.0-PRE#g'
# add source and javadoc in shoulder-xxx.jar
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_package -->#<phase>package</phase><!-- ACTIVE_WITH_package -->#g' pom.xml
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_package -->#<phase>package</phase><!-- ACTIVE_WITH_package -->#g' shoulder-parent/pom.xml
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_verify -->#<phase>verify</phase><!-- ACTIVE_WITH_verify -->#g' pom.xml
sed -i 's#<phase>deploy</phase><!-- ACTIVE_WITH_verify -->#<phase>verify</phase><!-- ACTIVE_WITH_verify -->#g' shoulder-parent/pom.xml
# print log
echo 'READY TO deploy-M1!!'
