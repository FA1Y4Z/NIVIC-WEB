import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MetaDAO {

    public boolean cadastrar(Meta meta, int usuarioId) {

        String sql = """
                INSERT INTO metas (nome, valor_objetivo, valor_atual, data_limite, usuario_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            comando.setString(1, meta.getNome());
            comando.setDouble(2, meta.getValorObjetivo());
            comando.setDouble(3, meta.getValorAtual());
            comando.setString(4, meta.getDataLimite());
            comando.setInt(5, usuarioId);

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {

                if (resultado.next()) {
                    meta.setId(resultado.getInt(1));
                    return true;
                }
            }

        } catch (SQLException e) {

            System.out.println("Erro ao cadastrar meta no banco: " + e.getMessage());
        }

        return false;
    }


    public ArrayList<Meta> listar(int usuarioId) {

        String sql = """
                SELECT id, nome, valor_objetivo, valor_atual, data_limite
                FROM metas
                WHERE usuario_id = ?
                ORDER BY data_limite, id
                """;

        ArrayList<Meta> metas = new ArrayList<>();

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);

            try (ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {
                    metas.add(montar(resultado));
                }
            }

            return metas;

        } catch (SQLException e) {

            System.out.println("Erro ao listar metas: " + e.getMessage());
        }

        return null;
    }


    public Meta buscarPorId(int id, int usuarioId) {

        String sql = """
                SELECT id, nome, valor_objetivo, valor_atual, data_limite
                FROM metas
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.setInt(2, usuarioId);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return montar(resultado);
                }
            }

        } catch (SQLException e) {

            System.out.println("Erro ao buscar meta: " + e.getMessage());
        }

        return null;
    }

    public boolean atualizar(Meta meta, int usuarioId) {

        String sql = """
                UPDATE metas
                SET nome = ?, valor_objetivo = ?, valor_atual = ?, data_limite = ?
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, meta.getNome());
            comando.setDouble(2, meta.getValorObjetivo());
            comando.setDouble(3, meta.getValorAtual());
            comando.setString(4, meta.getDataLimite());
            comando.setInt(5, meta.getId());
            comando.setInt(6, usuarioId);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Erro ao atualizar meta: " + e.getMessage());
        }

        return false;
    }

    public boolean excluir(int id, int usuarioId) {

        String sql = """
                DELETE FROM metas
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.setInt(2, usuarioId);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Erro ao excluir meta: " + e.getMessage());
        }

        return false;
    }

    private Meta montar(ResultSet resultado) throws SQLException {

        return new Meta(
                resultado.getInt("id"),
                resultado.getString("nome"),
                resultado.getDouble("valor_objetivo"),
                resultado.getDouble("valor_atual"),
                resultado.getString("data_limite")
        );
    }
}
