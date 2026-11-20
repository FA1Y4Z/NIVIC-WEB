import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Validacao {

    public static boolean textoValido(String texto) {

        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean dataValida(String data) {

        if (data == null) {
            return false;
        }

        String texto = data.trim();

        if (texto.length() != 10) {
            return false;
        }

        try {
            LocalDate.parse(texto);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static Double converterValor(String texto) {

        if (texto == null) {
            return null;
        }

        try {
            double valor = Double.parseDouble(texto.trim().replace(',', '.'));

            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                return null;
            }

            return valor;

        } catch (NumberFormatException e) {
            return null;
        }
    }



    public static String validarUsuario(String nome, String email, String senha) {

        if (!textoValido(nome)) {
            return "O nome não pode estar vazio.";
        }

        if (!textoValido(email)) {
            return "O email não pode estar vazio.";
        }

        if (!textoValido(senha)) {
            return "A senha não pode estar vazia.";
        }

        return null;
    }

    public static String validarReceita(String descricao, Double valor, String tipo, String data) {

        if (!textoValido(descricao)) {
            return "A descrição não pode estar vazia.";
        }

        if (valor == null || valor <= 0) {
            return "O valor deve ser um número maior que zero.";
        }

        if (!textoValido(tipo)) {
            return "O tipo não pode estar vazio.";
        }

        if (!dataValida(data)) {
            return "Data inválida. Use o formato AAAA-MM-DD.";
        }

        return null;
    }

    public static String validarDespesa(String descricao, Double valor, String categoria, String data) {

        if (!textoValido(descricao)) {
            return "A descrição não pode estar vazia.";
        }

        if (valor == null || valor <= 0) {
            return "O valor deve ser um número maior que zero.";
        }

        if (!textoValido(categoria)) {
            return "A categoria não pode estar vazia.";
        }

        if (!dataValida(data)) {
            return "Data inválida. Use o formato AAAA-MM-DD.";
        }

        return null;
    }

    public static String validarMeta(String nome, Double valorObjetivo, Double valorAtual, String dataLimite) {

        if (!textoValido(nome)) {
            return "O nome não pode estar vazio.";
        }

        if (valorObjetivo == null || valorObjetivo <= 0) {
            return "O valor objetivo deve ser um número maior que zero.";
        }

        if (valorAtual == null || valorAtual < 0) {
            return "O valor atual não pode ser negativo.";
        }

        if (!dataValida(dataLimite)) {
            return "Data inválida. Use o formato AAAA-MM-DD.";
        }

        return null;
    }
}
