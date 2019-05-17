# SimpleCompiler
## 运行环境
JRE 1.8及以上
> 我的运行环境
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

**test_program_ok.pas**: PPT上的代码，消除了m未定义错的代码。运行结果：
<img src="pics/test_program_ok.png">

**test_program_ver_error.pas**: 基于PPT代码，添加了变量重复定义。运行结果如下：
<img src="pics/test_program_var_error.png">

**test_program_func_error.pas**: 基于正确的代码添加了调用未定义函数错误。运行结果如下：
<img src="pics/test_program_func_error.png">

## 生成文件说明
当通过词法和语法分析后，除了源文件外，会生成四个文件：
<img src="pics/generate_files.png">

**dyd文件**：词法分析结果，二元式。

**err文件**：错误信息文件

**pro文件**：过程表

**var文件**：变量表