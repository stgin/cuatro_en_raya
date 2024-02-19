import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class Inicio extends HttpServlet {
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
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // Configurar el tipo de contenido de la respuesta
        res.setContentType("application/json");
        // Responder a la petición preflight
        res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        res.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, credentials");
        res.addHeader("Access-Control-Allow-Credentials", "true");

        // Crear un PrintWriter para escribir la respuesta
        PrintWriter out = res.getWriter();

        // Obtener la sesión HTTP
        HttpSession session = req.getSession(false);

        // Verificar si hay una sesión activa
        if (session == null || session.getAttribute("nombre") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No hay una sesión activa.");
            return;
        }

        // Obtener el nombre del usuario de la sesión
        String nombreUsuario = (String) session.getAttribute("nombre");

        // Crear un JSONObject para el usuario
        JSONObject usuarioJson = new JSONObject();
        usuarioJson.put("nombre", nombreUsuario);

        // Crear un JSONArray para los juegos
        JSONArray juegosJson = new JSONArray();

        try {
            Connection conexion;

            // Establecer la conexión con la base de datos
            conexion = DriverManager.getConnection(
                    "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.ifrsivpeaorjtqulwbbz&password=stgin@Upv20");
            // Consulta SQL para obtener los juegos en proceso del usuario
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT j.id, j.estado_partida, j.turno_color " +
                            "FROM partidas j " +
                            "INNER JOIN usuarios_partida uj ON j.id = uj.partida_id " +
                            "WHERE uj.usuario_id = (SELECT id FROM usuarios WHERE nombre = ?)"
            );
            ps.setString(1, nombreUsuario);
            ResultSet rs = ps.executeQuery();

            // Procesar cada juego y agregarlo al JSONArray
            while (rs.next()) {
                JSONObject juegoJson = new JSONObject();
                juegoJson.put("id", rs.getInt("id"));
                juegoJson.put("estado", rs.getString("estado_partida"));
                juegoJson.put("turno", rs.getString("turno_color"));
                juegosJson.put(juegoJson);
            }

            // Agregar el array de juegos al JSON del usuario
            usuarioJson.put("juegos", juegosJson);

            // Escribir el JSON en la respuesta
            out.print(usuarioJson.toString());

        } catch (SQLException e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al recuperar los juegos: " + e.getMessage());
        } finally {
            out.close();
        }
    }
}
