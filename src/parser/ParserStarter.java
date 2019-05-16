package parser;

public class ParserStarter {
    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("请将源文件路径传入命令行参数！");
            return;
        }

        Parser parser = new Parser(args[0]);
        parser.checkHasError();
        System.out.println("***语法分析：" + args[0]);
        parser.run();
        System.out.println("***语法分析完成");
    }
}
