# SimpleCompiler

## 重要

**本项目仅供参考，请勿作为实验代码提交！**

**本项目仅供参考，请勿作为实验代码提交！**

**本项目仅供参考，请勿作为实验代码提交！**

好吧，其实你真要抄我也没办法。但是，作为老学长（看到的应该很多都是大三的学弟学妹吧），我还是苦口婆心的劝你们一句，好好学习，大三再不努力大学就真的过去了～

有问题可以通过`issue`交流～

## 运行环境
JRE 1.8及以上
<img src="pics/run_time.png">

## 使用

### Jar方式

```
java -jar SimpleCompiler.jar test_program.pas
```

### 编译运行

```
# 编译(src目录下)
javac SimpleCompiler.java constant/* parser/* word/*
# 运行
java SimpleCompiler test_program.pas
```

## 测试文件说明

**test_program.pas**: PPT上的代码，有一个m未定义的错误。运行结果：
<img src="pics/test_program.png">

**test_program_ok.pas**: PPT上的代码，消除了m未定义错误的代码。运行结果：
<img src="pics/test_program_ok.png">

**test_program_ok_multi.pas**: 语法正确，具有多个嵌套过程，不同过程重复变量名的代码。运行结果：
<img src="pics/test_program_ok_multi.png">

**test_program_error.pas**: 有词法错误的代码。运行结果：
<img src="pics/test_program_error.png">

**test_program_syntax_error.pas**: 基于PPT代码，添加了多个错误的代码。运行结果如下：
<img src="pics/test_program_syntax_error.png">


## 生成文件说明
当通过词法和语法分析后，除了源文件外，会生成四个文件：
<img src="pics/generate_files.png">

**dyd文件**：词法分析结果，二元式。

**err文件**：错误信息文件

**pro文件**：过程表

**var文件**：变量表