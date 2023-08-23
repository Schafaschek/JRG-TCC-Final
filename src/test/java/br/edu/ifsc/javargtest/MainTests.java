package br.edu.ifsc.javargtest;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.printer.DotPrinter;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import net.jqwik.api.*;
import br.edu.ifsc.javargtest.JRGLog.Severity;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.printer.PrettyPrinter;
import javax.tools.JavaCompiler;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;

/**
 *
 * @author samuel
 */
public class MainTests {
    
    private static final String SKELETON_PATH = 
        "src/main/java/br/edu/ifsc/javarg/MainClass.java";
       
    private CompilationUnit mSkeleton;
    
    private ClassTable mCT;
   
    private JRGBase mBase;
    
    private JRGCore mCore;
    
    private JRGStmt mStmt;
    
    private JRGOperator mOperator;
    
    private Map<String, String> mCtx;
    
    public MainTests() throws FileNotFoundException, IOException {
        mSkeleton = StaticJavaParser.parse(new File(SKELETON_PATH));
        
        dumpAST();
        
        JRGLog.logLevel = Severity.MSG_ERROR;
     
    }
    
    @BeforeTry
    public void createObjects() {        
        mCT = new ClassTable(loadImports());
        
        mBase = new JRGBase(mCT);   
        
        mCore = new JRGCore(mCT , mBase);       
        
        mStmt = new JRGStmt(mCT , mBase, mCore);  
        
        mOperator = new JRGOperator(mCT , mBase , mCore);
        
        mCtx = new HashMap<String, String>();
       
    }
    
    @AfterTry
    public void cleaningmCtx(){
        mCtx.clear();
    }
    
    // Auxiliary methods    
    private List<String> loadImports() {
        NodeList<ImportDeclaration> imports = mSkeleton.getImports();
        
        List<String> list = new ArrayList<>();
        
        Iterator<ImportDeclaration> it = imports.iterator();        
        while (it.hasNext()) {
            ImportDeclaration i = it.next();            
            list.add(i.getName().asString());
        }
        
        return list;
    }
    
    private void dumpAST() throws IOException {
        DotPrinter printer = new DotPrinter(true);
        
        try (FileWriter fileWriter = new FileWriter("ast.dot");
        
            PrintWriter printWriter = new PrintWriter(fileWriter)) {
            
            printWriter.print(printer.output(mSkeleton));            
            
        }
    }
           
    private void printData(CompilationUnit Classe) throws IOException{
        //DotPrinter printer = new DotPrinter(true);
        
        PrettyPrinter printer = new PrettyPrinter();
        
        try (FileWriter arq = new FileWriter("MainClass.java");
        
            PrintWriter gravarArq = new PrintWriter(arq)) {
            
            //gravarArq.print(mSkeleton.toString());   
            //gravarArq.print(mSkeleton.toString());   
            gravarArq.print(printer.print(Classe));          
        } 
        
        compileOracleJDK("MainClass.java");
        compileOpenJDK("MainClass.java");
        compileZuluJDK("MainClass.java");
        compileBellsoftJDK("MainClass.java");
    }
    
    public void compileOracleJDK(String arquivo2) throws IOException{
        PrintWriter output = new PrintWriter(new FileWriter("logCompilacaoOracle.txt", true));

        int compileResult = com.sun.tools.javac.Main.compile
        (new String[]{"-sourcepath","C:\\Program Files\\Java\\jdk-18.0.2.1\\bin",arquivo2},output);
    }

    
     public void compileOpenJDK(String arquivo2) throws IOException{
        PrintWriter output = new PrintWriter(new FileWriter("logCompilacaoOpen.txt", true));
        
        int compileResult = com.sun.tools.javac.Main.compile
        (new String[]{"-sourcepath", "C:\\Program Files\\Java\\openJDK\\jdk-18.0.1.1\\bin",arquivo2},output);
    }
     
      public void compileZuluJDK(String arquivo2) throws IOException{
        PrintWriter output = new PrintWriter(new FileWriter("logCompilacaoZulu.txt", true)); 
     
        int compileResult = com.sun.tools.javac.Main.compile
        (new String[]{"-sourcepath", "C:\\Program Files\\Java\\zuluJDK\\zulu18.32.11-ca-jdk18.0.2-win_x64\\bin", arquivo2},output);
    }
      
      public void compileBellsoftJDK(String arquivo2) throws IOException{
        PrintWriter output = new PrintWriter(new FileWriter("logCompilacaoBellsoft.txt", true));
    
        int compileResult = com.sun.tools.javac.Main.compile
        (new String[]{"-sourcepath", "C:\\Program Files\\Java\\bellsoftJDK\\jdk-18.0.2\\bin",arquivo2},output);
    }
    
    //@Example
    boolean checkGenPrimitiveType() {
        Arbitrary<PrimitiveType.Primitive> t = mBase.primitiveTypes();
        
        Arbitrary<LiteralExpr> e = t.flatMap(tp -> mBase.genPrimitiveType(
                new PrimitiveType(tp)));
        
        System.out.println("Expressão gerada (tipo primitivo): " + 
                e.sample().toString());
 
        return true;        
    }
    
    //@Example
    boolean checkGenPrimitiveString() {        
        Arbitrary<LiteralExpr> s = mBase.genPrimitiveString();
        
        System.out.println("Frase gerada: " + s.sample());
        
        return true;
    }
    
    //@Example
    boolean checkGenObjectCreation() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenObjectCreation"
                + "::inicio");
        
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        
        c.setName("br.edu.ifsc.javargexamples.B");              
        
        //Arbitrary<ObjectCreationExpr> e = mCore.genObjectCreation(c);
        Arbitrary<Expression> e = mCore.genObjectCreation(mCtx, c);
        
        if (e != null) {
            System.out.println("ObjectCreation gerado: " + 
                    e.sample().toString());
        }
        else {
            JRGLog.showMessage(Severity.MSG_ERROR, "Não foi possível gerar "
                    + "criação de objeto");
        }
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenObjectCreation::fim");
        
        return true;
    }
    
    //@Property(tries = 10)
    boolean checkGenMethodInvokation() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMethodInvokation"
                + "::inicio");
        
        ClassOrInterfaceType c = new ClassOrInterfaceType();
        
        c.setName("br.edu.ifsc.javargexamples.B");
        //Arbitrary<MethodCallExpr> e = mCore.genMethodInvokation(c);
        Arbitrary<MethodCallExpr> e = mCore.genMethodInvokation(mCtx,
                ReflectParserTranslator.reflectToParserType("int"));
        
        if (e != null) {
            System.out.println("Method gerado: " + e.sample().toString());
            
        }
        else {
            JRGLog.showMessage(Severity.MSG_ERROR, "Não foi possível gerar "
                    + "criação do método");
            
        }
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenMethodInvokation"
                + "::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenCandidatesMethods() throws ClassNotFoundException {        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesMethods"
                + "::inicio");
        
        Arbitrary<Method> b = mCore.genCandidatesMethods("int");
        
        System.out.println("Candidatos Methods: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesMethods"
                + "::fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesFields() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesFields"
                + "::inicio");
        
        Arbitrary<Field> b = mCore.genCandidatesField("int");
        
        System.out.println("Candidatos Fields: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesFields:"
                + ":fim");
        
        return true;
    } 
    
    //@Example
    boolean checkGenCandidatesConstructors() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesConstructors"
                + "::inicio");
        
        Arbitrary<Constructor> b = mCore.genCandidatesConstructors("br.edu."
                + "ifsc.javargexamples.B");
        
        System.out.println("Candidatos Constructors: " + b.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidatesConstructors"
                + "::fim");
        
        return true;
    } 
    
    //@Property(tries = 10)
    boolean checkGenExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::inicio");
        
        try {
        
            Arbitrary<Expression> e = mCore.genExpression(mCtx,
                    ReflectParserTranslator.reflectToParserType("int"));
            System.out.println("Expressão gerada: " + e.sample());
        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
            return false;
        }
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenAttributeAccess() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenAtributteAcess"
                + "::inicio");
        
        Arbitrary<FieldAccessExpr> e = mCore.genAttributeAccess(mCtx,
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("Acesso gerado: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenUpCast() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenUpCast"
                + "::inicio");
        
         Arbitrary<CastExpr> e = mCore.genUpCast(mCtx,
                 ReflectParserTranslator.reflectToParserType("br.edu.ifsc."
                + "javargexamples.Aextend"));
        
        System.out.println("CheckGenUpCast: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenUpCast"
                + "::final");
        
        return true;
    }
    
    //@Example
    boolean checkGenVar() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVar"
                + "::inicio");
        
        Arbitrary<NameExpr> e = mCore.genVar(mCtx,
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("checkGenVar: " + e.sample());
        
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVar"
                + "::final");
        return true;
    }
   
    //@Example
    boolean checkSuperTypes() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSuperTypes"
                + "::inicio");
        
        List<Class> b = mCT.superTypes("br.edu.ifsc."
                + "javargexamples.AextendExtend");
        
        b.forEach((i) -> {
            System.out.println("SuperTypes: " + i );
        });
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSuperTypes"
                + "::final");
        
        return true;
    }
    
    //@Example
    boolean checkSubTypes() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::inicio");
        
        List<Class> b = mCT.subTypes("br.edu.ifsc."
                + "javargexamples.A");
        
         b.forEach((i) -> {
            System.out.println("subTypes: " + i.toString() );
        });
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::final");
        
        return true;
    }
    
    //@Example
    boolean checkSubTypes2() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::inicio");
        
        List<Class> b = mCT.subTypes2("br.edu.ifsc."
                + "javargexamples.A");
        
         b.forEach((i) -> {
            System.out.println("subTypes: " + i.toString() );
        });
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes"
                + "::final");
        
        return true;
    }
    
   
    
    //@Example
    boolean checkGenCandidateUpCast() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidateUpCast"
                + "::inicio");
        
        Arbitrary<Class> b = mCore.genCandidateUpCast("br.edu.ifsc."
                + "javargexamples.A");
        
        System.out.println("Candidatos UpCast: " + b.sample().getName());
         
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenCandidateUpCast"
                + "::final");
        
        return true;
    }         
     
    
    @Property(tries = 1)
    boolean checkGenBlockStmt() throws ClassNotFoundException, IOException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenBlockStmt::inicio");
        
        Arbitrary<BlockStmt> e = mStmt.genBlockStmt(mCtx);
        
        System.out.println("BlockStmt: " + e.sample());
        
        ClassOrInterfaceDeclaration classe = mSkeleton.getClassByName("MainClass").get();
        
        List<MethodDeclaration> ms = classe.getMethods();
        
        MethodDeclaration m = ms.get(0);
        
        m.setBody(e.sample());        
        
        printData(mSkeleton);
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenBlockStmt::fim");
        
        return true;
    }
    
    //@Property(tries = 100)
    boolean checkGenVarDeclAssign() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::inicio");
        
        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarDeclAssign(mCtx);
        
        System.out.println("checkGengenVarDeclaration: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::fim");
        
        return true;
    }
    
    //@Property(tries = 100)
    /*boolean checkGenVarDecl() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::inicio");
        
        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarDecl(mCtx);
        
        System.out.println("checkGengenVarDeclaration: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::fim");
        
        return true;
    }*/
    
    
          
     //@Example 
     boolean checkGenIfStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenIfStmt::inicio");
        
        Arbitrary<IfStmt> e = mStmt.genIfStmt(mCtx);
        
        System.out.println("checkGenIfStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenIfStmt::fim");
        
        return true;
    }
     
    //@Example
    boolean checkWhileStmt() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkWhileStmt::inicio");
        
        Arbitrary<WhileStmt> e = mStmt.genWhileStmt(mCtx);
        
        System.out.println("checkWhileStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkWhileStmt::fim");
        
        return true;
        
    }
    
    //@Example
    boolean checkGenStatement() throws ClassNotFoundException, IOException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatement::inicio");
        
        Arbitrary<Statement> e = mStmt.genStatement(mCtx);
        
        System.out.println("checkGenStatement: " + e.sample());
        
        System.out.println(mSkeleton.getClassByName("MainClass"));
        
        ClassOrInterfaceDeclaration classe = mSkeleton.getClassByName("MainClass").get();
        
        
        classe.addMethod("main", Modifier.publicModifier().getKeyword(),Modifier.Keyword.STATIC);
        //mSkeleton.addInterface(e.sample().toString());
        
        
        
        classe.addInitializer().addAndGetStatement(e.sample());
        
        //imprimiDados(mSkeleton.addClass(classe.toString()));
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatement::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenExpressionStmt() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpressionStmt::inicio");
        
        Arbitrary<ExpressionStmt> e = mStmt.genExpressionStmt(mCtx);
        
        System.out.println("checkGenExpressionStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpressionStmt::fim");
        
        return true;
    }
     
    //@Example
    //@Property(tries = 10)
    boolean checkGenLogiExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLogiExpression::inicio");
        
        Arbitrary<BinaryExpr> e = mOperator.genLogiExpression(mCtx);
        
        System.out.println("checkGenLogiExpression: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLogiExpression::fim");
        
        return true;
    }
    
   
    
    //@Example
    //@Property(tries = 10)
    boolean checkGenRelaExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenRelaExpression::inicio");
        
        Arbitrary<BinaryExpr> e = mOperator.genRelaExpression(mCtx);
        
        System.out.println("checkGenRelaExpression: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenRelaExpression::fim");
        
        return true;
    }
    
    //@Example
    //@Property(tries = 1000)
    boolean checkGenArithExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenArithExpression::inicio");
        
        Arbitrary<BinaryExpr> e = mOperator.genArithExpression(mCtx,
                ReflectParserTranslator.reflectToParserType("int"));
        
        System.out.println("checkGenArithExpression: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenArithExpression::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenStatementList() {
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatementList::inicio");
        
        Arbitrary<NodeList<Statement>> e = mStmt.genStatementList(mCtx);
        
        System.out.println("checkGenStatementList: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatementList::fim");
        
        return true;
    }
    //@Property
    boolean checkGenVarDeclarationStmt() throws ClassNotFoundException {
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclarationStmt::inicio");
        
        Arbitrary<ExpressionStmt> e = mStmt.genVarDeclarationStmt(mCtx);
        
        System.out.println("checkGenVarDeclarationStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclarationStmt::fim");
        
        return true;
    }
    
    //@Example
    boolean checkGenVarAssingStmt() throws ClassNotFoundException {
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarAssingStmt::inicio");
        
        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarAssingStmt(mCtx);
        
        System.out.println("checkGenVarAssingStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarAssingStmt::fim");
        
        return true;
    }
    
 
    //@Example
    boolean checkGenLambdaExpr() throws ClassNotFoundException {
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLambdaExpr::inicio");
        
        Arbitrary<LambdaExpr> e = mCore.genLambdaExpr(mCtx);
        
        System.out.println("checkGenLambdaExpr: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLambdaExpr::fim");
        
        return true;
    }
    
    //@Example
    /*boolean checkGenTypeAssingStmt() throws ClassNotFoundException {
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::inicio");
        
        Arbitrary<AssignExpr> e = mStmt.genTypeAssingStmt(mCtx);
        
        System.out.println("checkGenTypeAssingStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::fim");
        
        return true;
    }*/
    
    //@Example
    boolean checkGenFor() throws ClassNotFoundException {
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::inicio");
        
        Arbitrary<ForStmt> e = mStmt.genForStmt(mCtx);
        //mStmt.genForStmt(mCtx);
        System.out.println("checkGenTypeAssingStmt: " + e.sample());
        
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::fim");
        
        return true;
    }
}
