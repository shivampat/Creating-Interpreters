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

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);")

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) throws IOException {
        writer.println("\tinterface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("\t}");
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

        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");

        
        for (String field : fields.split(",")) {
            writer.println("\t\tfinal " + field.trim() + ";");
        }

        writer.println("\t}");
        writer.println();
    }

}