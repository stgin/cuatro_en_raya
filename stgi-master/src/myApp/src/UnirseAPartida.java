import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnirseAPartida extends HttpServlet {
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
        res.addHeader("Access-Control-Allow-Credentials", "true");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, credentials");

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

        // Obtener el ID de la partida a la que se quiere unir el usuario
        String idPartidaParam = req.getParameter("idPartida");
        int idPartida;
        try {
            idPartida = Integer.parseInt(idPartidaParam);
        } catch (NumberFormatException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "El parámetro idPartida debe ser un número entero válido.");
            return;
        }

        try {
            // Establecer la conexión con la base de datos
            Connection conexion = DriverManager.getConnection(
                    "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.ifrsivpeaorjtqulwbbz&password=stgin@Upv20");

            // Verificar si la partida existe
            PreparedStatement psPartida = conexion.prepareStatement(
                    "SELECT id FROM partidas WHERE id = ?");
            psPartida.setInt(1, idPartida);
            ResultSet rsPartida = psPartida.executeQuery();

            if (!rsPartida.next()) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "La partida con el ID proporcionado no existe.");
                return;
            }

            // Verificar si el usuario ya está en la partida
            PreparedStatement psUsuarioEnPartida = conexion.prepareStatement(
                    "SELECT * FROM usuarios_partida WHERE usuario_id = ? AND partida_id = ?");
            psUsuarioEnPartida.setInt(1, getUserId(nombreUsuario, conexion));
            psUsuarioEnPartida.setInt(2, idPartida);
            ResultSet rsUsuarioEnPartida = psUsuarioEnPartida.executeQuery();

            if (rsUsuarioEnPartida.next()) {
                res.sendError(HttpServletResponse.SC_CONFLICT, "El usuario ya está en esta partida.");
                return;
            }

            // Insertar al usuario en la partida
            PreparedStatement psInsertarUsuarioPartida = conexion.prepareStatement(
                    "INSERT INTO usuarios_partida (usuario_id, partida_id, color_usuario) VALUES (?, ?, ?)");
            psInsertarUsuarioPartida.setInt(1, getUserId(nombreUsuario, conexion));
            psInsertarUsuarioPartida.setInt(2, idPartida);
            psInsertarUsuarioPartida.setString(3, "rojo"); // Por ejemplo, asignar un color al azar
            psInsertarUsuarioPartida.executeUpdate();

            // Cerrar la conexión
            conexion.close();

            // Enviar respuesta
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("mensaje", "El usuario se ha unido a la partida exitosamente.");
            jsonResponse.put("idPartida", idPartida);

            // Enviar respuesta JSON
            out.println(jsonResponse.toString());
            res.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al unirse a la partida: " + e.getMessage());
        }
    }

    // Método para obtener el ID de un usuario dado su nombre
    private int getUserId(String nombreUsuario, Connection conexion) throws SQLException {
        PreparedStatement ps = conexion.prepareStatement("SELECT id FROM usuarios WHERE nombre = ?");
        ps.setString(1, nombreUsuario);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return 0; // En caso de que no se encuentre el usuario
    }
}
