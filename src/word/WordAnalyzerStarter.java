package word;

public class WordAnalyzerStarter {
    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("请将源文件路径传入命令行参数！");
            return;
        }

        WordAnalyzer wordAnalyzer = new WordAnalyzer(args[0]);
        System.out.println("*** 词法分析：" + args[0]);
        wordAnalyzer.run();
        System.out.println("*** 词法分析完成");
    }
}
