import parser.ParserStarter;
import word.WordAnalyzerStarter;

public class SimpleCompiler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: java < -jar <jar_filename> >|SimpleCompiler <filePath>");
            return;
        }
        WordAnalyzerStarter.main(args[0]);
        ParserStarter.main(args[0]);
    }
}
