package com.shivam.tool;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: generate_ast <output dir>");
            System.exit(1);
        }

        String dirName = args[0];
        defineAst(dirName, "Expr", Arrays.asList(
        "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(String dirName, String baseName, List<String> types) throws IOException {
        String path = dirName + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.shivam.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("abstract class " + baseName + " {");

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) throws IOException {
        writer.println("\tpublic class " + className + " implements " + baseName + " {");
        writer.println("\t\t" + className + "(" + fields + ") {");

        for (String field : fields.split(",")) {
            field = field.trim();
            String fieldName = field.split(" ")[1].trim();
            writer.println("\t\t\tthis." + fieldName + " = " + fieldName + ";");
        }
        writer.println("\t\t}");
        writer.println();
        
        for (String field : fields.split(",")) {
            writer.println("\t\tfinal " + field.trim() + ";");
        }

        writer.println("\t}");
        writer.println();
    }

}