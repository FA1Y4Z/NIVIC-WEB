import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UsuarioDAO {

    private static final int ERRO_EMAIL_DUPLICADO = 1062;

    public boolean cadastrar(Usuario usuario) {

        String sql = """
                INSERT INTO usuarios (nome, email, senha)
                VALUES (?, ?, ?)
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenha());

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {

                if (resultado.next()) {
                    usuario.setId(resultado.getInt(1));
                    return true;
                }
            }

        } catch (SQLException e) {

            if (e.getErrorCode() == ERRO_EMAIL_DUPLICADO) {
                System.out.println("Já existe um usuário cadastrado com esse email.");
            } else {
                System.out.println("Erro ao cadastrar usuário no banco: " + e.getMessage());
            }
        }

        return false;
    }


    public ArrayList<Usuario> listar() {

        String sql = "SELECT id, nome, email, senha FROM usuarios ORDER BY id";

        ArrayList<Usuario> usuarios = new ArrayList<>();

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                usuarios.add(montar(resultado));
            }

            return usuarios;

        } catch (SQLException e) {

            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }

        return null;
    }


    public Usuario buscarPorId(int id) {

        String sql = "SELECT id, nome, email, senha FROM usuarios WHERE id = ?";

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return montar(resultado);
                }
            }

        } catch (SQLException e) {

            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }

        return null;
    }

    public boolean atualizar(Usuario usuario) {

        String sql = """
                UPDATE usuarios
                SET nome = ?, email = ?, senha = ?
                WHERE id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenha());
            comando.setInt(4, usuario.getId());

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            if (e.getErrorCode() == ERRO_EMAIL_DUPLICADO) {
                System.out.println("Já existe um usuário cadastrado com esse email.");
            } else {
                System.out.println("Erro ao atualizar usuário: " + e.getMessage());
            }
        }

        return false;
    }


    public boolean excluir(int id) {

        try (Connection conexao = Conexao.conectar()) {

            conexao.setAutoCommit(false);

            try {

                executarExclusao(conexao, "DELETE FROM receitas WHERE usuario_id = ?", id);
                executarExclusao(conexao, "DELETE FROM despesas WHERE usuario_id = ?", id);
                executarExclusao(conexao, "DELETE FROM metas WHERE usuario_id = ?", id);

                int linhas = executarExclusao(conexao, "DELETE FROM usuarios WHERE id = ?", id);

                conexao.commit();

                return linhas > 0;

            } catch (SQLException e) {

                conexao.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.out.println("Erro ao excluir usuário: " + e.getMessage());
        }

        return false;
    }

    private int executarExclusao(Connection conexao, String sql, int id) throws SQLException {

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);

            return comando.executeUpdate();
        }
    }

    private Usuario montar(ResultSet resultado) throws SQLException {

        Usuario usuario = new Usuario(
                resultado.getString("nome"),
                resultado.getString("email"),
                resultado.getString("senha")
        );

        usuario.setId(resultado.getInt("id"));

        return usuario;
    }
}
