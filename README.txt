1. Build the shared libraries

	mvn clean install

2. Update deployment properties

	vi install/build.properties

3. Install to jboss-6.0.0-M5

	cd install
	ant

4. Start JBoss

5. Visit http://localhost:8080/jgroups-chat/App.html