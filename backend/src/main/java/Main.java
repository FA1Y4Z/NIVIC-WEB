import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Controller controller = new Controller(scanner);

        try {
            controller.iniciar();
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada encerrada. Saindo do NIVIC...");
        }

        scanner.close();
    }
}
