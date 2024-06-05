import java.util.Scanner;

public interface IntNode {
    public int evaluate(Robot robot);

    IntNode parse(Scanner s);
}
