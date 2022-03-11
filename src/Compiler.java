import SysY.AST.Root;
import SysY.ErrorHandle.ComplierError;
import SysY.ErrorHandle.ErrorHandle;
import SysY.LexicAnalysis.LexicAnalysis;
import SysY.ObjectCode.ObjectCode;
import SysY.ObjectCode.ObjectCodeOpt;
import SysY.SemanticAnalysis.Intermediate;
import SysY.SemanticAnalysis.Quadruple;
import SysY.SymbolTable.SymbolTable;
import SysY.SyntaxAnalysis.SyntaxAnalysis;
import SysY.LexicAnalysis.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // 开关
        boolean outputLexicAnalysis = false;
        boolean outputSyntaxAnalysis = false;
        boolean outputError = true;
        boolean outputIntermediate = true;
        boolean outputObjectCode = true;
        boolean IROpt = true;
        boolean mipsOpt = true;
        // 输入
        File inputFile = new File("testfile.txt");
        FileReader fileReader = new FileReader(inputFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        ArrayList<String> stringList = new ArrayList<>();
        String curString;
        while ((curString = bufferedReader.readLine()) != null) {
            stringList.add(curString);
        }
        // 错误处理
        ErrorHandle errorHandle = new ErrorHandle();
        // 符号表
        SymbolTable symbolTable = new SymbolTable();
        // 中间代码（四元式）
        Intermediate intermediate = new Intermediate(symbolTable);
        // 词法分析
        LexicAnalysis lexicAnalysis = new LexicAnalysis(stringList);
        lexicAnalysis.analyse();
        ArrayList<Token> tokenList = lexicAnalysis.getTokenList();
        if (outputLexicAnalysis) {
            for (Token token : tokenList) {
                System.out.println(token.getTokenString());
            }
            System.out.println("********************************词法分析**********************************");
        }
        // 语法分析
        SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis(symbolTable, errorHandle, intermediate, tokenList);
        syntaxAnalysis.analyse();
        ArrayList<String> syntaxList = syntaxAnalysis.getSyntaxList();
        if (outputSyntaxAnalysis) {
            for (String string : syntaxList) {
                System.out.println(string);
            }
            System.out.println("********************************语法分析**********************************");
        }
        // 语法树
        Root ast = syntaxAnalysis.getAst();
        //错误处理
        ast.errorCheck();
        if (errorHandle.getErrorCount() != 0) {
            System.out.println(errorHandle.getErrorCount() + " errors");
            File outputFile = new File("error.txt");
            FileWriter fileWriter = new FileWriter(outputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (ComplierError complierError : errorHandle.getErrorList()) {
                if (outputError) {
                    System.out.println(complierError.toString());
                }
                bufferedWriter.write(complierError.toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } else {
            // 语义分析及中间代码生成
            ast.semanticAnalyse();
            File outputFile1 = new File("19373135_田旗舰_优化前.txt");
            FileWriter fileWriter1 = new FileWriter(outputFile1);
            BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
            for (Quadruple quadruple : intermediate.getIntermediateCode()) {
                if (outputIntermediate) {
                    System.out.println(quadruple.toString() + " *** " + quadruple.getsRegFlag());
                }
                bufferedWriter1.write(quadruple.toString());
                bufferedWriter1.newLine();
                bufferedWriter1.flush();
            }
            System.out.println("********************************中间代码**********************************");
            // 中间代码优化
            if (IROpt) {
                intermediate.IROpt();
            }
            File outputFile2 = new File("19373135_田旗舰_优化后.txt");
            FileWriter fileWriter2 = new FileWriter(outputFile2);
            BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2);
            for (Quadruple quadruple : intermediate.getIntermediateCode()) {
                if (outputIntermediate) {
                    System.out.println(quadruple.toString()+ " *** " + quadruple.getsRegFlag());
                }
                bufferedWriter2.write(quadruple.toString());
                bufferedWriter2.newLine();
                bufferedWriter2.flush();
            }
            System.out.println("********************************中间代码**********************************");
            // 目标代码生成
            if (mipsOpt) {
                ObjectCodeOpt objectCodeOpt = new ObjectCodeOpt(intermediate.getIntermediateCode(), symbolTable);
                objectCodeOpt.genMipsCode();
                File outputFile = new File("mips.txt");
                FileWriter fileWriter = new FileWriter(outputFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                for (String code : objectCodeOpt.getMipsCode()) {
                    if (outputObjectCode) {
                        System.out.println(code);
                    }
                    bufferedWriter.write(code);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            } else {
                ObjectCode objectCode = new ObjectCode(intermediate.getIntermediateCode(), symbolTable);
                objectCode.genMipsCode();
                File outputFile = new File("mips.txt");
                FileWriter fileWriter = new FileWriter(outputFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                for (String code : objectCode.getMipsCode()) {
                    if (outputObjectCode) {
                        System.out.println(code);
                    }
                    bufferedWriter.write(code);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
            System.out.println("********************************目标代码**********************************");
        }
    }
}
