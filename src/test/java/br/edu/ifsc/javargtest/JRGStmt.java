package br.edu.ifsc.javargtest;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;

public class JRGStmt {
    private ClassTable mCT;
    
    private static final int FUEL_START = 10;
    
    private int mFuel;
    
    private JRGBase mBase;
    
    private List<String> mValidNames;
    
    private JRGCore mCore;
    
    private JRGOperator mOperator; 
    
    public static final int MAX_STMT = 5;
    //WHILE_STMT = 2,
    //VAR_DECL_STMT = 3
    public static final int IF_STMT = 1, FOR_STMT = 2 , VAR_DECLARATION_STMT = 3;
    
    public JRGStmt(ClassTable ct , JRGBase base, JRGCore core) {
        mCT = ct;
                
        mBase = base;
        
        mCore = core;
        
        mOperator = new JRGOperator(mCT , mBase , mCore);
     
        // removido o I e D por conta de conflito com if
        List<String> tempNames = Arrays.asList("a","b","c","e","f","g","h","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z");
        mValidNames = new LinkedList<>(tempNames);
        
        for(String l1 : tempNames) {
            for(String l2 : tempNames) {
                String letra = "";
                letra = l1 + l2;
                mValidNames.add(letra);
            }
        }
        
        mFuel = FUEL_START;
        
    }
    
    //ExpressionStmt
    /*@Provide
    public Arbitrary<VariableDeclarationExpr> genVarDecl(Map<String,String> ctx) throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclaration::inicio");       
        
        Arbitrary<PrimitiveType> pt = mBase.primitiveTypes().map(
                t -> new PrimitiveType(t));
        
        Arbitrary<Type> t = Arbitraries.oneOf(mBase.classOrInterfaceTypes(), pt);
       
        String v = Arbitraries.of(mValidNames).sample();
        
        Type tp = t.sample();
        
        ctx.put(v, tp.asString());
        
        mValidNames.remove(v);     
       
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclaration::fim"); 
        
        //return t.map(tp -> new VariableDeclarationExpr(tp, v));
        return Arbitraries.just(new VariableDeclarationExpr(tp, v));
    }*/
    
   @Provide
    public  Arbitrary<Statement> genStatement(Map<String,String> ctx) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatement::inicio");         
    
        try {
            if(mFuel > 0) {
                mFuel--;
                //IF_STMT, WHILE_STMT,FOR_STMT,
                Arbitrary<Integer> s = Arbitraries.of(IF_STMT, VAR_DECLARATION_STMT);
                switch(s.sample()){
                    case IF_STMT: return Arbitraries.oneOf(genIfStmt(ctx));
                    //case WHILE_STMT: return Arbitraries.oneOf(genWhileStmt(ctx));
                    //case VAR_DECL_STMT: return Arbitraries.oneOf(genVarDeclStmt(ctx));
                    case VAR_DECLARATION_STMT: return Arbitraries.oneOf(genVarDeclarationStmt(ctx));
                    //case FOR_STMT: return Arbitraries.oneOf(genForStmt(ctx));
                }      
            }else {
                Arbitrary<Integer> s = Arbitraries.of(VAR_DECLARATION_STMT);
                switch(s.sample()){
                    case VAR_DECLARATION_STMT: return Arbitraries.oneOf(genVarDeclarationStmt(ctx));
                    //case VAR_DECL_STMT: return Arbitraries.oneOf(genVarDeclStmt(ctx));
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JRGStmt.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatement::fim");
        
        return  null;
    } 
    
    @Provide
    public Arbitrary<NodeList<Statement>> genStatementList(Map<String,String> ctx) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatementList::inicio");       
        
        int n = Arbitraries.integers().between(1, MAX_STMT).sample();
        //List<Statement> exs =  new ArrayList<>();
        
        NodeList<Statement> nodes = new NodeList<>();
        for(int i = 0; i < n; i++) {
            nodes.add(genStatement(ctx).sample());
        }
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genStatementList::fim");
        
        return Arbitraries.just(nodes);
    }      
    
    @Provide
    public Arbitrary<BlockStmt> genBlockStmt(Map<String,String> ctx) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genBlockStmt::inicio");       
        
        Arbitrary<NodeList<Statement>> l = genStatementList(ctx);
        
        BlockStmt b = new BlockStmt(l.sample());
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genBlockStmt::fim");
        
        return Arbitraries.just(b);
    }

    //ExpressionStmt
    @Provide
    public Arbitrary<VariableDeclarationExpr> genVarDeclAssign(Map<String,String> ctx) throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclarator::inicio");
        Arbitrary<PrimitiveType> pt = mBase.primitiveTypes().map(
                t -> new PrimitiveType(t));
        
        Arbitrary<Type> t = Arbitraries.oneOf(mBase.classOrInterfaceTypes(), pt);
        
        Type tp = t.sample();
        
        Arbitrary<Expression> e = mCore.genExpression(ctx, tp);       
       
        String v = Arbitraries.of(mValidNames).sample();
        
        ctx.put(v, tp.asString());
        
        mValidNames.remove(v);
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclarator::fim");
        
        return e.map(obj -> new VariableDeclarationExpr(
                new VariableDeclarator(tp, v, obj)));
    }  
    
     //
    @Provide
    public Arbitrary<VariableDeclarationExpr> genVarAssingStmt(Map<String,String> ctx) throws ClassNotFoundException{
        String key = Arbitraries.of(ctx.keySet()).sample();
        
        String value = ctx.get(key);
        
        Type tp = ReflectParserTranslator
                .reflectToParserType(value);
        
        Arbitrary<Expression> e = mCore.genExpression(ctx, tp);
        
        return e.map(obj -> new VariableDeclarationExpr(
                new VariableDeclarator(tp, key, obj)));
    }
    
    /*@Provide
    public Arbitrary<AssignExpr> genTypeAssingStmt(Map<String,String> ctx) throws ClassNotFoundException{
        String key = Arbitraries.of(ctx.keySet()).sample();
        
        String value = ctx.get(key);
        
        Type tp = ReflectParserTranslator
                .reflectToParserType(value);
        
        Arbitrary<Expression> e = mCore.genExpression(ctx, tp);
        
        return e.map(obj -> new AssignExpr(e.sample(),obj, AssignExpr.Operator.ASSIGN
                ));
    }*/
    
    
    @Provide
    public Arbitrary<IfStmt> genIfStmt(Map<String,String> ctx) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genIfStmt::inicio");       
        
        Map<String, String> newCtxIf = new HashMap<String, String>(ctx);
        Map<String, String> newCtxElse = new HashMap<String, String>(ctx);
        
        Arbitrary<Expression> e = mCore.genExpressionOperator(ctx, PrimitiveType.booleanType());       
       
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genIfStmt::fim"); 
     
        return e.map(exp -> new IfStmt(exp, genBlockStmt(newCtxIf).sample(), genBlockStmt(newCtxElse).sample()));
    }
 
    @Provide
    public Arbitrary<WhileStmt> genWhileStmt(Map<String,String> ctx) {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genWhileStmt::inicio");       
                       
        Map<String, String> newCtx = new HashMap<String, String>(ctx);
        
        Arbitrary<Expression> e = mCore.genExpression(newCtx, PrimitiveType.booleanType());
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genWhileStmt::fim"); 
        return e.map(exp -> new WhileStmt(exp.asVariableDeclarationExpr(),genBlockStmt(newCtx).sample()));
    }   
    
    @Provide
    public Arbitrary<ExpressionStmt> genExpressionStmt(Map<String,String> ctx){
        //@TODO: Sortear o tipo aleatoriamente e passar para genExpression
        Arbitrary<PrimitiveType.Primitive> t = mBase.primitiveTypes();
         
        Arbitrary<Expression> e = mCore.genExpression(ctx, ReflectParserTranslator
                .reflectToParserType(t.sample().toString()));
        
        return e.map(exp -> new ExpressionStmt(exp));
    }
    
    @Provide
    public Arbitrary<ExpressionStmt> genVarDeclarationStmt(Map<String,String> ctx) throws ClassNotFoundException{
         
        Arbitrary<VariableDeclarationExpr> e = genVarDeclAssign(ctx);
        
        return e.map(exp -> new ExpressionStmt(exp));
    }
    
    /*@Provide
    public Arbitrary<ExpressionStmt> genVarDeclStmt(Map<String,String> ctx) throws ClassNotFoundException{
         
        Arbitrary<VariableDeclarationExpr> e = genVarDecl(ctx);
        
        return e.map(exp -> new ExpressionStmt(exp));
    }*/
    
    @Provide
    //Arbitrary<ForStmt>
    public Arbitrary<ForStmt> genForStmt(Map<String,String> ctx) throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genForStmt::inicio");      
        
        Arbitrary<VariableDeclarationExpr> variableD = genVarDeclAssignInt(ctx);
        
        VariableDeclarationExpr variable = variableD.sample();
        
        
        Arbitrary<LiteralExpr> inteiro = Arbitraries.integers().between(1, 100).map(i -> new IntegerLiteralExpr(
                        String.valueOf(i)));

        
        Map<String, String> newCtx = new HashMap<String, String>(ctx);
        
        System.out.println(variable.getVariable(0));
        
        Arbitrary<Expression> compare = mCore.genExpressionOperatorFor(newCtx, PrimitiveType.intType(),variable.getVariable(0),inteiro );
           
        Arbitrary<Expression> e = mCore.genExpression(ctx, PrimitiveType.intType());   
       
        
        Arbitrary<Statement> a = genStatement(newCtx);
 
        NodeList<Expression> nodes = new NodeList<>(variable);
       
        
     
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genForStmt::fim"); 
        return e.map(exp -> new ForStmt(nodes, compare.sample(), nodes.addFirst(exp),a.sample()));
    }   
    
    
     @Provide
    public Arbitrary<VariableDeclarationExpr> genVarDeclAssignInt(Map<String,String> ctx) throws ClassNotFoundException {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclarator::inicio");
        Arbitrary<PrimitiveType> pt = mBase.primitiveTypesInt().map(
                t -> new PrimitiveType(t));
        
        Arbitrary<Type> t = Arbitraries.oneOf(pt);
        
        Type tp = t.sample();
        
        Arbitrary<Expression> e = mCore.genExpression(ctx, tp);       
       
        String v = Arbitraries.of(mValidNames).sample();
        
        Arbitrary<LiteralExpr> inteiro = Arbitraries.integers().between(1, 100).map(i -> new IntegerLiteralExpr(
                        String.valueOf(i)));
        
        ctx.put(v, tp.asString());
        
        mValidNames.remove(v);
        
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genVarDeclarator::fim");
        
        return e.map(obj -> new VariableDeclarationExpr(
                new VariableDeclarator(tp, v, inteiro.sample())));
    } 
   
}
