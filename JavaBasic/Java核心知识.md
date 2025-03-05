# Java核心知识
## 基本数据类型
### 八种基本数据类型
- 6 种数字类型：
    - 4 种整数型：byte、short、int、long
    - 2 种浮点型：float、double
- 1 种字符类型：char
- 1 种布尔型：boolean。

### 基本类型 与 包装类型区别
- 用途：除了定义一些常量和局部变量之外，我们在其他地方比如方法参数、对象属性中很少会使用基本类型来定义变量。并且，包装类型可用于泛型，而基本类型不可以。
- 存储方式：基本数据类型的局部变量存放在 Java 虚拟机栈中的局部变量表中，基本数据类型的成员变量（未被 static 修饰 ）存放在 Java 虚拟机的堆中，被 static 修饰 JDK 1.7 及之前位于方法区，1.8 后存放于元空间 。包装类型属于对象类型，我们知道几乎所有对象实例都存在于堆中。
- 默认值：成员变量包装类型不赋值就是 null ，而基本类型有默认值且不是 null。
- 比较方式：对于基本数据类型来说，== 比较的是值。对于包装数据类型来说，== 比较的是对象的内存地址。所有整型包装类对象之间值的比较，全部使用 equals() 方法。

### 包装类的缓存机制
Byte,Short,Integer,Long 这 4 种包装类默认创建了数值 [-128，127] 的相应类型的缓存数据，character 创建了数值在 [0,127] 范围的缓存数据，Boolean 直接返回 true / false,超过缓存范围创建新的对象。

浮点类型包装类 Float、Double 没有缓存机制

### 自动装箱、拆箱
Java 1.5 引入

    # 自动装箱
    Integer i = 40;
    # 相当于编译器自动做以下语法编译
    Integer i = Integer.valueOf(40);

    # 自动拆箱
    Integer i = 100;
    int k = i;
    # 相当于编译器自动做以下语法编译
    int k = i.intValue();

### 字符型常量、字符串常量
- 形式 : 字符常量是单引号引起的一个字符，字符串常量是双引号引起的 0 个或若干个字符。
- 含义 : 字符常量相当于一个整型值( ASCII 值),可以参加表达式运算; 字符串常量代表一个地址值(该字符串在内存中存放位置)。
- 占内存大小：字符常量只占 2 个字节; 字符串常量占若干个字节。
 
⚠️ char 类型是 16 位无符号整数（占用 2 字节），用于表示 UTF-16 编码的 Unicode 字符

- Unicode 字符集：为全球所有字符分配唯一编号（称为 代码点，如 U+4E2D 表示汉字“中”）。

- UTF-16 编码：将 Unicode 代码点转换为具体的二进制存储格式。

    - 基本多文种平面（BMP）：代码点范围 U+0000 到 U+FFFF，可直接用 1 个 char 存储（如大多数常用汉字，常用汉字编码 U+4E00 到 U+9FFF）。

    - 补充平面（如表情符号、部分生僻汉字）：代码点范围 U+10000 到 U+10FFFF，需要用 2 个 char 组成的代理对 表示。

            public static void main(String[] args) throws       Exception {
                char a = '刘';
                System.out.println(a);
                System.out.println((int) a);
                System.out.println((char) 21016);
                //char b = '\uD840\uDC00'   char 无法表示 𠀀 字
                //𠀀   U+20000
                String s = "\uD840\uDC00";
                System.out.println(s);
                //输出131072  U+20000的十进制形式
                System.out.println(s.codePointAt(0));
            }

![上方代码输出结果](/pic/char_demo.jpg)
            

## 面向对象
### 封装、继承、多态
