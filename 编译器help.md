## 文件说明

### interpreter  解释器

```sh
Absyn.fs micro-C abstract syntax                              抽象语法
grammar.txt informal micro-C grammar and parser specification 文法定义
CLex.fsl micro-C lexer specification                          fslex词法定义
CPar.fsy micro-C parser specification                         fsyacc语法定义
Parse.fs micro-C parser                                       语法解析器
Interp.fs micro-C interpreter                                 解释器
example/ex1.c-ex25.c micro-C example programs                 例子程序
interpc.fsproj                                                项目文件

```

### compiler  编译器

```sh
Machine.fs definition of micro-C stack machine instructions  VM 指令定义
Machine.java micro-C stack machine in Java                   VM 实现 java
machine.csproj  machine project file                         VM 项目文件

Comp.fs compile micro-C to stack machine code             编译器 输出 stack vm 指令序列
Backend.fs x86_64 backend                                 编译器后端 翻译 stack vm 指令序列到 x86_64
driver.c     runtime support                                 运行时支持程序
microc.fsproj                                                编译器项目文件
```

## 构建与执行

### B 编译器

#### B.1 microc编译器构建步骤

```sh
# 构建 microc.exe 编译器程序 
dotnet restore  microc.fsproj # 可选
dotnet clean  microc.fsproj   # 可选
dotnet build  microc.fsproj   # 构建 ./bin/Debug/net5.0/microc.exe

dotnet run -p microc.fsproj example/ex1.c    # 执行编译器，编译 ex1.c，并输出  ex1.out 文件
dotnet run -p microc.fsproj -g example/ex1.c   # -g 查看调试信息

dotnet built -t:ccrun microc.fsproj     # 编译并运行 example 目录下多个文件
dotnet built -t:cclean microc.fsproj    # 清除生成的文件
```

#### B.2 dotnet fsi 中运行编译器

```sh
# 启动fsi
dotnet fsi

#r "nuget: FsLexYacc";;

#load "Absyn.fs"  "CPar.fs" "CLex.fs" "Debug.fs" "Parse.fs" "Machine.fs" "Backend.fs" "Comp.fs" "ParseAndComp.fs";;   

# 运行编译器
open ParseAndComp;;
compileToFile (fromFile "example\ex1.c") "ex1";; 

Debug.debug <-  true   # 打开调试
compileToFile (fromFile "example\ex4.c") "ex4";; # 观察变量在环境上的分配
#q;;


# fsi 中运行
#time "on";;  // 打开时间跟踪

# 参考A. 中的命令 比较下解释执行解释执行 与 编译执行 ex11.c 的速度
```


#### D.3 Java

```sh
javac -encoding UTF-8 Machine.java
java Machine ex9.out 3

javac Machinetrace.java
java Machinetrace ex9.out 0
java Machinetrace ex9.out 3
```
