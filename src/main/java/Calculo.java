public class Calculo {

    public double calcularTotalReceitas(double[] receitas) {

        double total = 0;

        for (double receita : receitas) {
            total += receita;
        }

        return total;
    }

    public double calcularTotalDespesas(double[] despesas) {

        double total = 0;

        for (double despesa : despesas) {
            total += despesa;
        }

        return total;
    }

    public double calcularSaldo(double totalReceitas, double totalDespesas) {

        return totalReceitas - totalDespesas;
    }

    public double calcularPercentualGasto(double totalReceitas, double totalDespesas) {

        if (totalReceitas == 0) {
            return 0;
        }

        return (totalDespesas / totalReceitas) * 100;
    }

    public double calcularValorDisponivel(double totalReceitas, double totalDespesas) {

        return totalReceitas - totalDespesas;
    }

    public double calcularMediaDiariaDisponivel(double saldo, int diasRestantes) {

        if (diasRestantes <= 0) {
            return 0;
        }

        return saldo / diasRestantes;
    }

    // Retorna de 0 a 100 (acima de 100% é limitado a 100).
    public double calcularProgressoMeta(double valorAtual, double valorObjetivo) {

        if (valorObjetivo <= 0 || valorAtual < 0) {
            return 0;
        }

        return Math.min(100, (valorAtual / valorObjetivo) * 100);
    }

    public double calcularValorRestanteMeta(double valorObjetivo, double valorAtual) {

        return Math.max(0, valorObjetivo - valorAtual);
    }

    public boolean metaConcluida(double valorAtual, double valorObjetivo) {

        return valorObjetivo > 0 && valorAtual >= valorObjetivo;
    }
}