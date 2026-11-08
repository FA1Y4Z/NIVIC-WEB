public class Receita {

    private int id;
    private String descricao;
    private double valor;
    private String tipo;
    private String data;

    public Receita( String descricao, double valor, String tipo, String data) {
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    public String getData() {
        return data;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setData(String data) {
        this.data = data;
    }
    public void setId(int id) {
        this.id = id;
    }
}