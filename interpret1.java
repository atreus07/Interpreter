import java.util.*;


enum TokenType {
    PLUS, MINUS, MUL, DIV, MOD,
    LPAREN, RPAREN, APOS, INTEGER, REAL, EOF, STR, TRUE, FALSE,
    EQUAL, NOT, GRET, LEST, GRE, LESE, ASSIGN, SEMI, ID, COMMA, DOT,
    FOR, WHILE, IF, ELSE, ELSIF, END
}

class Token {
    TokenType type;
    Object value;

    public Token(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }
    
    
    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Token(" + type + ", " + value + ")";
    }
}

class Lexer {
    private String text;
    private int pos;
    private int line;
    private char currentChar;

    public Lexer(String text) {
        this.text = text;
        this.pos = 0;
        this.line = 1;
        this.currentChar = text.charAt(pos);
    }

    private void error() {
        throw new RuntimeException("Invalid syntax");
    }

    private void advance() {
        pos++;
        if (pos > text.length() - 1) {
            currentChar = '\0'; //EOF
        } else {
            currentChar = text.charAt(pos);
        }
    }

    private char peek(int n) {
        int peekPos = pos + n;
        return peekPos > text.length() - 1 ? '\0' : text.charAt(peekPos);
    }

    private void skipWhiteSpace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            if (currentChar == '\n') {
                line++;
            }
            advance();
        }
    }

    private void skipComment() {
        while (currentChar != '\n' && currentChar != '\0') {
            advance();
        }
    }

    private Token number() {
        StringBuilder result = new StringBuilder();
        while (currentChar != '\0' && Character.isDigit(currentChar)) {
            result.append(currentChar);
            advance();
        }

        if (currentChar == '.') {
            result.append(currentChar);
            advance();

            while (currentChar != '\0' && Character.isDigit(currentChar)) {
                result.append(currentChar);
                advance();
            }

            return new Token(TokenType.REAL, Double.parseDouble(result.toString()));
        } else {
            return new Token(TokenType.INTEGER, Integer.parseInt(result.toString()));
        }
    }

    private Token string() {
        StringBuilder result = new StringBuilder();
        advance(); // Skip opening double quote
        while (currentChar != '\0' && currentChar != '"') {
            result.append(currentChar);
            advance();
        }
        advance(); // Skip closing double quote

        return new Token(TokenType.STR, result.toString());
    }

    private Token id() {
        StringBuilder result = new StringBuilder();
        while (currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            result.append(currentChar);
            advance();
        }

        if ("if".equals(result.toString())) {
            return new Token(TokenType.IF, "if");
        } else if ("elsif".equals(result.toString())) {
            return new Token(TokenType.ELSIF, "elsif");
        } else if ("else".equals(result.toString())) {
            return new Token(TokenType.ELSE, "else");
        } else if ("while".equals(result.toString())) {
            return new Token(TokenType.WHILE, "while");
        } else if ("end".equals(result.toString())) {
            return new Token(TokenType.END, "end");
        } else if ("true".equals(result.toString())) {
            return new Token(TokenType.TRUE, true);
        } else if ("false".equals(result.toString())) {
            return new Token(TokenType.FALSE, false);
        } else {
            return new Token(TokenType.ID, result.toString());
        }
    }

    public Token getNextToken() {
        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhiteSpace();
                continue;
            }

            if (currentChar == '#') {
                advance();
                skipComment();
                continue;
            }

            if (Character.isAlphabetic(currentChar)) {
                return id();
            }

            if (Character.isDigit(currentChar)) {
                return number();
            }

            switch (currentChar) {
                case '=':
                    if (peek(1) != '=') {
                        advance();
                        return new Token(TokenType.ASSIGN, '=');
                    } else {
                        advance();
                        advance();
                        return new Token(TokenType.EQUAL, "==");
                    }

                case '!':
                    if (peek(1) == '=') {
                        advance();
                        advance();
                        return new Token(TokenType.NOT, "!=");
                    } else {
                        error();
                    }

                case '>':
                    if (peek(1) != '=') {
                        advance();
                        return new Token(TokenType.GRET, '>');
                    } else {
                        advance();
                        advance();
                        return new Token(TokenType.GRE, ">=");
                    }

                case '<':
                    if (peek(1) == '=') {
                        advance();
                        advance();
                        return new Token(TokenType.LESE, "<=");
                    } else {
                        advance();
                        return new Token(TokenType.LEST, '<');
                    }

                case '+':
                    advance();
                    return new Token(TokenType.PLUS, '+');

                case '-':
                    advance();
                    return new Token(TokenType.MINUS, '-');

                case '*':
                    advance();
                    return new Token(TokenType.MUL, '*');

                case '/':
                    advance();
                    return new Token(TokenType.DIV, '/');

                case '%':
                    advance();
                    return new Token(TokenType.MOD, '%');

                case ';':
                    advance();
                    return new Token(TokenType.SEMI, ';');

                case ',':
                    advance();
                    return new Token(TokenType.COMMA, ',');

                case '(':
                    advance();
                    return new Token(TokenType.LPAREN, '(');

                case ')':
                    advance();
                    return new Token(TokenType.RPAREN, ')');

                case '.':
                    advance();
                    return new Token(TokenType.DOT, '.');

                case '"':
                    advance();
                    return new Token(TokenType.APOS, '"');

                default:
                    error();
            }
        }

        return new Token(TokenType.EOF, null);
    }
}



abstract class AST {
    @Override
    public String toString() {
        return "AST{}";
    }
}


// AST Node classes
class BinOp extends AST {
    AST left;
    Token token;
    AST right;

    public BinOp(AST left, Token op, AST right) {
        this.left = left;
        this.token = op;
        this.right = right;
    }
    
   
    
    public AST getLeft() {
        return left;
    }

    public AST getRight() {
        return right;
    }
    
    public Token getOp() {
        return token;
    }
    
    @Override
    public String toString() {
        return "BinOp{" +
                "left=" + left +
                ", op=" + token +
                ", right=" + right +
                '}';
    }
}

class Num extends AST {
    Token token;
    Object value;

    public Num(Token token) {
        this.token = token;
        this.value = token.value;
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "Num{" +
                "value=" + value +
                '}';
    }
}

class UnaryOp extends AST {
    Token op;
    AST expr;

    public UnaryOp(Token op, AST expr) {
        this.op = op;
        this.expr = expr;
    }
    
    public Token getOp() {
        return op;
    }

    public AST getExpr() {
        return expr;
    }
    
    
    @Override
    public String toString() {
        return "UnaryOp{" +
                "op=" + op +
                ", expr=" + expr +
                '}';
    }
}

class Compound extends AST {
    List<AST> children = new ArrayList<>();
    
    public List<AST> getChildren() {
        return children;
    }
    
     @Override
    public String toString() {
        return "Compound{" +
                "children=" + children +
                '}';
    }
}

class Assign extends AST {
    AST left;
    Token op;
    AST right;

    public Assign(AST left, Token op, AST right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
    
    public AST getLeft() {
        return left;
    }
    
    @Override
    public String toString() {
        return "Assign{" +
                "left=" + left +
                ", op=" + op +
                ", right=" + right +
                '}';
    }
}

class Var extends AST {
    Token token;
    Object value;

    public Var(Token token) {
        this.token = token;
        this.value = token.value;
    }
    
    
    
    @Override
    public String toString() {
        return "Var{" +
                "value='" + value + '\'' +
                '}';
    }
}

class If extends AST {
    AST condition;
    List<AST> body;
    List<AST> rest;

    public If(AST condition, List<AST> body, List<AST> rest) {
        this.condition = condition;
        this.body = body;
        this.rest = rest;
    }
    
    @Override
    public String toString() {
        return "If{" +
                "condition=" + condition +
                ", body=" + body +
                ", rest=" + rest +
                '}';
    }
}

class Else extends AST {
    List<AST> body;

    public Else(List<AST> body) {
        this.body = body;
    }
    
    @Override
    public String toString() {
        return "Else{" +
                "body=" + body +
                '}';
    }
}

class While extends AST {
    AST condition;
    List<AST> body;

    public While(AST condition, List<AST> body) {
        this.condition = condition;
        this.body = body;
    }
    
    @Override
    public String toString() {
        return "While{" +
                "condition=" + condition +
                ", body=" + body +
                '}';
    }
}

class NoOp extends AST {
    @Override
    public String toString() {
        return "NoOp{}";
    }
}


// Parser class
class Parser {
    Lexer lexer;
    Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = this.lexer.getNextToken();
    }

    private void error() {
        throw new RuntimeException("Invalid syntax");
    }

    private void eat(TokenType tokenType) {
        if (currentToken.type == tokenType) {
            currentToken = lexer.getNextToken();
        } else {
            error();
        }
    }

    public AST parse() {
        AST node = program();
        while (currentToken.type != TokenType.EOF) {
            error();
        }
        return node;
    }

    private AST program() {
        return compoundStatement();
    }

    private AST compoundStatement() {
        List<AST> nodes = statementList();
        Compound root = new Compound();
        root.children.addAll(nodes);
        return root;
    }

    private List<AST> statementList() {
        List<AST> results = new ArrayList<>();
        results.add(statement());

        while (currentToken.type != TokenType.EOF) {
            results.add(statement());
        }

        return results;
    }

    private AST statement() {
    if (currentToken.type == TokenType.ID) {
        return assignmentStatement();
    } 
    else if (currentToken.type == TokenType.IF) {
        return ifStatement();
    } 
    else if (currentToken.type == TokenType.ELSIF) {
        List<AST> elsifResult = elsifStatement();
        return elsifResult.get(0); 
    }
    else if (currentToken.type == TokenType.ELSE) {
        List<AST> elseResult = elseStatement();
        return elseResult.get(0); 
    } 
    else if (currentToken.type == TokenType.WHILE) {
        return whileStatement();
    } 
    else {
        return conditionalStatement();
    }
}

    private AST assignmentStatement() {
        AST left = variable();
        Token token = currentToken;
        eat(TokenType.ASSIGN);
        AST right = expr();
        return new Assign(left, token, right);
    }

    private AST ifStatement() {
        eat(TokenType.IF);
        AST condition = conditionalStatement();
        List<AST> body = new ArrayList<>();
        List<AST> rest = new ArrayList<>();

        while (currentToken.type != TokenType.ELSIF && currentToken.type != TokenType.ELSE && currentToken.type != TokenType.END) {
            body.add(statement());
        }

        if (currentToken.type == TokenType.ELSIF) {
            rest = elsifStatement();
        }

        if (currentToken.type == TokenType.ELSE) {
            rest = elseStatement();
        }

        return new If(condition, body, rest);
    }

    private AST whileStatement() {
        eat(TokenType.WHILE);
        AST condition = conditionalStatement();
        List<AST> body = new ArrayList<>();

        while (currentToken.type != TokenType.END) {
            body.add(statement());
        }

        eat(TokenType.END);
        return new While(condition, body);
    }

    private AST variable() {
        Token token = currentToken;
        eat(TokenType.ID);
        return new Var(token);
    }

    private List<AST> elsifStatement() {
    eat(TokenType.ELSIF);
    AST elsifCondition = conditionalStatement();
    List<AST> elsifBody = new ArrayList<>();
    List<AST> rest = new ArrayList<>();

    while (currentToken.type != TokenType.ELSE) {
        elsifBody.add(statement());
    }

    if (currentToken.type == TokenType.ELSE) {
        rest.addAll(elseStatement());
    }

    List<AST> result = new ArrayList<>();
    result.add(new If(elsifCondition, elsifBody, rest));
    return result;
}

private List<AST> elseStatement() {
    eat(TokenType.ELSE);
    List<AST> elseBody = new ArrayList<>();

    while (currentToken.type != TokenType.END) {
        elseBody.add(statement());
    }

    eat(TokenType.END);
    return elseBody;
}

    private AST conditionalStatement() {
        AST node = expr();

        while (currentToken.type == TokenType.EQUAL || currentToken.type == TokenType.GRE ||
               currentToken.type == TokenType.NOT || currentToken.type == TokenType.GRET ||
               currentToken.type == TokenType.LESE || currentToken.type == TokenType.LEST) {
            Token token = currentToken;
            if (token.type == TokenType.EQUAL) {
                eat(TokenType.EQUAL);
            } else if (token.type == TokenType.GRE) {
                eat(TokenType.GRE);
            } else if (token.type == TokenType.NOT) {
                eat(TokenType.NOT);
            } else if (token.type == TokenType.GRET) {
                eat(TokenType.GRET);
            } else if (token.type == TokenType.LESE) {
                eat(TokenType.LESE);
            } else if (token.type == TokenType.LEST) {
                eat(TokenType.LEST);
            }
            node = new BinOp(node, token, expr());
        }

        return node;
    }

    private AST expr() {
        AST node = term();

        while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            Token token = currentToken;
            if (token.type == TokenType.PLUS) {
                eat(TokenType.PLUS);
            } else if (token.type == TokenType.MINUS) {
                eat(TokenType.MINUS);
            }
            node = new BinOp(node, token, term());
        }

        return node;
    }

    private AST term() {
        AST node = factor();

        while (currentToken.type == TokenType.MUL || currentToken.type == TokenType.DIV ||
               currentToken.type == TokenType.MOD || currentToken.type == TokenType.EQUAL ||
               currentToken.type == TokenType.NOT || currentToken.type == TokenType.LEST ||
               currentToken.type == TokenType.GRET || currentToken.type == TokenType.LESE ||
               currentToken.type == TokenType.GRE) {
            Token token = currentToken;
            if (token.type == TokenType.MUL) {
                eat(TokenType.MUL);
            } else if (token.type == TokenType.DIV) {
                eat(TokenType.DIV);
            } else if (token.type == TokenType.MOD) {
                eat(TokenType.MOD);
            } else if (token.type == TokenType.EQUAL) {
                eat(TokenType.EQUAL);
            } else if (token.type == TokenType.NOT) {
                eat(TokenType.NOT);
            } else if (token.type == TokenType.LEST) {
                eat(TokenType.LEST);
            } else if (token.type == TokenType.GRET) {
                eat(TokenType.GRET);
            } else if (token.type == TokenType.LESE) {
                eat(TokenType.LESE);
            } else if (token.type == TokenType.GRE) {
                eat(TokenType.GRE);
            }
            node = new BinOp(node, token, factor());
        }

        return node;
    }

    private AST factor() {
        Token token = currentToken;
        if (token.type == TokenType.INTEGER) {
            eat(TokenType.INTEGER);
            return new Num(token);
        } else if (token.type == TokenType.REAL) {
            eat(TokenType.REAL);
            return new Num(token);
        } else if (token.type == TokenType.PLUS) {
            eat(TokenType.PLUS);
            return new UnaryOp(token, factor());
        } else if (token.type == TokenType.MINUS) {
            eat(TokenType.MINUS);
            return new UnaryOp(token, factor());
        } else if (token.type == TokenType.LPAREN) {
            eat(TokenType.LPAREN);
            AST result = expr();
            eat(TokenType.RPAREN);
            return result;
        } else if (token.type == TokenType.ID) {
            eat(TokenType.ID);
            return new Var(token);
        } else if (token.type == TokenType.STR) {
            eat(TokenType.STR);
            return new Num(token);
        } else {
            return variable();
        }
    }
}


// NodeVisitor class
class NodeVisitor {
    public Object visit(AST node) {
        String methodName = "visit_" + node.getClass().getSimpleName();
        try {
            return this.getClass().getMethod(methodName, node.getClass()).invoke(this, node);
        } catch (Exception e) {
            return genericVisit(node);
        }
    }

    public Object genericVisit(AST node) {
        throw new RuntimeException("No visit_" + node.getClass().getSimpleName() + " method");
    }
}

class Interpreter extends NodeVisitor {

    private static final int MAX_VARIABLES = 26;  // Assuming variables are single uppercase letters (A-Z)
    private double[] GLOBAL_MEMORY = new double[MAX_VARIABLES];
    private AST tree;

    public Interpreter(AST tree) {
        this.tree = tree;
    }

    public Double interpret() {
        if (tree == null) {
            return null;
        }
        return (Double) visit(tree);
    }

    
    public Double visit_BinOp(BinOp node) {
        double leftValue = (Double) visit(node.getLeft());
        double rightValue = (Double) visit(node.getRight());

        switch (node.getOp().getType()) {
            case PLUS:
                return leftValue + rightValue;
            case MINUS:
                return leftValue - rightValue;
            case MUL:
                return leftValue * rightValue;
            case DIV:
                return leftValue / rightValue;
            case MOD:
                return leftValue % rightValue;
            case EQUAL:
                return leftValue == rightValue ? 1.0 : 0.0;
            case NOT:
                return leftValue != rightValue ? 1.0 : 0.0;
            case GRE:
                return leftValue >= rightValue ? 1.0 : 0.0;
            case LESE:
                return leftValue <= rightValue ? 1.0 : 0.0;
            case GRET:
                return leftValue > rightValue ? 1.0 : 0.0;
            case LEST:
                return leftValue < rightValue ? 1.0 : 0.0;
            default:
                throw new IllegalArgumentException("Invalid operation type: " + node.getOp().getType());
        }
    }

    
    public Double visit_Num(Num node) {
        return (Double) node.getValue();
    }

   
    public Double visit_UnaryOp(UnaryOp node) {
        TokenType opType = node.getOp().getType();
        double exprValue = (Double) visit(node.getExpr());

        switch (opType) {
            case PLUS:
                return exprValue;
            case MINUS:
                return -exprValue;
            default:
                throw new IllegalArgumentException("Invalid unary operation type: " + opType);
        }
    }

    
    public void visit_Compound(Compound node) {
        for (AST child : node.getChildren()) {
            visit(child);
        }
    }

    
    public void visit_Assign(Assign node) {
    Var leftVar = (Var) node.getLeft();
    String varName = leftVar.getValue();

    int varIndex = varName.charAt(0) - 'A';

    double varValue = visit(node.getRight());

    GLOBAL_MEMORY[varIndex] = varValue;
}

    @Override
    public Double visit_Var(Var node) {
        int varIndex = node.getValue().charAt(0) - 'A';
        return GLOBAL_MEMORY[varIndex];
    }

    @Override
    public void visit_If(If node) {
        if (visit(node.getCondition()) != 0.0) {
            visit(node.getBody());
        } else {
            visit(node.getRest());
        }
    }

    @Override
    public void visit_Else(Else node) {
        visit(node.getBody());
    }

    @Override
    public void visit_While(While node) {
        while (visit(node.getCondition()) != 0.0) {
            visit(node.getBody());
        }
    }

    @Override
    public void visit_list(List<AST> nodes) {
        for (AST node : nodes) {
            visit(node);
        }
    }

    @Override
    public void visit_NoOp(NoOp node) {
        // Do nothing
    }
}


public class Main {
    public static void main(String[] args) {
        // Example code to parse and interpret
        String input = "A = 10 * 2 + 5; B = A - 7; IF B > 0 THEN C = B * 2; ELSE C = B / 2; END";

        // Create a lexer and parser
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);

        // Parse the input and get the abstract syntax tree (AST)
        AST tree = parser.parse();

        // Create an interpreter and interpret the AST
        Interpreter interpreter = new Interpreter(tree);
        interpreter.interpret();

        
    }
}
