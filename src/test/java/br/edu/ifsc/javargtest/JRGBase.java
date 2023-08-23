/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.javargtest;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import static com.sun.jmx.mbeanserver.Util.cast;
import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;

/**
 *
 * @author lukra
 */
public class JRGBase {
    
    private ClassTable mCT;
    
    public JRGBase(ClassTable ct) {
        mCT = ct;
    }
    
    /*public RandomTypes() {
    
    }*/
        
    @Provide
    public  Arbitrary<ClassOrInterfaceType>  classOrInterfaceTypes() 
            throws ClassNotFoundException {
        List<ClassOrInterfaceType> list = new ArrayList<>();
        
        for (String s : mCT.getTypes()) {
            ClassOrInterfaceType c = new ClassOrInterfaceType();
            c.setName(s);
            list.add(c);
        }
        
        return Arbitraries.of(list);
    }
    
    @Provide
    Arbitrary<PrimitiveType.Primitive> primitiveTypes() {
        return Arbitraries.of(PrimitiveType.Primitive.BOOLEAN, PrimitiveType.Primitive.INT, 
        PrimitiveType.Primitive.CHAR, PrimitiveType.Primitive.DOUBLE, PrimitiveType.Primitive.LONG,
        PrimitiveType.Primitive.SHORT, PrimitiveType.Primitive.BYTE);
    }
    
    @Provide
    Arbitrary<PrimitiveType.Primitive> primitiveTypesInt() {
        return Arbitraries.of(PrimitiveType.intType().getType());
    }
    
    @Provide
    Arbitrary<PrimitiveType.Primitive> primitiveTypesNumericals() {       
        //PrimitiveType.Primitive.FLOAT,
    
        return Arbitraries.of(PrimitiveType.Primitive.INT,PrimitiveType.Primitive.DOUBLE,
        PrimitiveType.Primitive.LONG,
        PrimitiveType.Primitive.SHORT);
    }
    
    public boolean isNumericType(Type t){
        switch (t.asPrimitiveType().getType()) {
            case DOUBLE:
                return true;                 
            //case FLOAT:
                //return true; 
            case INT:        
                return true; 
            case LONG:
                return true; 
            case BYTE:
                return true; 
            case SHORT:
                 return true;  
        }
        
        return false;
    }
    
    @Provide
    public  Arbitrary<LiteralExpr> genPrimitiveString() {
        JRGLog.showMessage(JRGLog.Severity.MSG_XDEBUG, "genPrimitiveString::inicio");
       
        return Arbitraries.strings().withCharRange('a', 'z')
                .ofMinLength(1).ofMaxLength(5).map(
                S -> new StringLiteralExpr(String.valueOf(S)));
        
    }
    
    @Provide
    public  Arbitrary<LiteralExpr> genPrimitiveType(PrimitiveType t) {
        LiteralExpr e = null;
        
        switch (t.getType()) {
            
            case BOOLEAN:                 
                return Arbitraries.of(true, false).map(b -> new 
                        BooleanLiteralExpr(b));
            
            // Feita mudança para arrumar os chars, tirado o ascii para range A,Z
            case CHAR:                
                return Arbitraries.chars().range('A', 'Z').map(c -> new CharLiteralExpr(
                        c));
                
            case DOUBLE:                
                return Arbitraries.doubles().between(-99999, +99999).map(d -> new DoubleLiteralExpr(
                        String.valueOf(d)));
                
            //case FLOAT:                 
                //return Arbitraries.doubles().between(0, 1000).map(f -> new DoubleLiteralExpr(
                        //String.valueOf(f.floatValue())));   
                //tá convertendo para um double
            case INT:                
                return Arbitraries.integers().map(i -> new IntegerLiteralExpr(
                        String.valueOf(i)));
                
            case LONG:
                return Arbitraries.integers().map(l -> new IntegerLiteralExpr(
                      String.valueOf(l)));
                
            case BYTE:                
                return Arbitraries.bytes().map(bt -> new IntegerLiteralExpr(
                        String.valueOf(bt)));
                
            case SHORT:
                return Arbitraries.shorts().map(s -> new IntegerLiteralExpr(
                        String.valueOf(s)));                
        }
        
        return Arbitraries.just(e);
    }
    
    
}
