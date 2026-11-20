import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ReceitaDAO {

    public boolean cadastrar(Receita receita, int usuarioId) {

        String sql = """
                INSERT INTO receitas (descricao, valor, tipo, data, usuario_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            comando.setString(1, receita.getDescricao());
            comando.setDouble(2, receita.getValor());
            comando.setString(3, receita.getTipo());
            comando.setString(4, receita.getData());
            comando.setInt(5, usuarioId);

            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {

                if (resultado.next()) {
                    receita.setId(resultado.getInt(1));
                    return true;
                }
            }

        } catch (SQLException e) {

            System.out.println("Erro ao cadastrar receita no banco: " + e.getMessage());
        }

        return false;
    }

    public ArrayList<Receita> listar(int usuarioId) {

        String sql = """
                SELECT id, descricao, valor, tipo, data
                FROM receitas
                WHERE usuario_id = ?
                ORDER BY data DESC, id DESC
                """;

        ArrayList<Receita> receitas = new ArrayList<>();

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, usuarioId);

            try (ResultSet resultado = comando.executeQuery()) {

                while (resultado.next()) {
                    receitas.add(montar(resultado));
                }
            }

            return receitas;

        } catch (SQLException e) {

            System.out.println("Erro ao listar receitas: " + e.getMessage());
        }

        return null;
    }


    public Receita buscarPorId(int id, int usuarioId) {

        String sql = """
                SELECT id, descricao, valor, tipo, data
                FROM receitas
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

            System.out.println("Erro ao buscar receita: " + e.getMessage());
        }

        return null;
    }

    public boolean atualizar(Receita receita, int usuarioId) {

        String sql = """
                UPDATE receitas
                SET descricao = ?, valor = ?, tipo = ?, data = ?
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, receita.getDescricao());
            comando.setDouble(2, receita.getValor());
            comando.setString(3, receita.getTipo());
            comando.setString(4, receita.getData());
            comando.setInt(5, receita.getId());
            comando.setInt(6, usuarioId);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Erro ao atualizar receita: " + e.getMessage());
        }

        return false;
    }

    public boolean excluir(int id, int usuarioId) {

        String sql = """
                DELETE FROM receitas
                WHERE id = ?
                AND usuario_id = ?
                """;

        try (Connection conexao = Conexao.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, id);
            comando.setInt(2, usuarioId);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Erro ao excluir receita: " + e.getMessage());
        }

        return false;
    }

    public double calcularTotal(int usuarioId) {

        String sql = """
                SELECT COALESCE(SUM(valor), 0)
                FROM receitas
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

            System.out.println("Erro ao calcular total de receitas: " + e.getMessage());
        }

        return 0;
    }

    private Receita montar(ResultSet resultado) throws SQLException {

        Receita receita = new Receita(
                resultado.getString("descricao"),
                resultado.getDouble("valor"),
                resultado.getString("tipo"),
                resultado.getString("data")
        );

        receita.setId(resultado.getInt("id"));

        return receita;
    }
}
