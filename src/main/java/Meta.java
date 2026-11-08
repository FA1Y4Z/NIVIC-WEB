public class Meta {

    private int id;
    private String nome;
    private double valorObjetivo;
    private double valorAtual;
    private String dataLimite;

    public Meta(int id, String nome, double valorObjetivo, double valorAtual, String dataLimite) {
        this.id = id;
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.valorAtual = valorAtual;
        this.dataLimite = dataLimite;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getValorObjetivo() {
        return valorObjetivo;
    }

    public double getValorAtual() {
        return valorAtual;
    }

    public String getDataLimite() {
        return dataLimite;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setValorObjetivo(double valorObjetivo) {
        this.valorObjetivo = valorObjetivo;
    }

    public void setValorAtual(double valorAtual) {
        this.valorAtual = valorAtual;
    }

    public void setDataLimite(String dataLimite) {
        this.dataLimite = dataLimite;
    }
}