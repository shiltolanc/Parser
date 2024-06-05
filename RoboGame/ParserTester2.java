import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import javax.swing.JFileChooser;

public class ParserTester2{

    /**
     * For testing a parser without requiring the world or the game:
     */
    public static void main(String[] args) {
        System.out.println("Testing parser");
        System.out.println("=======================");
        System.out.println("For each program in a collection of test programs,");
        System.out.println("it attempts to parse the program.");
        System.out.println("It reports the program and what the parser should do");
        System.out.println("and then reports whether the parser failed, generated a null tree, or succeeded.");
        System.out.println("It then prints the tree.");
        System.out.println("================================================================");
        Parser parser = new Parser();

        for (int stage=0; stage<4; stage++){
            System.out.println("\nTesting Parser on Stage "+stage+":\n");
            for (String[] test : programs[stage]){
                boolean valid= test[0]=="GOOD";
                String program = test[1];
                String message = test[2];
                System.out.println("------\nParsing: "+ (valid?"valid: ":"INVALID: ")+program+ " ["+message+"]");
                try{
                    ProgramNode ast = parser.parse(new Scanner(program));
                    String printedForm = (ast!=null)?ast.toString().replaceAll("\\n", " "):"null tree";
                    if (valid){
                        if (ast!=null){System.out.println("OK, valid program: "+printedForm);}
                        else          {System.out.println("BAD, failed to generate tree for valid program");}
                    }
                    else {
                        if (ast!=null){System.out.println("BAD, program is invalid, parser gave: "+printedForm);}
                        else          {System.out.println("???, program is invalid, parser did not throw exception but did not build a tree");}

                    }
                }
                catch (ParserFailureException e) {
                    System.out.println("  parser exception: "+e.toString().replaceAll("\\n", " "));
                    if (valid) {System.out.println("BAD, threw exception for a valid program");}
                    else       {System.out.println("OK, parser correctly threw an exception");}
                }
                catch (Exception e) {
                    System.out.println("BAD, Parser broke with some other kind of exception: ");
                    e.printStackTrace(System.out);
                }
            }
            System.out.println("Done");
        }
    }




    private static final String[][][] programs = new String[][][]{
            {//STAGE 0
                    {"GOOD", "move;",  "move action"},
                    {"GOOD", "turnL;",  "turnL action"},
                    {"GOOD", "turnR;",  "turnR action"},
                    {"GOOD", "takeFuel;",  "takeFuel action"},
                    {"GOOD", "wait;",  "wait action"},
                    {"GOOD", "move; turnL; turnR; move; takeFuel; ",  "sequence of actions"},
                    {"GOOD", "loop{move ;}",  "loop with a Block with one action"},
                    {"GOOD", "loop{move; wait; turnL; turnR;}",  "loop with a Block with four actions"},
                    {"GOOD", "loop{move; loop{turnL;}}",  "nested loop"},
                    {"GOOD", "move; turnL; turnR; move; takeFuel; loop{move; turnR; wait;}",  "all stage 0 elements"},

                    {"BAD", "move; turnR move;",  "missing ;"},
                    {"BAD", "move; turnR: move;",  ": instead of ;"},
                    {"BAD", "move; turnL; turnRight; move;",  "invalid action turnRight"},
                    {"BAD", "loop{}",  "Block in a loop with no statements"},
                    {"BAD", "{move;}",  "Block not inside a loop"},
                    {"BAD", "loop{move; turnL;",  "Block with no close }"},
                    {"BAD", "loop{move; loop{turnL;}",  "nested loop with one missing close } on block "},
                    {"BAD", "loop{move; loop{turnL;",  "nested loop with two missing close } on blocks "},
                    {"BAD", "loop{move; turnL;}}",  "Block with extra close }"},
            },
            {//STAGE 1:
                    {"GOOD", "while(eq(fuelLeft, 2)) { wait; }",   "while and condition using eq and  fuelLeft"},
                    {"GOOD", "if(lt(oppLR, 2)) { wait; }",   "if with condition using lt and oppLR"},
                    {"GOOD", "if(gt(oppFB, 2)) { move; }",   "if with condition using gt and oppFB"},
                    {"GOOD", "if(eq(numBarrels, 1)) {turnL;}",   "if with condition using eq and numbBarrels"},
                    {"GOOD", "while(lt(barrelLR, 1)) {turnR;}",   "while with condition using lt and barrelLR"},
                    {"GOOD", "while(gt(barrelFB, 1)) {wait;}",   "while with condition using gt and barrelFB"},
                    {"GOOD", "while(eq(wallDist, 0)) {turnL; wait;}",   "while with condition using eq and wallDis"},

                    {"GOOD", "while(gt(wallDist, 0)) {while(eq(fuelLeft, 4)) {turnL;}}",   "while with nested while"},
                    {"GOOD", "while(gt(wallDist, 0)) {if(eq(fuelLeft, 4)) {turnL;}}",   "while with nested if"},
                    {"GOOD", "if(gt(wallDist, 0)) {if(eq(fuelLeft, 4)) {turnL;}}",   "if with nested if"},
                    {"GOOD", "if(gt(wallDist, 0)) {while(eq(fuelLeft, 4)) {turnL;}}",   "if with nested while"},
                    {"GOOD", "move; while(gt(wallDist, 0)) {turnL;} if(eq(fuelLeft, 4)) {turnL;} wait;",   "sequence of 4 statements, including an if an a while"},

                    {"BAD", "while{move;}",   "while needs a condition"},
                    {"BAD", "if{move;}",   "if needs a condition"},
                    {"BAD", "while(){move;}",   "while can't have an empty condition"},
                    {"BAD", "if(){move;}",   "if can't have an empty condition"},
                    {"BAD", "if(eq(fuelLeft, 1) {move;}",   "Condition in if must have closing )"},
                    {"BAD", "if eq(fuelLeft, 1) {move;}",   "Condition in if must have opening )"},
                    {"BAD", "while(eq(fuelLeft, 1) {move;}",   "Condition in while must have closing )"},
                    {"BAD", "while eq(fuelLeft, 1) {move;}",   "Condition in while must have opening )"},

                    {"BAD", "while(eq(fuelLeft, 2) move;",   "while must have a block, not a statement."},
                    {"BAD", "if(eq(fuelLeft, 2) move;",   "if must have a block, not a statement."},
                    {"BAD", "while(eq(fuelLeft, 1)){}",   "block in a while must have at least one statement"},
                    {"BAD", "if(eq(fuelLeft, 1)){}",   "block in an if must have at least one statement"},

                    {"BAD", "if(eq(fuelLeft, 2)) move;",   "if must have a block, "},
                    {"BAD", "if(shieldOn){shieldOff;}",   "can't have an action as a boolean"},
                    {"BAD", "if(gt(turnL, 1)) {move;}",   "can't have an action as a sensor."},
                    {"BAD", "loop(gt(turnL, 1)){move; turnL;}",  "loop cannot have a condition"},
            },
            {//STAGE 2

                    {"GOOD", "move(3);",  "move with number argument"},
                    {"GOOD", "move(fuelLeft);",  "move with sensor argument"},
                    {"GOOD", "move(add(fuelLeft,2));",  "move with add argument"},
                    {"GOOD", "move(mul(oppLR,2));",  "move with mul argument"},
                    {"GOOD", "wait(sub(oppFB,2));",  "wait with sub argument"},
                    {"GOOD", "wait(div(oppLR,2));",  "wait with div argument"},
                    {"GOOD", "wait(div(add(3, 5), sub(mul(oppLR,2),sub(5, 6))));",  "wait with complex nested expression"},
                    {"GOOD", "if (lt(add(3,4), sub(10,2))) { wait; } else {move;}",  "lt on expressions, if with else"},
                    {"GOOD", "if (gt(mul(3,4), div(100,2))) { wait; } else {move;}",  "gt on expressions, if with else"},
                    {"GOOD", "while (eq(mul(3,add(1, 4)), 10)) { wait;}",  "eq with nested expression, "},
                    {"GOOD", "if (and(lt(3,4),gt(10,2))) { wait; } else {move;}",  "condition with and"},
                    {"GOOD", "if (or(lt(3,4),gt(10,2))) { wait; } else {move;}",  "condition with or"},
                    {"GOOD", "if (not(lt(4,3))) { wait; } else {move;}",  "condition with not"},
                    {"GOOD", "if (or(and(lt(3,4),gt(10,2)), not(not(lt(4,3))))) { wait; } else {move;}",  "nested ands, ors, nots"},
                    {"GOOD", "if (eq(oppLR,3)) { wait; } else {move;}",  "wait"},
                    {"GOOD", "if (eq(barrelFB,3)) { wait; } else {move;}",  "move"},
                    {"GOOD", "if (eq(3,oppLR)) { wait; } else {move;}",  "wait"},
                    {"GOOD", "if (eq(3,barrelFB)) { wait; } else {move;}",  "move"},

                    {"BAD", "turnL(3);", "turnL should not have an argument"},
                    {"BAD", "turnR(fuelLeft);",  "turnR should not have an argument"},
                    {"BAD", "turnAround(oppLR);",  "turnAround should not have an argument"},
                    {"BAD", "move();", "move with an argument needs an argument"},
                    {"BAD", "move(3, 4);", "move must not have two arguments"},
                    {"BAD", "if(lt(3, 4)){move;} else", "else clause must have a block"},
                    {"BAD", "if(lt(3, 4)){move;} else move;", "else clause must have a block"},
                    {"BAD", "if(lt(3,4)) else {move;}", "if must have a then part as well as an else"},
                    {"BAD", "while (and(lt(3,4), gt(5, 3), eq(2,2))) {move;}", "and(..) must not have more than 2 arguments"},
                    {"BAD", "while (and(lt(3,4))) {move;}", "and(..) must not have just 1 argument"},
                    {"BAD", "while (and()) {move;}", "and(..) must not have 0 arguments"},
                    {"BAD", "while (or(lt(3,4), gt(5, 3), eq(2,2))) {move;}", "or(..) must not have more than 2 arguments"},
                    {"BAD", "while (or(lt(3,4))) {move;}", "or(..) must not have just 1 argument"},
                    {"BAD", "while (or()) {move;}", "or(..) must not have 0 arguments"},
                    {"BAD", "while (not(lt(3,4),gt(4,5))) {move;}", "not(..) must not have more than 1 argument"},
                    {"BAD", "while (not()) {move;}", "not(..) must not have 0 arguments"},
                    {"BAD", "while (and(3,4)) {move;}", "and(..) must not have numeric arguments"},
                    {"BAD", "while (or(3,fuelLeft)) {move;}", "or(..) must not have numeric arguments"},
                    {"BAD", "while (not(3)) {move;}", "not(..) must not have numeric arguments"},
                    {"BAD", "wait(add(5));",  "add must not have just 1 argument"},
                    {"BAD", "wait(add());",  "add must not have 0 arguments"},
                    {"BAD", "wait(sub(5));",  "sub must not have just 1 argument"},
                    {"BAD", "wait(sub());",  "sub must not have 0 arguments"},
                    {"BAD", "wait(mul(5));",  "mul must not have just 1 argument"},
                    {"BAD", "wait(mul());",  "mul must not have 0 arguments"},
                    {"BAD", "wait(div(5));",  "div must not have just 1 argument"},
                    {"BAD", "wait(div());",  "div must not have 0 arguments"},
                    {"BAD", "wait(add(5, lt(3, 4)));",  "add must not have boolean arguments"},
                    {"BAD", "wait(sub(lt(3, 4),5));",  "sub must not have boolean arguments"},
                    {"BAD", "wait(mul(5, takeFuel));",  "add must not have action arguments"},
                    {"BAD", "wait(div(turnL,5));",  "div must not have action arguments"},
            },
            {//STAGE 3
                    //elif in if
                    //optional args to barrelLR and barrelFB
                    //variable and assignments (and use of vars in expressions and relops.
                    {"GOOD", "if (lt(3,4)) {wait;} elif(gt(10,2)) {move;} elif(eq(4,3)) { turnL; } else {turnR;}",  "two elif clauses with else"},
                    {"GOOD", "if (lt(3,4)) {wait;} elif(gt(10,2)) {move;}",  "one elif clause with no else"},
                    {"GOOD", "wait(barrelLR(3))", "barrelLR with argument"},
                    {"GOOD", "wait(barrelFB(add(1,fuelLeft))", "barrelFB with argument"},
                    {"GOOD", "$a=3 move($a)", "variable assignment and use in move argument"},
                    {"GOOD", "$a=3 while(lt($a, fuelLeft){$a=add($a,1) move;}", "variable assignment and use in while condition, and expression"},
                    {"GOOD", "$a=3 $b=4 if(eq($a, mul($b,3))){wait($a)}", "variable assignments and use in if condition"},
            }
    };

}