import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


public class Registro extends HttpServlet {
    public void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        // Configurar el tipo de contenido de la respuesta
        res.setContentType("application/json");

        // Responder a la petición preflight
        res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        res.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, credentials");
        res.addHeader("Access-Control-Allow-Credentials", "true");
        // Finalizar la respuesta
        res.setStatus(HttpServletResponse.SC_OK);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        // Configurar el tipo de contenido de la respuesta
        res.setContentType("application/json");

        // Permitir el acceso desde cualquier origen (para desarrollo)
        res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        // Permitir métodos HTTP específicos
        res.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        // Permitir cabeceras específicas
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        res.addHeader("Access-Control-Allow-Credentials", "true");


        PrintWriter out = res.getWriter();
        String nombre = "";
        String contrasenya = "";
        try {
            // Leer el cuerpo de la solicitud
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            // Convertir la cadena en un objeto JSON
            JSONObject jsonRecibido = new JSONObject(sb.toString());

            // Obtener los valores de los atributos nombre y contraseña
            nombre = jsonRecibido.getString("nombre");
            contrasenya = jsonRecibido.getString("contrasena");
        } catch (Exception e) {
            out.println("Error desmontando el JSON: " + e);
        }



        try {
            // Datos de conexión a la base de datos
            Connection conexion;
            ResultSet resultado;

            // Se carga el driver JDBC para PostgreSQL
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException ex) {
                System.out.println("Error al registrar el driver de PostgreSQL: " + ex);
            }
            conexion = DriverManager.getConnection(
                    "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.ifrsivpeaorjtqulwbbz&password=stgin@Upv20");

            PreparedStatement preparedStatement = conexion.prepareStatement(
                    "SELECT * FROM usuarios WHERE nombre = ? "
            );
            preparedStatement.setString(1, nombre);
            resultado = preparedStatement.executeQuery();
            // Mostrar resultados en una tabla HTML
            if (resultado.next()) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out = res.getWriter();
                out.println("Ya existe un usuario con ese nombre");
                out.close();
                conexion.close();
                return;
            }

            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO usuarios (nombre, contraseña) VALUES (?, ?)"
            );
            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, contrasenya); // Asegúrate de tener una variable para la contraseña
            preparedStatement.executeQuery();

            res.setStatus(HttpServletResponse.SC_OK);

            conexion.close();

        } catch (Exception e) {
            out.println("Error " + e);
        }
    }
}
