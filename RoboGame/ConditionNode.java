import java.util.Scanner;

public interface ConditionNode {

    public boolean evaluate(Robot robot);

    ConditionNode parse(Scanner s);
}
