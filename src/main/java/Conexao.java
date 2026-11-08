import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static final String URL =
            System.getenv().getOrDefault(
                    "DB_URL",
                    "jdbc:mariadb://localhost:3306/nivic"
            );

    private static final String USUARIO =
            System.getenv("DB_USERNAME");

    private static final String SENHA =
            System.getenv("DB_PASSWORD");

    public static Connection conectar() throws SQLException {

        if (USUARIO == null || SENHA == null) {
            throw new SQLException(
                    "Configure DB_USERNAME e DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}