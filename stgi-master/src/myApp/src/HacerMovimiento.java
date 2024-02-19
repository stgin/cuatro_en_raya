import org.json.JSONArray;
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

public class HacerMovimiento extends HttpServlet {
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
        BufferedReader reader = req.getReader();

        // Obtener el cuerpo de la solicitud
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // Convertir el cuerpo de la solicitud a un JSONObject
        JSONObject requestBody = new JSONObject(sb.toString());

        // Obtener el ID de la partida y la columna desde el cuerpo de la solicitud
        int idPartida = requestBody.getInt("idPartida");
        int columna = requestBody.getInt("columna");

        // Obtener la sesión HTTP
        HttpSession session = req.getSession(false);

        // Verificar si hay una sesión activa
        if (session == null || session.getAttribute("nombre") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No hay una sesión activa.");
            return;
        }

        // Obtener el nombre del usuario de la sesión
        String nombreUsuario = (String) session.getAttribute("nombre");

        try {
            // Establecer la conexión con la base de datos
            Connection conexion = DriverManager.getConnection(
                    "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.ifrsivpeaorjtqulwbbz&password=stgin@Upv20");
            PreparedStatement ps;
            ResultSet rs;
            // Verificar si es el turno del jugador
            // Verificar si es el turno del jugador
            ps = conexion.prepareStatement(
                    "SELECT turno_color FROM partidas WHERE id = ?");
            ps.setInt(1, idPartida);
            String turnoColor;
            rs = ps.executeQuery();
            if (rs.next()) {
                 turnoColor = rs.getString("turno_color");
                String colorJugador = obtenerColorJugador(nombreUsuario, idPartida, conexion);
                if (!turnoColor.equals(colorJugador)) {
                    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No es tu turno.");
                    return;
                }
            } else {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontró la partida.");
                return;
            }

            // Verificar si la columna tiene espacio para una nueva pieza
           ps = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM posiciones WHERE partida_id = ? AND columna = ?");
            ps.setInt(1, idPartida);
            ps.setInt(2, columna);
            rs = ps.executeQuery();
            if (rs.next()) {
                int cantidadPiezas = rs.getInt(1);
                if (cantidadPiezas >  5) {
                    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "La columna ya tiene  6 piezas.");
                    return;
                }
            }

            // Insertar el nuevo movimiento en la base de datos
            ps = conexion.prepareStatement(
                    "INSERT INTO posiciones (partida_id, fila, columna, color_usuario) VALUES (?, ?, ?, ?)");
            ps.setInt(1, idPartida);
            ps.setInt(2, obtenerFilaVacia(idPartida, columna, conexion)); // Obtener la fila vacía en la columna seleccionada
            ps.setInt(3, columna);
            ps.setString(4, obtenerColorJugador(nombreUsuario, idPartida, conexion)); // Obtener el color del jugador según su nombre y la partida
            ps.executeUpdate();

            ps = conexion.prepareStatement("UPDATE partidas SET turno_color = ? WHERE id = ?");
            if (turnoColor.equals("azul")) {
                ps.setString(1, "rojo");
            } else {
                ps.setString(1, "azul");
            }
            ps.setInt(2, idPartida);
            ps.executeUpdate();

            // Cerrar la conexión
            conexion.close();

            // Enviar respuesta exitosa
            JSONObject jsonResponse = new JSONObject();


            jsonResponse.put("mensaje", "Movimiento realizado con éxito.");

            res.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al realizar el movimiento: " + e.getMessage());
        }
    }

    // Método para obtener la fila vacía en una columna específica
    private int obtenerFilaVacia(int idPartida, int columna, Connection conexion) throws SQLException {
        PreparedStatement ps = conexion.prepareStatement(
                "SELECT MAX(fila) FROM posiciones WHERE partida_id = ? AND columna = ?");
        ps.setInt(1, idPartida);
        ps.setInt(2, columna);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) + 1; // La siguiente fila vacía en la columna
        }
        return 1; // Si la columna está vacía, la fila será la primera
    }

    // Método para obtener el color del jugador según su nombre y la partida
    private String obtenerColorJugador(String nombreUsuario, int idPartida, Connection conexion) throws SQLException {
        PreparedStatement ps = conexion.prepareStatement(
                "SELECT color_usuario FROM usuarios_partida WHERE partida_id = ? AND usuario_id = (SELECT id FROM usuarios WHERE nombre = ?)");
        ps.setInt(1, idPartida);
        ps.setString(2, nombreUsuario);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("color_usuario");
        }
        return ""; // En caso de que no se encuentre el color del jugador
    }
}
