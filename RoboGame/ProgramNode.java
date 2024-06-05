/**
 * Interface for all nodes that can be executed,
 * including the top level program node
 */
import java.util.Scanner;

interface ProgramNode {
    public void execute(Robot robot);

    public ProgramNode parse(Scanner s);
}
