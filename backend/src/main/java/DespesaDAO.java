import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DespesaDAO {

    public boolean cadastrar(Despesa despesa, int usuarioId) {

        String sql = """
                INSERT INTO despesas (descricao, valor, categoria, data, usuario_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            comando.setString(1, despesa.getDescricao());
            comando.setDouble(2, despesa.getValor());
            comando.setString(3, despesa.getCategoria());
            comando.setString(4, despesa.getData());
            comando.setInt(5, usuarioId);

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {

                if (resultado.next()) {
                    despesa.setId(resultado.getInt(1));
                    return true;
                }
            }

        } catch (SQLException e) {

            System.out.println("Erro ao cadastrar despesa no banco: " + e.getMessage());
        }

        return false;
    }


    public ArrayList<Despesa> listar(int usuarioId) {

        String sql = """
                SELECT id, descricao, valor, categoria, data
                FROM despesas
                WHERE usuario_id = ?
                ORDER BY data DESC, id DESC
                """;

        ArrayList<Despesa> despesas = new ArrayList<>();

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);

            try (ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {
                    despesas.add(montar(resultado));
                }
            }

            return despesas;

        } catch (SQLException e) {

            System.out.println("Erro ao listar despesas: " + e.getMessage());
        }

        return null;
    }


    public Despesa buscarPorId(int id, int usuarioId) {

        String sql = """
                SELECT id, descricao, valor, categoria, data
                FROM despesas
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

            System.out.println("Erro ao buscar despesa: " + e.getMessage());
        }

        return null;
    }

    public boolean atualizar(Despesa despesa, int usuarioId) {

        String sql = """
                UPDATE despesas
                SET descricao = ?, valor = ?, categoria = ?, data = ?
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, despesa.getDescricao());
            comando.setDouble(2, despesa.getValor());
            comando.setString(3, despesa.getCategoria());
            comando.setString(4, despesa.getData());
            comando.setInt(5, despesa.getId());
            comando.setInt(6, usuarioId);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Erro ao atualizar despesa: " + e.getMessage());
        }

        return false;
    }

    public boolean excluir(int id, int usuarioId) {

        String sql = """
                DELETE FROM despesas
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.setInt(2, usuarioId);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Erro ao excluir despesa: " + e.getMessage());
        }

        return false;
    }

    public double calcularTotal(int usuarioId) {

        String sql = """
                SELECT COALESCE(SUM(valor), 0)
                FROM despesas
                WHERE usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return resultado.getDouble(1);
                }
            }

        } catch (SQLException e) {

            System.out.println("Erro ao calcular total de despesas: " + e.getMessage());
        }

        return 0;
    }

    private Despesa montar(ResultSet resultado) throws SQLException {

        return new Despesa(
                resultado.getInt("id"),
                resultado.getString("descricao"),
                resultado.getDouble("valor"),
                resultado.getString("categoria"),
                resultado.getString("data")
        );
    }
}
