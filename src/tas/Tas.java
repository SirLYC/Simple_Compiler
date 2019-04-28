package tas;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Tas {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("usage: java tas.Tas <filename>");
            return;
        }

        for (String arg : args) {
            try {
                System.out.println("--------------- process " + arg + " ---------------");
                WordAnalyzer wordAnalyzer = new WordAnalyzer(arg);
                wordAnalyzer.run();
            } catch (IOException e) {
                if (e instanceof FileNotFoundException) {
                    System.err.println("file" + args[0] + "not found");
                } else {
                    System.err.println("IO error:");
                    e.printStackTrace();
                }
            }
        }
    }
}
