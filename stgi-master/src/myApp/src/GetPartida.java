import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class GetPartida extends HttpServlet {
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

        String idPartida = req.getParameter("idPartida");

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
        JSONObject respuestaJSON = new JSONObject();

        respuestaJSON.put("idPartida", idPartida);
        respuestaJSON.put("nombreUsuario", nombreUsuario);

        String estadoPartida = "";
        String colorJugador = "";
        boolean esTurno = false;

        try {
            Connection conexion;

            // Establecer la conexión con la base de datos
            conexion = DriverManager.getConnection(
                    "jdbc:postgresql://aws-0-eu-central-1.pooler.supabase.com:5432/postgres?user=postgres.ifrsivpeaorjtqulwbbz&password=stgin@Upv20");
            // Consulta SQL para obtener los juegos en proceso del usuario
            String query = "SELECT j.estado_partida, uj.color_usuario, j.turno_color = uj.color_usuario AS es_turno, u2.nombre as nombre_contrincante " +
                    "FROM partidas j " +
                    "JOIN usuarios_partida uj ON j.id = uj.partida_id " +
                    "JOIN usuarios u ON uj.usuario_id = u.id " +
                    "JOIN usuarios_partida uj2 ON j.id = uj2.partida_id " +
                    "JOIN usuarios u2 ON uj2.usuario_id = u2.id " +
                    "WHERE j.id = ? AND u.nombre = ? AND u.id != uj2.usuario_id";
            PreparedStatement ps = conexion.prepareStatement(query);
            ps.setInt(1, Integer.parseInt(idPartida));
            ps.setString(2, nombreUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                estadoPartida = rs.getString("estado_partida");
                colorJugador = rs.getString("color_usuario");
                esTurno = rs.getBoolean("es_turno");
                String nombreContrincante = rs.getString("nombre_contrincante");

                // Agregar todos los datos al JSON de respuesta
                respuestaJSON.put("idPartida", idPartida);
                respuestaJSON.put("nombreUsuario", nombreUsuario);
                respuestaJSON.put("colorJugador", colorJugador);
                respuestaJSON.put("esTurno", esTurno);
                respuestaJSON.put("nombreContrincante", nombreContrincante);
            }

            int maxColumnas =  6; // Ajusta este valor según el número de columnas en tu tabla

            JSONArray estadoPartidaArray = new JSONArray();

            ps = conexion.prepareStatement(
                    "SELECT color_usuario, fila, columna " +
                            "FROM posiciones " +
                            "WHERE partida_id = ? " +
                            "ORDER BY fila, columna");
            ps.setInt(1, Integer.parseInt(idPartida));
            rs = ps.executeQuery();



            // Crear una matriz bidimensional para el estado de la partida
            String[][] estadoPartidaMatrix = new String[maxColumnas][maxColumnas];

            // Llenar la matriz con valores nulos
            for (int i = 0; i < maxColumnas; i++) {
                for (int j = 0; j < maxColumnas; j++) {
                    estadoPartidaMatrix[i][j] = "null";
                }
            }

            int fila;
            int columna;
            String color;

            while (rs.next()) {
                fila = rs.getInt("fila");
                columna = rs.getInt("columna");
                color = rs.getString("color_usuario");

                estadoPartidaMatrix[fila-1][columna-1] = color;
            }


            estadoPartidaArray.put(estadoPartidaMatrix);
            if (isTableroCompleto(estadoPartidaMatrix)) {
                // Contar las secuencias para cada color y longitud
                int secuenciasRojo4 = contarSecuencias(estadoPartidaMatrix, "rojo", 4);
                int secuenciasRojo5 = contarSecuencias(estadoPartidaMatrix, "rojo", 5);
                int secuenciasRojo6 = contarSecuencias(estadoPartidaMatrix, "rojo", 6);

                int secuenciasAzul4 = contarSecuencias(estadoPartidaMatrix, "azul", 4);
                int secuenciasAzul5 = contarSecuencias(estadoPartidaMatrix, "azul", 5);
                int secuenciasAzul6 = contarSecuencias(estadoPartidaMatrix, "azul", 6);

                // Construir el JSON de respuesta

                respuestaJSON.put("estadoPartida", "terminada");
                respuestaJSON.put("puntos rojo de 4", secuenciasRojo4);
                respuestaJSON.put("puntos rojo de 5", secuenciasRojo5);
                respuestaJSON.put("puntos rojo de 6", secuenciasRojo6);
                respuestaJSON.put("puntos azul de 4", secuenciasAzul4);
                respuestaJSON.put("puntos azul de 5", secuenciasAzul5);
                respuestaJSON.put("puntos azul de 6", secuenciasAzul6);

                out.print(respuestaJSON.toString());
                return;
            }

// Agregar todos los datos al JSON de respuesta
            respuestaJSON.put("idPartida", idPartida);
            respuestaJSON.put("colorJugador", colorJugador);
            respuestaJSON.put("esTurno", esTurno);
            respuestaJSON.put("estadoPartida", estadoPartidaArray);

// Escribir el JSON en la respuesta
            out.print(respuestaJSON.toString());

        } catch (SQLException e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al recuperar los juegos: " + e.getMessage());
        } finally {
            out.close();
        }
    }

    private boolean isTableroCompleto(String[][] tablero) {
        for (String[] fila : tablero) {
            for (String ficha : fila) {
                if (ficha.equals("null")) {
                    return false; // Si hay alguna casilla vacía, el tablero no está completo
                }
            }
        }
        return true; // Si no hay casillas vacías, el tablero está completo
    }

    // Método para verificar secuencias de fichas consecutivas
    private int contarSecuencias(String[][] tablero, String color, int longitud) {
        int secuencias = 0;
        List<String> visitadas = new ArrayList<>();

        // Buscar secuencias horizontales
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j <= tablero[i].length - longitud; j++) {
                boolean secuenciaValida = true;
                for (int k = 0; k < longitud; k++) {
                    if (!tablero[i][j + k].equals(color)) {
                        secuenciaValida = false;
                        break;
                    }
                }
                if (secuenciaValida && !visitadas.contains(i + "-" + j)) {
                    secuencias++;
                    for (int k = 0; k < longitud; k++) {
                        visitadas.add(i + "-" + (j + k));
                    }
                }
            }
        }

        // Buscar secuencias verticales
        for (int i = 0; i <= tablero.length - longitud; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                boolean secuenciaValida = true;
                for (int k = 0; k < longitud; k++) {
                    if (!tablero[i + k][j].equals(color)) {
                        secuenciaValida = false;
                        break;
                    }
                }
                if (secuenciaValida && !visitadas.contains(i + "-" + j)) {
                    secuencias++;
                    for (int k = 0; k < longitud; k++) {
                        visitadas.add((i + k) + "-" + j);
                    }
                }
            }
        }

        // Buscar secuencias diagonales (de izquierda a derecha)
        for (int i = 0; i <= tablero.length - longitud; i++) {
            for (int j = 0; j <= tablero[i].length - longitud; j++) {
                boolean secuenciaValida = true;
                for (int k = 0; k < longitud; k++) {
                    if (!tablero[i + k][j + k].equals(color)) {
                        secuenciaValida = false;
                        break;
                    }
                }
                if (secuenciaValida && !visitadas.contains(i + "-" + j)) {
                    secuencias++;
                    for (int k = 0; k < longitud; k++) {
                        visitadas.add((i + k) + "-" + (j + k));
                    }
                }
            }
        }

        // Buscar secuencias diagonales (de derecha a izquierda)
        for (int i = 0; i <= tablero.length - longitud; i++) {
            for (int j = tablero[i].length - 1; j >= longitud - 1; j--) {
                boolean secuenciaValida = true;
                for (int k = 0; k < longitud; k++) {
                    if (!tablero[i + k][j - k].equals(color)) {
                        secuenciaValida = false;
                        break;
                    }
                }
                if (secuenciaValida && !visitadas.contains(i + "-" + j)) {
                    secuencias++;
                    for (int k = 0; k < longitud; k++) {
                        visitadas.add((i + k) + "-" + (j - k));
                    }
                }
            }
        }

        return secuencias;
    }


}
