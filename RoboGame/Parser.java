import java.util.*;
import java.util.regex.*;
import java.io.PrintStream;

/**
 * See assignment handout for the grammar.
 * You need to implement the parse(..) method and all the rest of the parser.
 * There are several methods provided for you:
 * - several utility methods to help with the parsing
 * See also the TestParser class for testing your code.
 */
public class Parser {

    // Useful Patterns

    static final Pattern NUMPAT = Pattern.compile("-?[1-9][0-9]*|0");
    static final Pattern OPENPAREN = Pattern.compile("\\(");
    static final Pattern CLOSEPAREN = Pattern.compile("\\)");
    static final Pattern OPENBRACE = Pattern.compile("\\{");
    static final Pattern CLOSEBRACE = Pattern.compile("\\}");

    static final Pattern ACT = Pattern.compile("move|turnL|turnR|takeFuel|wait|turnAround|shieldOn|shieldOff");
    static final Pattern SENS = Pattern.compile("fuelLeft|oppLR|oppFB|numBarrels|barrelLR|barrelFB|wallDist");
    static final Pattern RELOP = Pattern.compile("lt|gt|eq");
    static final Pattern OP = Pattern.compile("add|sub|mul|div");
    static final Pattern SEMICOL = Pattern.compile(";");
    static final Pattern LOOP = Pattern.compile("loop");
    static final Pattern IF = Pattern.compile("if");
    static final Pattern WHILE = Pattern.compile("while");
    static final Pattern MOVE = Pattern.compile("move");
    static final Pattern TURNL = Pattern.compile("turnL");
    static final Pattern TURNR = Pattern.compile("turnR");
    static final Pattern TAKEFUEL = Pattern.compile("takeFuel");
    static final Pattern WAIT = Pattern.compile("wait");
    static final Pattern TURNAROUND = Pattern.compile("turnAround");
    static final Pattern SHIELDON = Pattern.compile("shieldOn");
    static final Pattern SHIELDOFF = Pattern.compile("shieldOff");

    //----------------------------------------------------------------

    /**
     * The top of the parser, which is handed a scanner containing
     * the text of the program to parse.
     * Returns the parse tree.
     */
    ProgramNode parse(Scanner s) {
        // Set the delimiter for the scanner.
        s.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");
        // THE PARSER GOES HERE
        // Call the parseProg method for the first grammar rule (PROG) and return the node
        return new PROG().parse(s);
    }

    //----------------------------------------------------------------
    // utility methods for the parser
    // - fail(..) reports a failure and throws exception
    // - require(..) consumes and returns the next token as long as it matches the pattern
    // - requireInt(..) consumes and returns the next token as an int as long as it matches the pattern
    // - checkFor(..) peeks at the next token and only consumes it if it matches the pattern

    /**
     * Report a failure in the parser.
     */
    static void fail(String message, Scanner s) {
        String msg = message + "\n   @ ...";
        for (int i = 0; i < 5 && s.hasNext(); i++) {
            msg += " " + s.next();
        }
        throw new ParserFailureException(msg + "...");
    }

    /**
     * Requires that the next token matches a pattern if it matches, it consumes
     * and returns the token, if not, it throws an exception with an error
     * message
     */
    static String require(String p, String message, Scanner s) {
        if (s.hasNext(p)) {
            return s.next();
        }
        fail(message, s);
        return null;
    }

    static String require(Pattern p, String message, Scanner s) {
        if (s.hasNext(p)) {
            return s.next();
        }
        fail(message, s);
        return null;
    }

    /**
     * Requires that the next token matches a pattern (which should only match a
     * number) if it matches, it consumes and returns the token as an integer
     * if not, it throws an exception with an error message
     */
    static int requireInt(String p, String message, Scanner s) {
        if (s.hasNext(p) && s.hasNextInt()) {
            return s.nextInt();
        }
        fail(message, s);
        return -1;
    }

    static int requireInt(Pattern p, String message, Scanner s) {
        if (s.hasNext(p) && s.hasNextInt()) {
            return s.nextInt();
        }
        fail(message, s);
        return -1;
    }

    /**
     * Checks whether the next token in the scanner matches the specified
     * pattern, if so, consumes the token and return true. Otherwise returns
     * false without consuming anything.
     */
    static boolean checkFor(String p, Scanner s) {
        if (s.hasNext(p)) {
            s.next();
            return true;
        }
        return false;
    }

    static boolean checkFor(Pattern p, Scanner s) {
        if (s.hasNext(p)) {
            s.next();
            return true;
        }
        return false;
    }

    class PROG implements ProgramNode {
        List<ProgramNode> statements = new ArrayList<>();
        public void execute(Robot robot) {
            for (ProgramNode r : statements) {
                r.execute(robot);
            }
        }
        public ProgramNode parse(Scanner s) {
            STMT stmt;
            while (s.hasNext()) {
                stmt = new STMT();
                statements.add(stmt.parse(s));
            }
            return this;
        }

        public String toString() {
            String s = "";
            for (ProgramNode r : statements) {
                s += r.toString();
            }
            return s;
        }
    }
    class STMT implements ProgramNode {
        ProgramNode node = null;
        public ProgramNode parse(Scanner s) {
            if (s.hasNext(ACT)) {
                node = new ACT();
            } else if (s.hasNext(LOOP)) {
                node = new LOOP();
            } else if (s.hasNext(IF)){
                node = new IF();
            } else if (s.hasNext(WHILE)){
                node = new WHILE();
            }
            node.parse(s);
            return this;
        }
        public void execute(Robot robot) { node.execute(robot); }
        public String toString() { return node.toString() + "\n"; }
    }
    class ACT implements ProgramNode {
        ProgramNode node;
        public ProgramNode parse(Scanner s) {
             if (Parser.checkFor(MOVE, s)) {
                node = new moveNode();
                 node.parse(s);
            } else if (Parser.checkFor(TURNL, s)) {
                node = new turnLNode();
            } else if (Parser.checkFor(TURNR, s)) {
                node = new turnRNode();
            } else if (Parser.checkFor(TAKEFUEL, s)) {
                node = new takeFuelNode();
            } else if (Parser.checkFor(WAIT, s)) {
                node = new waitNode();
                node.parse(s);
            } else if (Parser.checkFor(TURNAROUND, s)) {
                 node = new turnAroundNode();
             } else if (Parser.checkFor(SHIELDON, s)) {
                 node = new shieldOnNode();
             } else if (Parser.checkFor(SHIELDOFF, s)) {
                 node = new shieldOffNode();
             } else {
                Parser.fail("Expecting valid action", s);
            }

            Parser.require(Parser.SEMICOL, "Expecting ';'", s);

            return node;
        }

        public void execute(Robot robot) { node.execute(robot); }
        public String toString() { return node.toString() + ";"; }
    }
    class LOOP implements ProgramNode {
        BLOCK node = null;
        public ProgramNode parse(Scanner s) {
            Parser.require(Parser.LOOP, "Expecting 'loop'", s);
            node = new BLOCK();
            node.parse(s);
            return node;
        }
        public void execute(Robot robot) {
            node.execute(robot);
        }

        public String toString() {
            String s = node.toString();
            return String.format("loop %s", s);
        }
    }
    class IF implements ProgramNode {
        private COND Condition = null;
        private BLOCK If = null;
        private BLOCK Else = null;
        private List<ProgramNode> ELSEIF = new ArrayList<>();

        public void execute(Robot robot) {
            if (Condition != null) {
                if(Condition.evaluate(robot)){
                    If.execute(robot);
                    
                } else if(ELSEIF.size() != 0){
                    for(ProgramNode node: ELSEIF){
                        node.execute(robot);
                    }
                } else if(Else != null) {
                    Else.execute(robot);
                }
            }
        }

        public ProgramNode parse(Scanner s) {
            require(Parser.IF, "Expected 'if'", s);
            require(Parser.OPENPAREN, "Expected '('",s);

            Condition = new COND();
            Condition.parse(s);

            require(Parser.CLOSEPAREN, "Expected ')'",s);

            If = new BLOCK();
            If.parse(s);

            while(checkFor("elif", s)){
                ELSEIF.add(new ELSEIF().parse(s));
            }

            if(checkFor("else",s)){
                Else = new BLOCK();
                Else.parse(s);
            }

            return this;
        }

        public String toString() { return "if (" + Condition.toString() + ")" + If.toString(); }

    }
    class ELSEIF implements ProgramNode{
        private COND Condition = null;
        private BLOCK If = null;
        public void execute(Robot robot) {
            if (Condition != null) {
                if (Condition.evaluate(robot)) {
                    If.execute(robot);
                }
            }
        }

        public ProgramNode parse(Scanner s) {
            require(OPENPAREN,"Expecting '('", s);
            Condition = new COND();
            Condition.parse(s);
            require(CLOSEPAREN,"Expecting ')'", s);
            If = new BLOCK();
            If.parse(s);
            return this;
        }

        public String toString() {
            return "elif (" + Condition.toString() + ")" + If.toString();
        }

    }
    class WHILE implements ProgramNode {
        private COND Condition = null;
        private BLOCK While = null;

        public void execute(Robot robot) {
            if (Condition == null) { return; }
                while(true){
                    if(Condition.evaluate(robot)){
                        While.execute(robot);
                    } else {
                        return;
                    }
                }
        }

        public ProgramNode parse(Scanner s) {
            require(WHILE, "Expected 'while'", s);
            require(OPENPAREN, "Expected '('", s);

            Condition = new COND();
            Condition.parse(s);

            require(CLOSEPAREN, "Expected ')'",s);

            While = new BLOCK();
            While.parse(s);

            return this;
        }

        public String toString() { return "while (" + Condition.toString() + ")" + While.toString(); }

    }
    class BLOCK implements ProgramNode {
        List<ProgramNode> blocks = new ArrayList<>();

        public void execute(Robot robot) {
            for (ProgramNode r : blocks) {
                r.execute(robot);
            }
        }

        public ProgramNode parse(Scanner s) {
            STMT node;
            Parser.require(Parser.OPENBRACE, "Expecting '" + Parser.OPENBRACE + "'", s);
            if (s.hasNext() && !s.hasNext(Parser.CLOSEBRACE)) {
                while (!s.hasNext(Parser.CLOSEBRACE)) {
                    node = new STMT();
                    node.parse(s);
                    blocks.add(node);
                }
            } else {
                fail("Expecting at least one or more statements inside loop",s);
            }

            Parser.require(Parser.CLOSEBRACE, "Expecting '" + Parser.CLOSEBRACE + "'", s);
            return this;
        }

        public String toString() {
            String s = "{\n";
            for (ProgramNode r : blocks) {
                s += r.toString();
            }
            return (s + "}\n");
        }
    }
    class EXPR implements IntNode {
        IntNode node = null;
        public int evaluate(Robot robot) {
            return node.evaluate(robot);
        }
        public IntNode parse(Scanner s) {
            if(s.hasNext(NUMPAT)){
                node = new NUM();
            } else if(s.hasNext(SENS)){
                node = new SENS();
            } else if(s.hasNext(OP)){
                node = new OP();
            } else {
                fail("expecting expression",s);
            }

            node.parse(s);
            return node;
        }

        public String toString(){ return node.toString() ;}
    }
    class SENS implements IntNode{
        IntNode node = null;
        public int evaluate(Robot robot) {
            return node.evaluate(robot);
        }
        public IntNode parse(Scanner s) {

            if(s.hasNext("fuelLeft")){
                node = new fuelLeft();
            } else if(s.hasNext("oppLR")){
                node = new oppLR();
            } else if(s.hasNext("oppFB")){
                node = new oppFB();
            } else if(s.hasNext("numBarrels")){
                node = new numBarrels();
            } else if(s.hasNext("barrelLR")){
                node = new barrelLR();
            } else if(s.hasNext("barrelFB")){
                node = new barrelFB();
            } else if(s.hasNext("wallDist")){
                node = new wallDist();
            } else {
                fail("Expecting '" + SENS + "'",s);
            }
            node.parse(s);

            return node;
        }

        public String toString() { return node.toString(); }

    }
    class OP implements IntNode{
        IntNode node = null;
        public int evaluate(Robot robot) {
            return node.evaluate(robot);
        }

        public IntNode parse(Scanner s) {
            if(checkFor("add",s)){
                node = new ADD();
            } else if(checkFor("sub",s)){
                node = new SUB();
            } else if(checkFor("mul",s)){
                node = new MUL();
            } else if(checkFor("div",s)){
                node = new DIV();
            }

            require(OPENPAREN,"Expecting '('",s);

            node.parse(s);
            return node;
        }

        public String toString(){ return node.toString() ;}
    }
    class COND implements ConditionNode{
        ConditionNode node = null;
        public boolean evaluate(Robot robot) {
            return node.evaluate(robot);
        }
        public ConditionNode parse(Scanner s) {
            if(s.hasNext(RELOP)){
                node = new RELOP();
                node.parse(s);
                return node;

            } else if(s.hasNext("and")){
                node = new AND();
            } else if(s.hasNext("or")){
                node = new OR();
            } else if(s.hasNext("not")){
                node = new NOT();
            } else {
                fail("Expecting " + RELOP, s);
            }

            s.next();
            require(OPENPAREN, "Expecting '('",s);

            node.parse(s);
            return node;
        }
        public String toString() { return node.toString(); }
    }
    class RELOP implements ConditionNode{
        ConditionNode node = null;
        public boolean evaluate(Robot robot) {
            return node.evaluate(robot);
        }

        public ConditionNode parse(Scanner s) {
            if(s.hasNext("gt")){
                node = new GT();
            } else if(s.hasNext("lt")){
                node = new LT();
            } else if(s.hasNext("eq")){
                node = new EQ();
            } else {
                fail("Expecting " + RELOP, s);
            }

            s.next();
            require(OPENPAREN, "Expecting '('",s);

            node.parse(s);
            return node;
        }

        public String toString(){ return node.toString();}
    }
    class NUM implements IntNode{
        int num = 0;
        public int evaluate(Robot robot) { return num; }
        public IntNode parse(Scanner s) {
            if(s.hasNext(NUMPAT)){
                num = s.nextInt();
            } else {
                fail("Expecting number", s);
            }
            return this;
        }

        public String toString() { return String.valueOf(num); }

    }


    class AND implements ConditionNode{
        ConditionNode node1 = null;
        ConditionNode node2 = null;
        public boolean evaluate(Robot robot) {
            return node1.evaluate(robot) && node2.evaluate(robot);
        }
        public ConditionNode parse(Scanner s) {

            node1 = new COND();
            node1.parse(s);
            require(",", "Expecting ','",s);
            node2 = new COND();
            node2.parse(s);
            require(CLOSEPAREN, "Expecting ')'",s);

            return this;
        }

        public String toString(){ return "and (" + node1.toString() + "," + node2.toString() + ")";}

    }
    class OR implements ConditionNode{
        ConditionNode node1 = null;
        ConditionNode node2 = null;
        public boolean evaluate(Robot robot) {
            return node1.evaluate(robot) || node2.evaluate(robot);
        }
        public ConditionNode parse(Scanner s) {
            node1 = new COND();
            node1.parse(s);
            require(",", "Expecting ','",s);
            node2 = new COND();
            node2.parse(s);
            require(CLOSEPAREN, "Expecting ')'",s);

            return this;
        }

        public String toString(){ return "or (" + node1.toString() + "," + node2.toString() + ")";}
    }
    class NOT implements ConditionNode{
        ConditionNode node = null;
        public boolean evaluate(Robot robot) {
            return !node.evaluate(robot);
        }
        public ConditionNode parse(Scanner s) {
            node = new COND();
            node.parse(s);
            require(CLOSEPAREN, "Expecting ')'",s);

            return this;
        }

        public String toString(){ return "and (" + node.toString() + ")"; }
    }


    class GT implements ConditionNode{
        public IntNode conditionOne = null;
        public IntNode conditionTwo = null;
        public boolean evaluate(Robot robot) {
            return (conditionOne.evaluate(robot)>conditionTwo.evaluate(robot));
        }
        public ConditionNode parse(Scanner s){
            conditionOne = new EXPR();
            conditionOne.parse(s);
            require(",", "Expecting ','",s);
            conditionTwo = new EXPR();
            conditionTwo.parse(s);

            require(CLOSEPAREN, "Expecting ')'",s);
            return this;
        }

        public String toString() { return "gt(" + conditionOne.toString() + "," + conditionTwo.toString() + ")"; }

    }
    class LT implements ConditionNode{
        public IntNode conditionOne = null;
        public IntNode conditionTwo = null;
        public boolean evaluate(Robot robot) {
            return (conditionOne.evaluate(robot)<conditionTwo.evaluate(robot));
        }
        public ConditionNode parse(Scanner s){
            conditionOne = new EXPR();
            conditionOne.parse(s);
            require(",", "Expecting ','",s);
            conditionTwo = new EXPR();
            conditionTwo.parse(s);

            require(CLOSEPAREN, "Expecting ')'",s);
            return this;
        }

        public String toString() { return "lt(" + conditionOne.toString() + "," + conditionTwo.toString() + ")"; }

    }
    class EQ implements ConditionNode{
        public IntNode conditionOne = null;
        public IntNode conditionTwo = null;
        public boolean evaluate(Robot robot) {
            return (conditionOne.evaluate(robot)==conditionTwo.evaluate(robot));
        }
        public ConditionNode parse(Scanner s){
            conditionOne = new EXPR();
            conditionOne.parse(s);
            require(",", "Expecting ','",s);
            conditionTwo = new EXPR();
            conditionTwo.parse(s);

            require(CLOSEPAREN, "Expecting ')'",s);
            return this;
        }

        public String toString() { return "eq(" + conditionOne.toString() + "," + conditionTwo.toString() + ")"; }

    }

    class ADD implements IntNode{
        IntNode node1 = null;
        IntNode node2 = null;
        public int evaluate(Robot robot) { return node1.evaluate(robot) + node2.evaluate(robot);}
        public IntNode parse(Scanner s) {
            node1 = new EXPR();
            node1.parse(s);
            require(",","Expecting ','",s);
            node2 = new EXPR();
            node2.parse(s);
            require(CLOSEPAREN, "Expecting '('", s);

            return this;
        }

        public String toString(){return "add (" + node1.toString() + "," + node2.toString() + ")" ;}
    }
    class SUB implements IntNode{
        IntNode node1 = null;
        IntNode node2 = null;
        public int evaluate(Robot robot) { return node1.evaluate(robot) - node2.evaluate(robot);}
        public IntNode parse(Scanner s) {
            node1 = new EXPR();
            node1.parse(s);
            require(",","Expecting ','",s);
            node2 = new EXPR();
            node2.parse(s);
            require(CLOSEPAREN, "Expecting '('", s);

            return this;
        }

        public String toString(){return "sub (" + node1.toString() + "," + node2.toString() + ")" ;}
    }
    class MUL implements IntNode{
        IntNode node1 = null;
        IntNode node2 = null;
        public int evaluate(Robot robot) { return node1.evaluate(robot) * node2.evaluate(robot);}
        public IntNode parse(Scanner s) {
            node1 = new EXPR();
            node1.parse(s);
            require(",","Expecting ','",s);
            node2 = new EXPR();
            node2.parse(s);
            require(CLOSEPAREN, "Expecting '('", s);

            return this;
        }

        public String toString(){return "mul (" + node1.toString() + "," + node2.toString() + ")" ;}
    }
    class DIV implements IntNode{
        IntNode node1 = null;
        IntNode node2 = null;
        public int evaluate(Robot robot) { return node1.evaluate(robot) / node2.evaluate(robot);}
        public IntNode parse(Scanner s) {
            node1 = new EXPR();
            node1.parse(s);
            require(",","Expecting ','",s);
            node2 = new EXPR();
            node2.parse(s);
            require(CLOSEPAREN, "Expecting '('", s);

            return this;
        }

        public String toString(){return "div (" + node1.toString() + "," + node2.toString() + ")" ;}
    }


    class fuelLeft implements IntNode{
        public int evaluate(Robot robot) { return robot.getFuel(); }
        public IntNode parse(Scanner s) {
            require("fuelLeft","Expecting 'fuelLeft'",s);
            return this;
        }

        public String toString(){ return "fuelLeft"; }
    }
    class oppLR implements IntNode{
        public int evaluate(Robot robot) { return robot.getOpponentLR(); }
        public IntNode parse(Scanner s) {
            require("oppLR","Expecting 'oppLR'",s);
            return this;
        }

        public String toString(){ return "oppLR"; }

    }
    class oppFB implements IntNode{
        public int evaluate(Robot robot) { return robot.getOpponentFB(); }
        public IntNode parse(Scanner s) {
            require("oppFB","Expecting 'oppFB'",s);
            return this;
        }

        public String toString(){ return "oppFB"; }

    }
    class numBarrels implements IntNode{
        public int evaluate(Robot robot) { return robot.numBarrels(); }
        public IntNode parse(Scanner s) {
            require("numBarrels","Expecting 'numBarrels'",s);
            return this;
        }

        public String toString(){ return "numBarrels"; }

    }
    class barrelLR implements IntNode{
        public int evaluate(Robot robot) { return robot.getClosestBarrelLR(); }
        public IntNode parse(Scanner s) {
            require("barrelLR","Expecting 'barrelLR'",s);
            return this;
        }

        public String toString(){ return "barrelLR"; }

    }
    class barrelFB implements IntNode{
        public int evaluate(Robot robot) { return robot.getClosestBarrelFB(); }
        public IntNode parse(Scanner s) {
            require("barrelFB","Expecting 'barrelFB'",s);
            return this;
        }

        public String toString(){ return "barrelFB"; }

    }
    class wallDist implements IntNode{
        public int evaluate(Robot robot) { return robot.getDistanceToWall(); }
        public IntNode parse(Scanner s) {
            require("wallDist","Expecting 'wallDist'",s);
            return this;
        }

        public String toString(){ return "wallDist"; }

    }
    class moveNode implements ProgramNode {
        IntNode node = null;
        public void execute(Robot robot) {
            if(node != null){
                for(int i = 0; i < node.evaluate(robot); i++){
                    robot.move();
                }
            } else {
                robot.move();
            }
        }
        public ProgramNode parse(Scanner s) {
            if(checkFor(OPENPAREN, s)){
                node = new EXPR();
                node.parse(s);
                require(CLOSEPAREN, "Expecting ')'", s);
            }
            return this;
        }
        public String toString() {
            String move = "move";

            if(node != null){
                move += ("(" + node.toString() + ")");
            }
            return move;
        }
    }
    class turnLNode implements ProgramNode {
        public void execute(Robot robot) { robot.turnLeft(); }
        public ProgramNode parse(Scanner s) { return null; }
        public String toString() { return "turnL"; }
    }
    class turnRNode implements ProgramNode {
        public void execute(Robot robot) { robot.turnRight(); }
        public ProgramNode parse(Scanner s) { return null; }
        public String toString() { return "turnR"; }
    }
    class takeFuelNode implements ProgramNode {
        public void execute(Robot robot) { robot.takeFuel(); }
        public ProgramNode parse(Scanner s) { return null; }

        public String toString() { return "takeFuel"; }
    }
    class waitNode implements ProgramNode {
        IntNode node = null;
        public void execute(Robot robot) {
            if(node != null){
                for(int i = 0; i < node.evaluate(robot); i++){ robot.idleWait();  }
            } else {
                robot.idleWait();
            }
        }
        public ProgramNode parse(Scanner s) {
            if(checkFor(OPENPAREN, s)){
                node = new EXPR();
                node.parse(s);
                require(CLOSEPAREN, "Expecting ')'", s);
            }
            return this;
        }
        public String toString() {
            String wait = "wait";

            if(node != null){
                wait += ("(" + node.toString() + ")");
            }
            return wait;
        }
    }
    class turnAroundNode implements ProgramNode {
        public void execute(Robot robot) { robot.turnAround(); }
        public ProgramNode parse(Scanner s) { return null; }
        public String toString() {
            return "turnAround";
        }
    }
    class shieldOnNode implements ProgramNode {
        public void execute(Robot robot) { robot.setShield(true); }
        public ProgramNode parse(Scanner s) { return null; }
        public String toString() {
            return "shieldOn";
        }
    }
    class shieldOffNode implements ProgramNode {
        public void execute(Robot robot) { robot.setShield(false); }
        public ProgramNode parse(Scanner s) { return null; }
        public String toString() {
            return "shieldOff";
        }
    }

}