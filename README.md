2020-2021学年第2学期 实 验 报 告

![zucc](assets/zucc.png)

- 课程名称：编程语言原理与编译
- 实验项目：<u>MicroC</u>
- 专业班级：计算机1803
- 学生学号：31801135，31801137
- [编译原理期末大作业 (github)](https://github.com/YeQiuO/lec_program)



## 简介

这是一个名为MicroC的编译原理大作业，主要基于microC和Yuby完成，通过对解释器和编译器的代码改进和开发，实现了部分C语言的语法(并改进了部分)。主要完成功能如下：



## 项目说明

### 结构

- `CLex.fsl`生成的`CLex.fs`词法分析器。

  + CLex 中定义了基本的关键字、标识符、常量、使用大写字母表示

    程序读到这个符号就会转换为我们定义的大写字母，然后就给 CPar 处理

- `CPar.fsy`生成的`CPar.fs`语法分析器。

  + CPar 文件分为两部分，每个部分之间通过 %% 分隔

  + 第一部分声明需要使用的变量(词元)，声明变量后还需要声明优先级

  + 第二部分定义语法规则(文法)

    包括 : statement ,expression ,function  ,main ,vardeclare   variabledescirbe ,type ,const这些基本元素

    表示识别到前面定义的这些大写字母组成的符号串后,怎么处理这些规则

- `Absyn.fs` 定义了抽象语法树

  定义了变量描述、函数和类型的构造方法

- `Comp.fs`将抽象语法树转化为栈式虚拟机

- `interp.fs`对抽象语法树进行语义分析

- `Machine.fs` 虚拟机指令定义

- `Machine.java` 执行虚拟机指令

+ `Machine.java`生成`Machine.class`虚拟机与`Machinetrace.class`堆栈追踪

测试集：测试程序放在example文件夹内



### 使用方法

#### 编译器

**生成汇编指令数字文件 .out (任选以下两文件之一)**

```js
dotnet restore  microc.fsproj // 可选
dotnet clean  microc.fsproj   // 可选
dotnet build  microc.fsproj   // 构建 ./bin/Debug/net5.0/microc.exe

dotnet run -p microc.fsproj example/ex1.c    // 执行编译器，编译 ex1.c，并输出  ex1.out 文件
dotnet run -p microc.fsproj -g example/ex1.c  // -g 查看调试信息
```

**虚拟机的构建与运行**

```java
javac -encoding UTF-8 Machine.java
java Machine ex9.out 3//直接显示结果    
java Machinetrace ex9.out 0//查看栈式虚拟机每一步的细节
```

![image-20210628160209282](assets\image-20210628160209282.png)

在实际使用中发现，如果不加-encoding UTF-8，会造成编辑提示错误。



#### 解释器

**运行编译解释器  interpc.exe **

```js
dotnet restore  interpc.fsproj   // 可选
dotnet clean  interpc.fsproj     // 可选
dotnet build -v n interpc.fsproj // 构建 ./bin/Debug/net5.0/interpc.exe
```

**执行解释器**

```
./bin/Debug/net5.0/interpc.exe ex_all.c
dotnet run -p interpc.fsproj ex_all.c
dotnet run -p interpc.fsproj ex_all.c  //-g 显示token AST 等调试信息
```



## 功能实现

### 编译器

#### 1.变量定义时赋值

+ 简介：原本的microC声明和赋值不能写在一行语句中，现在声明和赋值可以写在一起了

+ 对比

  ```c
  //old
  void main(){
      int a;
      a = 3;
      print a;
  }
  ```

  ```c
  //new ex1.c
  void main(){
      int a = 3;
      print a;
  }
  ```

+ 堆栈图

  <img src="assets\image-20210628160751205.png" alt="image-20210628160751205" style="zoom:67%;" />

#### 2.+=|-=|*=|/= 

+ 简介：+=对应于相加并赋值，后面跟表达式

+ 例子：

  ```c
  //ex3.c
  void main() {
      int i = 3;
      i += 1;
      print(i);
  
      i -= 1;
      print(i);
  
      i *= 2;
      print(i);
  
      i = i/2;
      print(i);
  }
  ```

+ 堆栈图

  <img src="C:\Users\12147\Desktop\microc\assets\image-20210628181631321.png" alt="image-20210628181631321" style="zoom: 67%;" />

  <img src="C:\Users\12147\Desktop\microc\assets\image-20210628181831304.png" alt="image-20210628181831304" style="zoom:67%;" />

  ![image-20210628181857170](C:\Users\12147\Desktop\microc\assets\image-20210628181857170.png)

  首先取得变量e1的位置，DUP一份后取值，再编译右值表达式e2，最后match操作进行加减乘除后赋值

#### 3.自增、自减操作

+ 简介：包含i++，++i，i--，--i的操作，其中自增自减符号在前面和在后面虚拟栈中的操作有所不同，在后面需要保留操作之前的值，而在前面则不需要

+ 例子：

  ```c
  void main(){
      int i = 1;
      print i++;
      print i;
      print i--;
      print i;
      print ++i;
      print i;
      print --i;
      print i;
  }
  ```

+ 运行栈追踪

  <img src="C:\Users\12147\Desktop\microc\assets\image-20210628161525892.png" alt="image-20210628161525892" style="zoom:67%;" />

  <img src="C:\Users\12147\Desktop\microc\assets\image-20210628161756941.png" alt="image-20210628161756941" style="zoom:67%;" />

  ![image-20210628162327621](C:\Users\12147\Desktop\microc\assets\image-20210628162327621.png)

  ex2中a为1，8个print输出结果应为1 2 2 1 2 2 1 1，栈显示操作如图。

  举i++为例，首先找到i的位置（GETBP+CSTI 0+ADD），DUP一份后LDI取值，SWAP将原值保存。之后DUP一份换到栈顶的i的地址用于取值，再加入常量1，相加后STI赋值，INCSP -1将计算后的值出栈，留下备份的i值用于print。
  
  ```
  [ 4 -999    1       1         2        1      1 ]{27: ADD}
            i的值   备份的i值   i的地址   i的值   常量1
                                        相      加
  ```

#### 4.float|double|long|char

+ 简介：

  在CLex.fsl词法分析中通过F#识别，添加对应的识别规则，在CPar.fsy语法分析中添加对应的词元token，在Absyn.fs抽象语法树中定义关键字，

  float：单精度浮点型，识别格式为'数字'+'.'+'数字'+'f(F)'，在栈中占一个地址单位

  double：双精度浮点型，识别格式为'数字'+'.'+'数字'，通过字节运算拆分成两个32位整数，在虚拟机中转化为小数，在栈中占两个地址单位

  long：长整型，识别格式为'数字'+'l(L)'，通过字节运算拆分成两个32位，交给虚拟机处理，在栈中占两个地址单位

  char：字符型，识别格式为'''+'字符'+''，在栈中占一个地址单位

+ 例子：

  ```c
  //ex4.c
  void main() {
      
      long a = 100000L;
      print(a);
      
      float b = 2.55F;
      print(b);
  
      char c = 's';
      print(c);
      
      double d = 3.666;
      print(d);
  
  }
  ```

+ 堆栈图

#### 5.三目运算

+ 简介：

  a>b?a:b，格式为Expr1 ？ Expr2 ： Expr3，若Expr1为1则执行Expr2，否则执行Expr3

+ 例子：

  ```c
  //ex5.c
  void main() {
      int i=3;
      i > 5 ? i=0 : i=5;
      print(i);
  }
  ```

+ 堆栈图

<img src="C:\Users\12147\Desktop\microc\assets\image-20210628193652151.png" alt="image-20210628193652151" style="zoom:67%;" />

![image-20210628193804314](C:\Users\12147\Desktop\microc\assets\image-20210628193804314.png)

先编译右值表达式e，判断条件，如果为0，执行e2；如果为1，执行e1。

#### 6.for

+ 简介：

  for解析格式：FOR (Expr ； Expr ； Expr ) StmtM

+ 例子：

  ```c
  //ex6.c
  void main() {
      int i;
      for (i=0; i<5; i=i+1) {
          print(i);
      }
  }
  ```

+ 堆栈图

<img src="C:\Users\12147\Desktop\microc\assets\image-20210628195302598.png" alt="image-20210628195302598" style="zoom:67%;" />

![image-20210628195326218](C:\Users\12147\Desktop\microc\assets\image-20210628195326218.png)

先编译e1赋值，赋值后释放计算时占用的空间，然后进行条件判断e2，若IFNZRO=1执行body语句，相反结束for循环。

#### 7.dowhile|dountil

+ 简介：

  dowhile解析格式：DO StmtM WHILE(Expr);

  dountil解析格式：DO StmtM UNTIL(Expr);

+ 例子：

  ```c
  //ex7.c
  void main() {
      int i=3;
      do{
          i+=1;
      }while(i<3);
      print(i);
      do{
          i-=1;
      }until(i<3);
      print(i);
  }
  ```

+ 堆栈图

<img src="C:\Users\12147\Desktop\microc\assets\image-20210628200327896.png" alt="image-20210628200327896" style="zoom:67%;" />

![image-20210628200641621](C:\Users\12147\Desktop\microc\assets\image-20210628200641621.png)

与while相似，只需要将body语句第一次执行一遍后即可

<img src="C:\Users\12147\Desktop\microc\assets\image-20210628200558295.png" alt="image-20210628200558295" style="zoom:67%;" />

与dowhile的区别就是dowhile是条件为1则继续进行，而dountil是条件为1则退出循环

#### 8.switch-case

+ 简介:与c语言的switch-case不同，取消了穿透逻辑，并且没有default，实现逻辑为遇到正确的case后执行对应语句后直接跳出

+ 例子

  ```c
  int main(){
      int op = 1;
      switch (op)
      {
      case 1:
        print 1;
      case 2:
        print 2;
      case 3:
        print 3;
      }
  }
  ```

+ 运行栈追踪：

  

  




### 解释器

#### 变量初始化

- 简介:  变量声明后同时可以初始化变量的值

- 语法分析:

  ```
  | Vardec ASSIGN Expr SEMI StmtOrDecSeq { DecAndAssign (fst $1, snd $1, $3) :: $5 }
  ```

- 抽象语法树:

  ```
  | DecAndAssign of typ * string * expr
  ```

- 语义分析(解释器):

  ```
  and stmtordec stmtordec locEnv gloEnv store =
      match stmtordec with
      | Stmt stmt -> (locEnv, exec stmt locEnv gloEnv store)
      | Dec (typ, x) -> allocate (typ, x, None) locEnv store
      | DecAndAssign (typ, name, expr) -> allocate (typ, name, Some(fst (eval expr locEnv gloEnv store))) locEnv store
  ```
  
- 例子:

  ```c
  void main() {
      int i = 2;
      print i;
  }
  ```


#### float\double类型

- 简介:  float为32位浮点型, double为64位浮点型

- 语法分析:

  ```
  | ConstFloat                          { CstF $1             }
  | ConstDouble                         { CstD $1             }
  
  ConstFloat:                                              
      CSTFLOAT                            { $1       }
    | MINUS CSTFLOAT                      { - $2     }
  ;
  
  ConstDouble:                                            
      CSTDOUBLE                           { $1       }
    | MINUS CSTDOUBLE                     { - $2     }
  ;
  
  | FLOAT                               { TypF     }
  | DOUBLE                              { TypD     }
  ```

- 抽象语法树:

  ```
  | TypF
  | TypD
  | CstF of float
  | CstD of double
  ```

- 语义分析(解释器):

  ```
  // memData
      | FLOAT of float
      | DOUBLE of double
  
      member this.float =
          match this with
          | FLOAT i -> i
          | INT i -> float i
          | DOUBLE i -> float i
          | STRING i -> float i
          | _ -> failwith ("wrong float")
  
      member this.double = 
          match this with
          | DOUBLE i -> i
          | INT i -> double i
          | _ -> failwith ("wrong double")
  
  | CstF i -> (FLOAT(i), store)
  | CstD i -> (DOUBLE(i), store)
  ```

- 语义分析(编译器):

  

- 例子:

  ```c
  void main() {
      double i = 2.21;
      print i;
  
      float j = 3.21;
      print j;
  }
  ```

- 运行时堆栈:

  

#### string类型

- 简介: 字符串类型, 本项目中将其限定为64位字符串

- 语法分析:

  ```
  | ConstString                         { CstS $1             }
  
  ConstString:                                           
    CSTSTRING                           { $1       }
  ;
  
  | STRING                              { TypS     }
  ```

- 抽象语法树:

  ```
  | TypS
  | CstS of string
  ```

- 语义分析(解释器)::

  ```
  // memData
      | STRING of string
  
      member this.string = 
          match this with
          | STRING i -> i
          | INT i -> string i
          | CHAR i -> string i
          | POINTER i -> string i
          | FLOAT i -> string i
          | DOUBLE i -> string i
  
  | CstS i -> (STRING(i), store)
  ```

- 

- 例子:

  ```c
  void main() {
      string i = "编译原理";
      print i;
  }
  ```

#### print

- 简介: 拓展了 print 的功能，可以输出新增的三种类型（string float double）

- 语义分析:

  ```c
  | Prim1 (ope, e1) ->
          let (i1, store1) = eval e1 locEnv gloEnv store
  
          let res =
              match ope with
              | "!" -> if i1.int = 0 then INT(1) else INT(0)
              | "printi" ->
                  if i1 = STRING(i1.string) then
                      printf "%s " i1.string
                      i1 
                  else if i1.float = float(i1.int) then 
                      printf "%d " i1.int 
                      i1 
                  else 
                      printf "%.2f " i1.float 
                      i1 
              | "printc" ->
                  printf "%c " i1.char
                  i1
              | _ -> failwith ("unknown primitive " + ope)
  ```
  
- 例子:

  ```c
  同上方 float double string 的例子
  ```
  

#### for

- 简介:  类似于 C 的 for 循环

- 语法分析:

  ```
  | FOR Access IN RANGE LPAR Expr COMMA Expr COMMA Expr RPAR StmtM {ForIn($2, $6, $8, $10, $12)}
  ```
  
- 抽象语法树:

  ```
  | For of expr * expr * expr * stmt 
  ```

- 语义分析(解释器):

  ```
  | For(e1,e2,e3,body) ->
      let (v, store1) = eval e1 locEnv gloEnv store
      let rec loop store1 = 
      let (v,store2) = eval e2 locEnv gloEnv store1
      if v<>INT(0) then loop(snd(eval e3 locEnv gloEnv (exec body locEnv gloEnv store2)))
      else store2
  
      loop store1
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i;
      for (i = 0; i < 5; i = i + 1)
      {
          print i;
      }
  }
  ```

- 运行时堆栈：

  

#### do while

- 简介: 类似于C的do whle.先执行一次body中的语句, 不符合条件跳出循环.

- 语法分析:

  ```
  | DO StmtM WHILE LPAR Expr RPAR SEMI  { DoWhile($2, $5)      }
  ```
  
- 抽象语法树:

  ```
  | DoWhile of stmt * expr
  ```

- 语义分析(解释器):

  ```
  | DoWhile (body, e) ->
          let rec loop store1 =
              let (v, store2) = eval e locEnv gloEnv store1
              if v <> INT(0) then
                  loop (exec body locEnv gloEnv store2)
              else
                  store2
  
          loop (exec body locEnv gloEnv store)
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i=1;
      do {
          print ++i;
      } while(i<=5);
  }
  ```

- 运行时堆栈：

  

#### do until

- 简介: 类似于C的do until. 先执行一次body中的语句, 符合条件跳出循环.

- 语法分析:

  ```
  | DO StmtM UNTIL LPAR Expr RPAR SEMI  { DoUntil($2, $5)      }
  ```
  
- 抽象语法树:

  ```
  | DoUntil of stmt * expr
  ```

- 语义分析(解释器):

  ```
  | DoUntil (body, e) -> 
          let rec loop store1 =
              let (v, store2) = eval e locEnv gloEnv store1
              if v = INT(0) then 
                  loop (exec body locEnv gloEnv store2)
              else 
                  store2    
  
          loop (exec body locEnv gloEnv store)
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i=1;
      do {
          print ++i;
      } until(i>=5);
  }
  ```

- 运行时堆栈：

  ```c
  
  ```

#### 三目运算符 : 

- 简介: 类似于 C 的三目运算符：i = a > b ? a : b

- 语法分析:

  ```
  | Expr QUEST Expr COLON Expr          { Prim3($1,$3,$5)     }
  ```
  
- 抽象语法树:

  ```
  | Prim3 of expr * expr * expr
  ```

- 语义分析(解释器):

  ```
  | Prim3(e1, e2, e3) ->
          let (v, store1) = eval e1 locEnv gloEnv store
          if v <> INT(0) then eval e2 locEnv gloEnv store1
          else eval e3 locEnv gloEnv store1
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int j = 1 > 2 ? 1 : 2;
      print j;
  }
  ```

- 运行时堆栈：

  ```c
  
  ```

#### +=  -=  *=  /= %=

- 简介: 类似于C. 变量对自身作四则运算.

- 语法分析:

  ```
  | Access PLUSASSIGN Expr              { AssignPrim("+=", $1, $3) }
  | Access MINUSASSIGN Expr             { AssignPrim("-=", $1, $3) }
  | Access TIMESASSIGN Expr             { AssignPrim("*=", $1, $3) }
  | Access DIVASSIGN Expr               { AssignPrim("/=", $1, $3) }
  | Access MODASSIGN Expr               { AssignPrim("%=", $1, $3) }
  ```
  
- 抽象语法树:

  ```
  | AssignPrim of string * access * expr
  ```

- 语义分析(解释器):

  ```
  | AssignPrim(ope, acc, e) ->
          let (loc,store1) = access acc locEnv gloEnv store
          let tmp = getSto store1 loc.pointer
          let (res,store2) = eval e locEnv gloEnv store1
          let num = 
              match ope with
              | "+=" -> 
                  match (tmp) with
                  | INT i -> INT(tmp.int + res.int)
                  | FLOAT i -> FLOAT(tmp.float + res.float)
                  | _ -> failwith ("wrong calu")
              | "-=" -> 
                  match (tmp) with
                  | INT i -> INT(tmp.int - res.int)
                  | FLOAT i -> FLOAT(tmp.float - res.float)
                  | _ -> failwith ("wrong calu")
              | "*=" -> 
                  match (tmp) with
                  | INT i -> INT(tmp.int / res.int)
                  | FLOAT i -> FLOAT(tmp.float / res.float)
                  | _ -> failwith ("wrong calu")
              | "/=" -> 
                  match (tmp) with
                  | INT i -> INT(tmp.int / res.int)
                  | FLOAT i -> FLOAT(tmp.float / res.float)
                  | _ -> failwith ("wrong calu")
              | "%=" -> 
                  match (tmp) with
                  | INT i -> INT(tmp.int % res.int)
                  | FLOAT i -> FLOAT(tmp.float % res.float)
                  | _ -> failwith ("wrong calu")
              | _  -> failwith("unkown primitive " + ope)
          (num, setSto store2 loc.pointer num)
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i=1;
      
      i += 3;
      print i;
  
      i *= 3;
      print i;
  }
  ```

- 运行时堆栈：

  ```c
  
  ```

#### switch-case-default

- 简介: 类似于C. 根据switch中的变量表达式, 找到相同的case值, 若全都不符合, 则执行default中的表达式, 且default表达式必须写在最后.

- 语法分析:

  ```
  | FOR LPAR Expr SEMI Expr SEMI Expr RPAR StmtU { For($3, $5, $7, $9) }
  
  CaseList:
                                          { []                   }
    | CaseDec                             { [$1]                 }
    | CaseDec CaseList                    { $1 :: $2             }
    | DEFAULT COLON StmtM                 { [Default($3)]        }
  
  CaseDec:
      CASE Expr COLON Stmt                { Case($2,$4)          }
  ```
  
- 抽象语法树:

  ```
  | Switch of expr * stmt list
  | Case of expr * stmt
  | Default of stmt 
  ```

- 语义分析(解释器):

  ```
  	| Switch(e, body) ->
          let (v, store0) = eval e locEnv gloEnv store
          let rec carry list = 
              match list with
              | Case(e1, body1) :: next -> 
                  let (v1, store1) = eval e1 locEnv gloEnv store0
                  if v1 = v then exec body1 locEnv gloEnv store1
                  else carry next
              | Default(body) :: over ->
                  exec body locEnv gloEnv store0
              | [] -> store0
              | _ -> store0
  
          (carry body)
  
      | Case (e, body) -> exec body locEnv gloEnv store
      
      | Default(body) -> exec body locEnv gloEnv store
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i=1;
      switch (i){
          case 3: i=121;
          case 2: i=212;
          default: i=333;
      }
      print i;
  }
  ```

- 运行时堆栈：

  ```c
  
  ```

#### 自增 自减

- 语法分析:

  ```
  | Access PREINC                       { Prim4("I++", $1)     } 
  | Access PREDEC                       { Prim4("I--", $1)     } 
  | PREINC Access                       { Prim4("++I", $2)     } 
  | PREDEC Access                       { Prim4("--I", $2)     } 
  ```

- 抽象语法树:

  ```
  | Prim4 of string * access
  ```

- 语义分析(解释器):

  ```
  | Prim4(ope, acc) -> 
          let (loc, store1) = access acc locEnv gloEnv store
          let (i1) = getSto store1 loc.pointer
          match ope with
              | "I++" -> 
                  match (i1) with
                  | INT i -> 
                      let res = INT(i1.int + 1)
                      (i1, setSto store1 loc.pointer res)
                  | FLOAT i -> 
                      let res = FLOAT(i1.float + 1.0)
                      (i1, setSto store1 loc.pointer res)
                  | _ -> failwith ("wrong calu")
              | "I--" -> 
                  match (i1) with
                  | INT i -> 
                      let res = INT(i1.int - 1)
                      (i1, setSto store1 loc.pointer res)
                  | FLOAT i -> 
                      let res = FLOAT(i1.float - 1.0)
                      (i1, setSto store1 loc.pointer res)
                  | _ -> failwith ("wrong calu")
              | "++I"-> 
                  match (i1) with
                  | INT i -> 
                      let res = INT(i1.int + 1)
                      (res, setSto store1 loc.pointer res)
                  | FLOAT i -> 
                      let res = FLOAT(i1.float + 1.0)
                      (res, setSto store1 loc.pointer res)
                  | _ -> failwith ("wrong calu")
              | "--I"-> 
                  match (i1) with
                  | INT i -> 
                      let res = INT(i1.int - 1)
                      (res, setSto store1 loc.pointer res)
                  | FLOAT i -> 
                      let res = FLOAT(i1.float - 1.0)
                      (res, setSto store1 loc.pointer res)
                  | _ -> failwith ("wrong calu")
              | _ -> failwith ("unknown primitive " + ope)
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i=1;
      print ++i;
      print i++;
      print i;
  }
  ```

- 运行时堆栈：

  ```c
  
  ```

#### for-in-range

- 简介: 类似于C. 根据switch中的变量表达式, 找到相同的case值, 若全都不符合, 则执行default中的表达式, 且default表达式必须写在最后.

- 语法分析:

  ```
  | FOR Access IN RANGE LPAR Expr COMMA Expr COMMA Expr RPAR StmtM {ForIn($2, $6, $8, $10, $12)}
  ```

- 抽象语法树:

  ```
  | ForIn of access * expr * expr * expr * stmt
  ```

- 语义分析(解释器):

  ```
  | ForIn (var, e1, e2, e3, body) ->
          let (local_var, store1) = access var locEnv gloEnv store
          let (start_num, store2) = eval e1 locEnv gloEnv store1
          let (end_num, store3) = eval e2 locEnv gloEnv store2
          let (step, store4) = eval e3 locEnv gloEnv store3
  
          let rec loop temp store5 =
              let store_local =
                  exec body locEnv gloEnv (setSto store5 local_var.pointer temp)
  
              if temp.int + step.int < end_num.int then
                  let nextValue = INT(temp.int + step.int)
                  loop nextValue store_local
              else
                  store_local
  
          if start_num.int < end_num.int then
              let intValue = INT(start_num.int)
              loop intValue store4
          else
              store4
  ```

- 语义分析(编译器):

  ```
  
  ```

- 例子:

  ```c
  void main() {
      int i;
      for i in range (2,10,3)
      {
          print i;
      }
  }
  ```

- 运行时堆栈：

  ```c
  
  ```

#### 

## 小组分工

| 姓名   | 学号     | 班级       | 任务       | 权重 |
| ------ | -------- | ---------- | ---------- | ---- |
| 王哲文 | 31801137 | 计算机1803 | 解释器部分 | 0.95 |
| 潘嘉辉 | 31801135 | 计算机1803 | 编译器部分 | 0.95 |

**成员代码提交日志**

![image-20210627235747819](assets/image-20210627235747819.png)

![image-20210627235731115](assets/image-20210627235731115.png)

![image-20210628002639780](assets/image-20210628002639780.png)



## 技术评价

+ 编译器 （在xy_example目录下）

  |     功能     |     对应文件      |  优  |  良  |  中  |
  | :----------: | :---------------: | :--: | :--: | :--: |
  | 变量声明定义 | ex_decAndAssign.c |      |  √   |      |
  |  自增、自减  |   ex_addOrEbb.c   |  √   |      |      |
  |   for循环    |     ex_for.c      |  √   |      |      |
  |  三目运算符  |    ex_prim3.c     |  √   |      |      |
  | switch-case  |    ex_switch.c    |  √   |      |      |
  |   long类型   |     ex_long.c     |  √   |      |      |
  |  double类型  |    ex_double.c    |  √   |      |      |
  |  float类型   |    ex_float.c     |  √   |      |      |

+ 解释器（在myexample目录下）

  | 功能                | 对应文件    | 优   | 良   | 中   |
  | ------------------- | ----------- | ---- | ---- | ---- |
  | 变量初始化          | init.c      | √    |      |      |
  | short/long类型      | short.c     | √    |      |      |
  | char类型            | for.c       | √    |      |      |
  | string类型          | string.c    | √    |      |      |
  | printf/prints       | print.c     | √    |      |      |
  | for                 | for.c       | √    |      |      |
  | do while            | dowhile.c   | √    |      |      |
  | do until            | dountil.c   | √    |      |      |
  | 三目运算符 : ?      | prim3.c     | √    |      |      |
  | +=  -=  *=  /=      | assignPro.c | √    |      |      |
  | switch-case-default | switch.c    |      | √    |      |
  | 自增 自减           | self.c      | √    |      |      |



## 心得体会

+ 王哲文 

  ​        通过该课程的学习，对编程原理有了更深的理解，明白了其内部的运行机制。在本次实验过程中，我深入学习了另一种语言 F# ，并对函数式编程有了崭新的理解，虽然还不明白编译过程中机器是怎么运作的，构建编译器的框架是怎么实现运作的，但通过本课程和大作业的学习与实践，我已经有了突飞猛进的进步，不仅学到了课程中的知识，也学习到了怎么学习一类新的知识的方法。

+ 潘嘉辉

  ​        编译原理实在是一门非常有意思的课程, 但是, 难度略高, 可能是我三年来遇到的最难的一门课. 编译原理非常地抽象, 很难理解, 需要深层次地一点一点慢慢地挖.幸运的是, 我能够在这门课程中学习到程序更深层次的东西, 这能更好地帮助我加深对程序的理解.特别是大作业, 虽然是在前人的基础上进行修改, 但还是学到了很多, 过程虽然比较痛苦, 但能得到一些结果, 还是比较开心的.希望这门课程能对我以后的学习和工作产生帮助吧.





## 参考文档

> http://sigcc.gitee.io/plc2021/#/07/microc.compiler 学习指令
>
> ![image-20210624160420200](assets/image-20210624160420200.png)
>
> + INCSP n
>
>   + n > 0 增长栈 分配空间
>   + n < 0 减少栈 释放空间
>     1. 存完值可能需要缩减栈
>
> + STI i v 
>
>   将s[i]的值变为 v，变之后栈中i消失，留下v
>
> + LDI i
>
>   将s[i]的值取出，变之后i消失，留下s[i]



