# Virtual Classroom - Fixed Build

This archive includes the classroom navigation fix.

Important files:
- src/main/java/com/vc/servlet/DashboardServlet.java
- src/main/java/com/vc/servlet/ClassroomManagementServlet.java
- src/main/webapp/dashboard.jsp
- src/main/webapp/classrooms.jsp

Build:
    mvn clean test
    mvn clean package

The generated WAR is:
    target/virtual-classroom.war

Tomcat deployment:
1. Stop Tomcat.
2. Delete the old:
   webapps/virtual-classroom
   webapps/virtual-classroom.war
3. Copy target/virtual-classroom.war into Tomcat webapps.
4. Start Tomcat.
5. Open:
   http://localhost:8080/virtual-classroom/
