import java.util.ArrayList;
import java.util.Scanner;

public class Controller {

    private Menu menu;
    private Calculo calculo;

    private UsuarioDAO usuarioDAO;
    private ReceitaDAO receitaDAO;
    private DespesaDAO despesaDAO;
    private MetaDAO metaDAO;
    private LoginDAO loginDAO;

    private Usuario usuarioAtual;

    public Controller(Scanner scanner) {

        usuarioDAO = new UsuarioDAO();
        receitaDAO = new ReceitaDAO();
        despesaDAO = new DespesaDAO();
        metaDAO = new MetaDAO();
        loginDAO = new LoginDAO();

        this.menu = new Menu(scanner);
        this.calculo = new Calculo();
    }

    public void iniciar() {

        boolean executando = true;

        while (executando) {

            if (usuarioAtual == null) {

                int opcao = menu.mostrarMenuInicial();

                switch (opcao) {

                    case 1:
                        fazerLogin();
                        break;

                    case 2:
                        cadastrarUsuario();
                        break;

                    case 0:
                        executando = false;
                        System.out.println("\nSaindo do NIVIC...");
                        break;

                    default:
                        System.out.println("\nOpção inválida!");
                }

            } else {

                int opcao = menu.mostrarMenuUsuario(usuarioAtual);

                switch (opcao) {

                    case 1:
                        menuReceitas();
                        break;

                    case 2:
                        menuDespesas();
                        break;

                    case 3:
                        menuMetas();
                        break;

                    case 4:
                        verSaldo();
                        break;

                    case 5:
                        menuConta();
                        break;

                    case 6:
                        fazerLogout();
                        break;

                    case 0:
                        executando = false;
                        System.out.println("\nSaindo do NIVIC...");
                        break;

                    default:
                        System.out.println("\nOpção inválida!");
                }
            }
        }
    }

    // ==================================================
    // SESSÃO
    // ==================================================

    private void fazerLogin() {

        System.out.println("\n============================");
        System.out.println("            LOGIN");
        System.out.println("============================");

        String email = menu.lerTexto("Email: ").trim();
        String senha = menu.lerTexto("Senha: ");

        if (!Validacao.textoValido(email) || !Validacao.textoValido(senha)) {
            System.out.println("\nInforme email e senha.");
            return;
        }

        Usuario usuario = loginDAO.autenticar(email, senha);

        if (usuario != null) {

            usuarioAtual = usuario;

            System.out.println("\nLogin realizado com sucesso!");
            System.out.println("Bem-vindo, " + usuario.getNome() + "!");

        } else {

            System.out.println("\nEmail ou senha incorretos.");
        }
    }

    private void fazerLogout() {

        usuarioAtual = null;

        System.out.println("\nLogout realizado com sucesso!");
    }

    private void cadastrarUsuario() {

        System.out.println("\n============================");
        System.out.println("      CADASTRAR USUÁRIO");
        System.out.println("============================");

        String nome = menu.lerTexto("Nome: ").trim();
        String email = menu.lerTexto("Email: ").trim();
        String senha = menu.lerTexto("Senha: ");

        String erro = Validacao.validarUsuario(nome, email, senha);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        Usuario usuario = new Usuario(nome, email, senha);

        if (usuarioDAO.cadastrar(usuario)) {

            System.out.println("\nUsuário cadastrado com sucesso!");
            System.out.println("ID: " + usuario.getId());
            System.out.println("\nAgora você pode fazer login.");

        } else {

            System.out.println("\nNão foi possível cadastrar o usuário.");
        }
    }

    // ==================================================
    // USUÁRIO
    // ==================================================

    private void menuConta() {

        boolean voltar = false;

        while (!voltar && usuarioAtual != null) {

            switch (menu.mostrarMenuConta()) {

                case 1:
                    verMeusDados();
                    break;

                case 2:
                    listarUsuarios();
                    break;

                case 3:
                    atualizarMeusDados();
                    break;

                case 4:
                    excluirMinhaConta();
                    break;

                case 0:
                    voltar = true;
                    break;

                default:
                    System.out.println("\nOpção inválida!");
            }
        }
    }

    private void verMeusDados() {

        Usuario usuario = usuarioDAO.buscarPorId(usuarioAtual.getId());

        if (usuario == null) {
            System.out.println("\nUsuário não encontrado.");
            return;
        }

        System.out.println("\n============================");
        System.out.println("        MEUS DADOS");
        System.out.println("============================");
        System.out.println("ID: " + usuario.getId());
        System.out.println("Nome: " + usuario.getNome());
        System.out.println("Email: " + usuario.getEmail());
    }

    private void listarUsuarios() {

        System.out.println("\n============================");
        System.out.println("         USUÁRIOS");
        System.out.println("============================");

        ArrayList<Usuario> usuarios = usuarioDAO.listar();

        if (usuarios == null) {
            return;
        }

        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
            return;
        }

        for (Usuario usuario : usuarios) {
            System.out.println("\nID: " + usuario.getId());
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Email: " + usuario.getEmail());
        }
    }

    private void atualizarMeusDados() {

        Usuario usuario = usuarioDAO.buscarPorId(usuarioAtual.getId());

        if (usuario == null) {
            System.out.println("\nUsuário não encontrado.");
            return;
        }

        System.out.println("\n============================");
        System.out.println("    ATUALIZAR MEUS DADOS");
        System.out.println("============================");
        System.out.println("(Enter mantém o valor atual)");

        String nome = lerTextoOuManter("Nome", usuario.getNome());
        String email = lerTextoOuManter("Email", usuario.getEmail());

        String senha = menu.lerTexto("Nova senha: ");

        if (senha.isEmpty()) {
            senha = usuario.getSenha();
        }

        String erro = Validacao.validarUsuario(nome, email, senha);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim());
        usuario.setSenha(senha);

        if (usuarioDAO.atualizar(usuario)) {

            usuarioAtual = usuario;

            System.out.println("\nDados atualizados com sucesso!");

        } else {

            System.out.println("\nNão foi possível atualizar os dados.");
        }
    }

    private void excluirMinhaConta() {

        System.out.println("\nIsso apagará sua conta e todas as suas receitas, despesas e metas.");

        String resposta = menu.lerTexto("Tem certeza? (S/N): ").trim();

        if (!resposta.equalsIgnoreCase("S")) {
            System.out.println("\nOperação cancelada.");
            return;
        }

        if (usuarioDAO.excluir(usuarioAtual.getId())) {

            usuarioAtual = null;

            System.out.println("\nConta excluída com sucesso!");

        } else {

            System.out.println("\nNão foi possível excluir a conta.");
        }
    }

    // ==================================================
    // RECEITAS
    // ==================================================

    private void menuReceitas() {

        boolean voltar = false;

        while (!voltar) {

            switch (menu.mostrarMenuCrud("RECEITAS")) {

                case 1:
                    cadastrarReceita();
                    break;

                case 2:
                    listarReceitas();
                    break;

                case 3:
                    buscarReceita();
                    break;

                case 4:
                    atualizarReceita();
                    break;

                case 5:
                    excluirReceita();
                    break;

                case 0:
                    voltar = true;
                    break;

                default:
                    System.out.println("\nOpção inválida!");
            }
        }
    }

    private void cadastrarReceita() {

        System.out.println("\n============================");
        System.out.println("       CADASTRAR RECEITA");
        System.out.println("============================");

        String descricao = menu.lerTexto("Descrição: ").trim();
        Double valor = lerValor("Valor: ");
        String tipo = menu.lerTexto("Tipo: ").trim();
        String data = menu.lerTexto("Data (AAAA-MM-DD): ").trim();

        String erro = Validacao.validarReceita(descricao, valor, tipo, data);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        Receita receita = new Receita(descricao, valor, tipo, data);

        if (receitaDAO.cadastrar(receita, usuarioAtual.getId())) {

            System.out.println("\nReceita cadastrada com sucesso!");
            System.out.println("ID: " + receita.getId());

        } else {

            System.out.println("\nNão foi possível cadastrar a receita.");
        }
    }

    private void listarReceitas() {

        System.out.println("\n============================");
        System.out.println("          RECEITAS");
        System.out.println("============================");

        ArrayList<Receita> receitas = receitaDAO.listar(usuarioAtual.getId());

        if (receitas == null) {
            return;
        }

        if (receitas.isEmpty()) {
            System.out.println("Nenhuma receita cadastrada.");
            return;
        }

        for (Receita receita : receitas) {
            imprimirReceita(receita);
        }
    }

    private void buscarReceita() {

        int id = menu.lerInteiro("ID da receita: ");

        Receita receita = receitaDAO.buscarPorId(id, usuarioAtual.getId());

        if (receita == null) {
            System.out.println("\nReceita não encontrada.");
            return;
        }

        imprimirReceita(receita);
    }

    private void atualizarReceita() {

        int id = menu.lerInteiro("ID da receita: ");

        Receita receita = receitaDAO.buscarPorId(id, usuarioAtual.getId());

        if (receita == null) {
            System.out.println("\nReceita não encontrada.");
            return;
        }

        System.out.println("\n(Enter mantém o valor atual)");

        String descricao = lerTextoOuManter("Descrição", receita.getDescricao());
        Double valor = lerValorOuManter("Valor", receita.getValor());
        String tipo = lerTextoOuManter("Tipo", receita.getTipo());
        String data = lerTextoOuManter("Data (AAAA-MM-DD)", receita.getData());

        String erro = Validacao.validarReceita(descricao, valor, tipo, data);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        receita.setDescricao(descricao);
        receita.setValor(valor);
        receita.setTipo(tipo);
        receita.setData(data);

        if (receitaDAO.atualizar(receita, usuarioAtual.getId())) {
            System.out.println("\nReceita atualizada com sucesso!");
        } else {
            System.out.println("\nNão foi possível atualizar a receita.");
        }
    }

    private void excluirReceita() {

        int id = menu.lerInteiro("ID da receita: ");

        if (receitaDAO.excluir(id, usuarioAtual.getId())) {
            System.out.println("\nReceita excluída com sucesso!");
        } else {
            System.out.println("\nReceita não encontrada.");
        }
    }

    private void imprimirReceita(Receita receita) {

        System.out.println("\nID: " + receita.getId());
        System.out.println("Descrição: " + receita.getDescricao());
        System.out.printf("Valor: R$ %.2f%n", receita.getValor());
        System.out.println("Tipo: " + receita.getTipo());
        System.out.println("Data: " + receita.getData());
    }

    // ==================================================
    // DESPESAS
    // ==================================================

    private void menuDespesas() {

        boolean voltar = false;

        while (!voltar) {

            switch (menu.mostrarMenuCrud("DESPESAS")) {

                case 1:
                    cadastrarDespesa();
                    break;

                case 2:
                    listarDespesas();
                    break;

                case 3:
                    buscarDespesa();
                    break;

                case 4:
                    atualizarDespesa();
                    break;

                case 5:
                    excluirDespesa();
                    break;

                case 0:
                    voltar = true;
                    break;

                default:
                    System.out.println("\nOpção inválida!");
            }
        }
    }

    private void cadastrarDespesa() {

        System.out.println("\n============================");
        System.out.println("       CADASTRAR DESPESA");
        System.out.println("============================");

        String descricao = menu.lerTexto("Descrição: ").trim();
        Double valor = lerValor("Valor: ");
        String categoria = menu.lerTexto("Categoria: ").trim();
        String data = menu.lerTexto("Data (AAAA-MM-DD): ").trim();

        String erro = Validacao.validarDespesa(descricao, valor, categoria, data);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        Despesa despesa = new Despesa(0, descricao, valor, categoria, data);

        if (despesaDAO.cadastrar(despesa, usuarioAtual.getId())) {

            System.out.println("\nDespesa cadastrada com sucesso!");
            System.out.println("ID: " + despesa.getId());

        } else {

            System.out.println("\nNão foi possível cadastrar a despesa.");
        }
    }

    private void listarDespesas() {

        System.out.println("\n============================");
        System.out.println("          DESPESAS");
        System.out.println("============================");

        ArrayList<Despesa> despesas = despesaDAO.listar(usuarioAtual.getId());

        if (despesas == null) {
            return;
        }

        if (despesas.isEmpty()) {
            System.out.println("Nenhuma despesa cadastrada.");
            return;
        }

        for (Despesa despesa : despesas) {
            imprimirDespesa(despesa);
        }
    }

    private void buscarDespesa() {

        int id = menu.lerInteiro("ID da despesa: ");

        Despesa despesa = despesaDAO.buscarPorId(id, usuarioAtual.getId());

        if (despesa == null) {
            System.out.println("\nDespesa não encontrada.");
            return;
        }

        imprimirDespesa(despesa);
    }

    private void atualizarDespesa() {

        int id = menu.lerInteiro("ID da despesa: ");

        Despesa despesa = despesaDAO.buscarPorId(id, usuarioAtual.getId());

        if (despesa == null) {
            System.out.println("\nDespesa não encontrada.");
            return;
        }

        System.out.println("\n(Enter mantém o valor atual)");

        String descricao = lerTextoOuManter("Descrição", despesa.getDescricao());
        Double valor = lerValorOuManter("Valor", despesa.getValor());
        String categoria = lerTextoOuManter("Categoria", despesa.getCategoria());
        String data = lerTextoOuManter("Data (AAAA-MM-DD)", despesa.getData());

        String erro = Validacao.validarDespesa(descricao, valor, categoria, data);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        despesa.setDescricao(descricao);
        despesa.setValor(valor);
        despesa.setCategoria(categoria);
        despesa.setData(data);

        if (despesaDAO.atualizar(despesa, usuarioAtual.getId())) {
            System.out.println("\nDespesa atualizada com sucesso!");
        } else {
            System.out.println("\nNão foi possível atualizar a despesa.");
        }
    }

    private void excluirDespesa() {

        int id = menu.lerInteiro("ID da despesa: ");

        if (despesaDAO.excluir(id, usuarioAtual.getId())) {
            System.out.println("\nDespesa excluída com sucesso!");
        } else {
            System.out.println("\nDespesa não encontrada.");
        }
    }

    private void imprimirDespesa(Despesa despesa) {

        System.out.println("\nID: " + despesa.getId());
        System.out.println("Descrição: " + despesa.getDescricao());
        System.out.printf("Valor: R$ %.2f%n", despesa.getValor());
        System.out.println("Categoria: " + despesa.getCategoria());
        System.out.println("Data: " + despesa.getData());
    }

    // ==================================================
    // METAS
    // ==================================================

    private void menuMetas() {

        boolean voltar = false;

        while (!voltar) {

            switch (menu.mostrarMenuCrud("METAS")) {

                case 1:
                    cadastrarMeta();
                    break;

                case 2:
                    listarMetas();
                    break;

                case 3:
                    buscarMeta();
                    break;

                case 4:
                    atualizarMeta();
                    break;

                case 5:
                    excluirMeta();
                    break;

                case 0:
                    voltar = true;
                    break;

                default:
                    System.out.println("\nOpção inválida!");
            }
        }
    }

    private void cadastrarMeta() {

        System.out.println("\n============================");
        System.out.println("         CADASTRAR META");
        System.out.println("============================");

        String nome = menu.lerTexto("Nome: ").trim();
        Double valorObjetivo = lerValor("Valor objetivo: ");
        Double valorAtual = lerValor("Valor atual: ");
        String dataLimite = menu.lerTexto("Data limite (AAAA-MM-DD): ").trim();

        String erro = Validacao.validarMeta(nome, valorObjetivo, valorAtual, dataLimite);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        Meta meta = new Meta(0, nome, valorObjetivo, valorAtual, dataLimite);

        if (metaDAO.cadastrar(meta, usuarioAtual.getId())) {

            System.out.println("\nMeta cadastrada com sucesso!");
            System.out.println("ID: " + meta.getId());

        } else {

            System.out.println("\nNão foi possível cadastrar a meta.");
        }
    }

    private void listarMetas() {

        System.out.println("\n============================");
        System.out.println("           METAS");
        System.out.println("============================");

        ArrayList<Meta> metas = metaDAO.listar(usuarioAtual.getId());

        if (metas == null) {
            return;
        }

        if (metas.isEmpty()) {
            System.out.println("Nenhuma meta cadastrada.");
            return;
        }

        for (Meta meta : metas) {
            imprimirMeta(meta);
        }
    }

    private void buscarMeta() {

        int id = menu.lerInteiro("ID da meta: ");

        Meta meta = metaDAO.buscarPorId(id, usuarioAtual.getId());

        if (meta == null) {
            System.out.println("\nMeta não encontrada.");
            return;
        }

        imprimirMeta(meta);
    }

    private void atualizarMeta() {

        int id = menu.lerInteiro("ID da meta: ");

        Meta meta = metaDAO.buscarPorId(id, usuarioAtual.getId());

        if (meta == null) {
            System.out.println("\nMeta não encontrada.");
            return;
        }

        System.out.println("\n(Enter mantém o valor atual)");

        String nome = lerTextoOuManter("Nome", meta.getNome());
        Double valorObjetivo = lerValorOuManter("Valor objetivo", meta.getValorObjetivo());
        Double valorAtual = lerValorOuManter("Valor atual", meta.getValorAtual());
        String dataLimite = lerTextoOuManter("Data limite (AAAA-MM-DD)", meta.getDataLimite());

        String erro = Validacao.validarMeta(nome, valorObjetivo, valorAtual, dataLimite);

        if (erro != null) {
            System.out.println("\n" + erro);
            return;
        }

        meta.setNome(nome);
        meta.setValorObjetivo(valorObjetivo);
        meta.setValorAtual(valorAtual);
        meta.setDataLimite(dataLimite);

        if (metaDAO.atualizar(meta, usuarioAtual.getId())) {
            System.out.println("\nMeta atualizada com sucesso!");
        } else {
            System.out.println("\nNão foi possível atualizar a meta.");
        }
    }

    private void excluirMeta() {

        int id = menu.lerInteiro("ID da meta: ");

        if (metaDAO.excluir(id, usuarioAtual.getId())) {
            System.out.println("\nMeta excluída com sucesso!");
        } else {
            System.out.println("\nMeta não encontrada.");
        }
    }

    private void imprimirMeta(Meta meta) {

        double progresso = calculo.calcularProgressoMeta(
                meta.getValorAtual(),
                meta.getValorObjetivo()
        );

        double restante = calculo.calcularValorRestanteMeta(
                meta.getValorObjetivo(),
                meta.getValorAtual()
        );

        System.out.println("\nID: " + meta.getId());
        System.out.println("Nome: " + meta.getNome());
        System.out.printf("Valor objetivo: R$ %.2f%n", meta.getValorObjetivo());
        System.out.printf("Valor atual: R$ %.2f%n", meta.getValorAtual());
        System.out.printf("Valor restante: R$ %.2f%n", restante);
        System.out.printf("Progresso: %.1f%%%n", progresso);
        System.out.println("Data limite: " + meta.getDataLimite());

        if (calculo.metaConcluida(meta.getValorAtual(), meta.getValorObjetivo())) {
            System.out.println("Situação: Meta concluída!");
        } else {
            System.out.println("Situação: Em andamento");
        }
    }

    // ==================================================
    // SALDO
    // ==================================================

    private void verSaldo() {

        int usuarioId = usuarioAtual.getId();

        double totalReceitas =
                receitaDAO.calcularTotal(usuarioId);

        double totalDespesas =
                despesaDAO.calcularTotal(usuarioId);

        double saldo = calculo.calcularSaldo(
                totalReceitas,
                totalDespesas
        );

        System.out.println("\n============================");
        System.out.println("           SALDO");
        System.out.println("============================");

        System.out.printf(
                "Total de receitas: R$ %.2f%n",
                totalReceitas
        );

        System.out.printf(
                "Total de despesas: R$ %.2f%n",
                totalDespesas
        );

        System.out.printf(
                "Saldo atual: R$ %.2f%n",
                saldo
        );
    }

    // ==================================================
    // LEITURA DE DADOS
    // ==================================================

    // Retorna null se o texto digitado não for um número.
    private Double lerValor(String rotulo) {

        return Validacao.converterValor(menu.lerTexto(rotulo));
    }

    // Se o usuário só apertar Enter, mantém o valor atual.
    private String lerTextoOuManter(String rotulo, String atual) {

        String texto = menu.lerTexto(rotulo + " [" + atual + "]: ").trim();

        if (texto.isEmpty()) {
            return atual;
        }

        return texto;
    }

    // Se o usuário só apertar Enter, mantém o valor atual.
    // Retorna null se o texto digitado não for um número.
    private Double lerValorOuManter(String rotulo, double atual) {

        String texto = menu.lerTexto(rotulo + " [" + atual + "]: ").trim();

        if (texto.isEmpty()) {
            return atual;
        }

        return Validacao.converterValor(texto);
    }
}
