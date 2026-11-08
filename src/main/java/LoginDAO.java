import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {

    public Usuario autenticar(String email, String senha) {

        String sql = """
                SELECT id, nome, email, senha
                FROM usuarios
                WHERE email = ? AND senha = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, email);
            comando.setString(2, senha);

            ResultSet resultado = comando.executeQuery();

            if (resultado.next()) {

                Usuario usuario = new Usuario(
                        resultado.getString("nome"),
                        resultado.getString("email"),
                        resultado.getString("senha")
                );

                usuario.setId(resultado.getInt("id"));

                return usuario;
            }

        } catch (SQLException e) {

            System.out.println("Erro ao realizar login: " + e.getMessage());
        }

        return null;
    }
}