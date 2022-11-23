import java.util.*;


public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;

    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }


    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);

        Declarations d = declarations(); //declarations

        Block b = programStatements(); //statement

        // student exercise
        match(TokenType.RightBrace);
        return new Program(d, b);  // student exercise
    }

    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations ds = new Declarations();
        while(isType())
            declaration(ds);
        return ds;  // student exercise
    }

    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        Type t = type();
        Variable v = new Variable(match(TokenType.Identifier)); //type과 변수 매칭
        Declaration d = new Declaration(v, t);
        ds.add(d); //arraylist 로 저장

        //{ , Identifier } ',' 를 통해 여러 변수를 선언했을 때
        while(token.type().equals(TokenType.Comma)) {
            token = lexer.next(); //다음 토큰
            v = new Variable(match(TokenType.Identifier));
            d = new Declaration(v, t);
            ds.add(d);
        }
        match(TokenType.Semicolon); //선언문 종료, 세미콜론 매치
        // student exercise
    }

    private Type type () {
        // Type  -->  int | bool | float | char 
        Type t = null;
        // student exercise
        if (token.type().equals(TokenType.Int))
            t = Type.INT;
        else if (token.type().equals(TokenType.Bool))
            t = Type.BOOL;
        else if (token.type().equals(TokenType.Float))
            t = Type.FLOAT;
        else if (token.type().equals(TokenType.Char))
            t = Type.CHAR;
        else error("Error in type()");
        token = lexer.next();
        return t;
    }

    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = null;

        if (token.type().equals(TokenType.Semicolon))
            s = new Skip(); // ";"일 경우 Skip
        else if (token.type().equals(TokenType.LeftBrace))
            s = statements(); // "{" 일 경우 Block
        else if (token.type().equals(TokenType.If))
            s = ifStatement(); // "if" 일 경우 IfStatement
        else if (token.type().equals(TokenType.While))
            s = whileStatement(); // "while"일 경우 WhileStatement
        else if (token.type().equals(TokenType.Identifier))
            s = assignment(); // identifier 일 경우 Assignment
        else error("Error in statement()");
        // student exercise
        return s;
    }

    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();

        match(TokenType.LeftBrace);
        while(isStatement())
            b.members.add(statement());
        match(TokenType.RightBrace);
        // student exercise
        return b;
    }

    private Block programStatements () {
        // Block -->  Statements
        Block b = new Block();

        while(isStatement())
            b.members.add(statement());
        return b;
    }

    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;

        Variable target = new Variable(match(TokenType.Identifier)); //target 변수 매치
        match(TokenType.Assign); //"=" 매치
        Expression source = expression(); //expression()을 통해 매치
        match(TokenType.Semicolon); //";"매치

        return new Assignment(target, source);
        // student exercise
    }

    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        Conditional c;
        match(TokenType.If);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        Statement s = statement();
        if (token.type().equals(TokenType.Else)) {
            Statement elseState = statement();
            c = new Conditional(e, s, elseState);
        }
        else c = new Conditional(e, s);
        return c;
        // student exercise
    }

    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        Statement s = statement();
        return new Loop(e, s);  // student exercise
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression c = conjunction();
        while (token.type().equals(TokenType.And)) {
            Operator op = new Operator(match(token.type()));
            Expression e = expression();
            c = new Binary(op, c, e);
        }
        return c;  // student exercise
    }

    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression eq = equality();
        while (token.type().equals(TokenType.And)) {
            Operator op = new Operator(match(token.type()));
            Expression c = conjunction();
            eq = new Binary(op, eq, c);
        }
        return eq;  // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression r = relation();
        while (isEqualityOp()) {
            Operator op = new Operator(match(token.type()));
            Expression r2 = relation();
            r = new Binary(op, r, r2);
        }
        return r;  // student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition]
        Expression a = addition();
        while (isRelationalOp()) {
            Operator op = new Operator(match(token.type()));
            Expression a2 = addition();
            a = new Binary(op, a, a2);
        }
        return a;  // student exercise
    }

    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();

        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        Value v = null;
        String sv = token.value();
        if (token.type().equals(TokenType.IntLiteral)) {
            v = new IntValue(Integer.parseInt(sv));
            token = lexer.next();
        } else if (token.type().equals(TokenType.FloatLiteral)) {
            v = new FloatValue(Float.parseFloat(sv));
            token = lexer.next();
        } else if (token.type().equals(TokenType.CharLiteral)) {
            v = new CharValue(sv.charAt(0));
            token = lexer.next();
        } else if (token.type().equals(TokenType.True)) {
            v = new BoolValue(true);
            token = lexer.next();
        } else if (token.type().equals(TokenType.False)) {
            v = new BoolValue(false);
            token = lexer.next();
        } else error("Error In Literal");

        return v;  // student exercise
    }

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }

    private boolean isStatement() {
        return token.type().equals(TokenType.Semicolon)
                || token.type().equals(TokenType.LeftBrace)
                || token.type().equals(TokenType.If)
                || token.type().equals(TokenType.While)
                || token.type().equals(TokenType.Identifier);
    }

    public static void main(String args[]) {
        System.out.println("Begin parsing... " + args[0] + "\n");

        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
