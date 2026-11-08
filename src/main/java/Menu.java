import java.util.Scanner;

public class Menu {

    private Scanner scanner;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    public int mostrarMenuInicial() {

        System.out.println("\n============================");
        System.out.println("           NIVIC");
        System.out.println("============================");
        System.out.println("1 - Login");
        System.out.println("2 - Cadastrar usuário");
        System.out.println("0 - Sair");
        System.out.println("============================");

        return lerInteiro("Escolha: ");
    }

    public int mostrarMenuUsuario(Usuario usuario) {

        System.out.println("\n============================");
        System.out.println("           NIVIC");
        System.out.println("============================");
        System.out.println("Usuário: " + usuario.getNome());
        System.out.println("============================");
        System.out.println("1 - Receitas");
        System.out.println("2 - Despesas");
        System.out.println("3 - Metas");
        System.out.println("4 - Ver saldo");
        System.out.println("5 - Usuário");
        System.out.println("6 - Logout");
        System.out.println("0 - Sair");
        System.out.println("============================");

        return lerInteiro("Escolha: ");
    }


    public int mostrarMenuCrud(String titulo) {

        System.out.println("\n============================");
        System.out.println("  " + titulo);
        System.out.println("============================");
        System.out.println("1 - Cadastrar");
        System.out.println("2 - Listar");
        System.out.println("3 - Buscar por ID");
        System.out.println("4 - Atualizar");
        System.out.println("5 - Excluir");
        System.out.println("0 - Voltar");
        System.out.println("============================");

        return lerInteiro("Escolha: ");
    }

    public int mostrarMenuConta() {

        System.out.println("\n============================");
        System.out.println("  USUÁRIO");
        System.out.println("============================");
        System.out.println("1 - Ver meus dados");
        System.out.println("2 - Listar usuários");
        System.out.println("3 - Atualizar meus dados");
        System.out.println("4 - Excluir minha conta");
        System.out.println("0 - Voltar");
        System.out.println("============================");

        return lerInteiro("Escolha: ");
    }

    public String lerTexto(String rotulo) {

        System.out.print(rotulo);

        return scanner.nextLine();
    }


    public int lerInteiro(String rotulo) {

        String texto = lerTexto(rotulo).trim();

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
