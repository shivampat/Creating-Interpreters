package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: jlox [script name]");
            System.exit(1);
        }
        else if (args.length == 1) {
            runFile(args[1]);
        }
        else {
            runPrompt();
        }
    }    

    private static void runFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void run(String statement) {
        Scanner scanner = new Scanner(statement);    
        List<Token> tokens = scanner.scanTokens();      

        // Remove once actually implemented 
        // For now just print all tokens
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
