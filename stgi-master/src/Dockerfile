# Install Tomcat 9 & openjdk 8
FROM tomcat:9-jdk8-openjdk
# Copia todos los archivos al contenedor de Tomcat
COPY . /usr/local/tomcat/webapps/
COPY json-20230618.jar /usr/local/tomcat/webapps/myApp/WEB-INF/lib/
COPY postgresql-42.7.1.jar /usr/local/tomcat/webapps/myApp/WEB-INF/lib/


# Compilar IniciarSesion.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/IniciarSesion.java"]

# Compilar Registro.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/Registro.java"]

# Compilar Inicio.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/Inicio.java"]

# Compilar GetPartida.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/GetPartida.java"]

# Compilar CrearPartida.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/CrearPartida.java"]

# Compilar UnirseAPartida.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/UnirseAPartida.java"]

# Compilar HacerMovimiento.java y colocar los archivos .class en el directorio de clases
RUN ["javac", "-cp", ".:/usr/local/tomcat/lib/servlet-api.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/json-20230618.jar:/usr/local/tomcat/webapps/myApp/WEB-INF/lib/postgresql-42.7.1.jar", "-d", "/usr/local/tomcat/webapps/myApp/WEB-INF/classes/", "/usr/local/tomcat/webapps/myApp/src/HacerMovimiento.java"]

# Expone el puerto  8080
EXPOSE  8080

# Ejecuta Tomcat
CMD ["catalina.sh", "run"]
