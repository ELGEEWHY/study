# JVM 入门

## 1.内存结构

### 1.1 jvm运行时数据区



![数据区](jvm_storage.png)

1. 方法区

- 存储已被Java虚拟机加载的***类信息***、***常量***、***静态变量***、即时编译器编译后的代码等数据；

- 无法满足内存分配需求时，抛 **OutOfMemoryError异常**

  

2. 堆区

- 分配的内存最大，Java虚拟机规范中的描述：所有的***对象实例***以及***数组*** 都要在堆上分配;

- 垃圾收集器管理的主要区域，分为 **新生代**、**老年代**;
    > - 默认比例
    > 新生代（Young Generation）：约占堆内存的 1/3
    > 老年代（Old Generation）：约占堆内存的 2/3
    > - 配置参数
    > -XX:NewRatio=N
    > 定义老年代与新生代的比例（默认 N=2，即 老年代:新生代=2:1）。
    > 示例：-XX:NewRatio=3 表示老年代占 3/4，新生代占 1/4
    > - 新生代中的分配
    > eden：约占 80%，新对象优先分配在eden区
    > survivor：from + to 各占 10%
    > -XX:SurvivorRatio=N
    > 定义 Eden 区与单个 Survivor 区的比例（默认 N=8，即 Eden:Survivor=8:1）。
    > 示例：-XX:SurvivorRatio=4 表示 Eden 占新生代的 80%，每个 Survivor 占 10%（总和为 20%）。


- 可通过配置 -Xmx 和 -Xms 控制

  ![](dockerFile_xmx_xms.png)

  - xmx:jvm可分配的最大堆内存
  - xms:jvm分配的初始堆内存

- 无可分配内存且无法再扩展时，抛 **OutOfMemoryError 异常**



3. 程序计数器(PC)

- 计算下一条需要执行的指令的地址；
- 没有规定 ***OutOfMemoryError*** ；



4. 虚拟机栈

- 方法执行时，创建栈帧（编译器用来实现方法调用所用的一种数据结构，包含局部变量、操作数栈、出口、动态链接等内容）
- 如果线程请求的栈深度大于虚拟机所允许的深度，将抛出 ***StackOverflowError*** 异常，创建栈帧若无法申请到足够内存时，抛 ***OutOfMemoryError 异常***



5. 本地方法栈

- 管理**本地方法**调用

  - 本地方法（非java语言实现，当接口使用即可）:

    - 追求效率
    - java不易实现
    - 与OS交互
    - 与java环境外交互
    - ......

    示例：


![image-20240818185744901](native_method.jpg)



### 1.2 直接内存
JVM 外直接向操作系统申请的一部内存，主要目的是**提升IO读写性能**，且垃圾回收不管理直接内存，可以**避免GC干扰**

- 引入了一种基于 **通道(Channel)** 与 **缓冲区（Buffer）** 的I/O 方式，**使用 native 函数库 **直接分配数据区外内存，然后通过**DirectByteBuffer** 对象作为这块内存的引用进行操作，图示如下:

![直接内存](direct_storage_example.jpg)
- 使用场景
    - NIO（New IO）：Java NIO 提供的 ByteBuffer.allocateDirect() 允许分配直接内存，避免 I/O 复制。
    - Netty：高性能网络框架 Netty 也大量使用直接内存来优化网络通信。
    - 数据库驱动（如 MySQL JDBC Driver）：某些数据库驱动（如 MySQL Connector）会使用直接内存加速数据传输。

- 与数据区区别:

  - 直接内存申请空间耗费很高的性能，堆内存申请空间耗费比较低
  - 直接内存的IO读写的性能要优于堆内存，在多次读写操作的情况相差非常明显
  - 不受GC影响，使用时需要手动释放内存

  示例代码 （同时展示直接内存操作方式）:

  ```
  package com.lijie;
  
  import java.nio.ByteBuffer;
  
  /**
   * 直接内存 与 堆内存的比较
   */
  public class ByteBufferCompare {
  
      public static void main(String[] args) {
          allocateCompare();   //分配比较
          operateCompare();    //读写比较
      }
  
      /**
       * 直接内存 和 堆内存的 分配空间比较
       */
      public static void allocateCompare() {
          int time = 10000000;    //操作次数
          long st = System.currentTimeMillis();
          for (int i = 0; i < time; i++) {
  
              ByteBuffer buffer = ByteBuffer.allocate(2);      //非直接内存分配申请
          }
          long et = System.currentTimeMillis();
          System.out.println("在进行" + time + "次分配操作时，堆内存：分配耗时:" + (et - st) + "ms");
          long st_heap = System.currentTimeMillis();
          for (int i = 0; i < time; i++) {
              ByteBuffer buffer = ByteBuffer.allocateDirect(2); //直接内存分配申请
          }
          long et_direct = System.currentTimeMillis();
          System.out.println("在进行" + time + "次分配操作时，直接内存：分配耗时:" + (et_direct - st_heap) + "ms");
      }
  
      /**
       * 直接内存 和 堆内存的 读写性能比较
       */
      public static void operateCompare() {
          //如果报错修改这里，把数字改小一点
          int time = 1000000000;
          ByteBuffer buffer = ByteBuffer.allocate(2 * time);
          long st = System.currentTimeMillis();
          for (int i = 0; i < time; i++) {
              buffer.putChar('a');
          }
          buffer.flip();
          for (int i = 0; i < time; i++) {
              buffer.getChar();
          }
          long et = System.currentTimeMillis();
          System.out.println("在进行" + time + "次读写操作时，堆内存：读写耗时：" + (et - st) + "ms");
          ByteBuffer buffer_d = ByteBuffer.allocateDirect(2 * time);
          long st_direct = System.currentTimeMillis();
          for (int i = 0; i < time; i++) {
              buffer_d.putChar('a');
          }
          buffer_d.flip();
          for (int i = 0; i < time; i++) {
              buffer_d.getChar();
          }
          long et_direct = System.currentTimeMillis();
          System.out.println("在进行" + time + "次读写操作时，直接内存：读写耗时:" + (et_direct - st_direct) + "ms");
      }
  }
  
  // -----------------------------结果---------------------------------
  //在进行10000000次分配操作时，堆内存：分配耗时:98ms
  //在进行10000000次分配操作时，直接内存：分配耗时:8895ms
  //在进行1000000000次读写操作时，堆内存：读写耗时：5666ms
  //在进行1000000000次读写操作时，直接内存：读写耗时:884ms
  
  ————————————————
  原文链接：https://blog.csdn.net/weixin_43122090/article/details/105093777
  ```


## 2.垃圾回收
G1 垃圾回收器为例

### 1. 垃圾回收概述
- **目标**：自动回收堆内存中不再使用的对象，避免内存泄漏。
- **核心思想**：基于对象的存活时间差异，采用**分代回收策略**（新生代高频回收，老年代低频回收）。

---

### 2. 分代回收机制
JVM 堆内存划分为三个区域，针对不同区域采用不同的回收算法：

| 区域         | 存储对象              | 回收算法           | 触发条件          |
|--------------|-----------------------|--------------------|-------------------|
| **新生代**   | 新创建的对象          | 复制算法           | Eden 区空间不足   |
| - Eden 区    | 对象首次分配的位置    |                    |                   |
| - Survivor区 | 存活对象的中转站      | （From 和 To 交替）|                   |
| **老年代**   | 长期存活的对象        | 标记-清除-整理算法 | 老年代空间不足    |

---

### 3. Minor GC（年轻代回收）
#### 触发条件
- **Eden 区空间不足**时触发。

#### 回收过程
1. **标记存活对象**  
   - 从 **GC Roots**（如线程栈引用、静态变量）出发，标记所有存活对象。
2. **复制到 Survivor 区**  
   - 将 **Eden 区**和当前使用的 **From Survivor 区**中的存活对象复制到 **To Survivor 区**。  
   - 存活对象的年龄（Age）**+1**，并清空 Eden 和 From 区。
3. **Survivor 区交换**  
   - 复制完成后，To Survivor 区变为下一次 GC 的 From Survivor 区。
4. **晋升老年代**  
   - 对象年龄阈值：若对象年龄 **≥阈值（默认15）**，或 Survivor 区空间不足，对象直接进入老年代。
   - 动态年龄判断：若某年龄的对象总大小超过 survivor 区的50%，则所有 **≥该年龄** 的对象会直接晋升（即使未达阈值）
   
   > 空间分配担保（Handle Promotion）
在 Minor GC 发生前，JVM 会执行空间分配担保检查，确保老年代有足够空间容纳可能晋升的对象：

检查老年代剩余空间是否 ≥ 新生代所有对象总大小（即极端情况：所有存活对象都晋升）。

若满足，则允许执行 Minor GC。

若不满足，则检查是否开启 -XX:-HandlePromotionFailure（JDK 6 Update 24 后此参数失效，默认启用担保机制）。

二次检查老年代剩余空间是否 ≥ 历次晋升到老年代对象的平均大小。

若满足，则冒险执行 Minor GC（存在 Full GC 风险）。

若不满足，则直接触发 Full GC（回收老年代后再执行 Minor GC）。



---

### 4. Full GC（全局回收）
#### 触发条件
- **老年代空间不足**（可能由晋升对象过多或大对象分配导致）。
- 显式调用 `System.gc()`（不推荐，可能被 JVM 忽略）。

#### 回收过程
1. **标记存活对象**  
   - 遍历整个堆内存，标记所有存活对象。
2. **清除死亡对象**  
   - 回收未标记的内存区域（即死亡对象占用的空间）。
3. **内存整理**（可选）  
   - 使用 **标记-整理（Mark-Compact）** 算法，将存活对象向内存一端移动，消除内存碎片。

---

### 5. 关键特点与优化
#### 1. Stop-The-World（STW）
- GC 过程中会暂停所有应用线程，不同 GC 器优化目标不同：  
  - **Serial GC**：单线程，STW 时间长。  
  - **Parallel GC**：多线程，吞吐量优先。  
  - **G1/ZGC**：并发标记，STW 时间可控。

#### 2. 空间分配担保
- 执行 Minor GC 前，JVM 检查老年代剩余空间是否足够容纳**所有新生代对象**。  
  - 若不足，则直接触发 **Full GC**。

#### 3. 分代回收优势
- **新生代**：对象存活率低，复制算法高效（仅复制存活对象）。  
- **老年代**：对象存活率高，标记-清除/整理算法减少复制开销。

---

### 6. 常见 GC 日志解读
```plaintext
[GC (Allocation Failure) [PSYoungGen: 8192K->1008K(9216K)] 8192K->2000K(19456K), 0.005 secs]

- PSYoungGen：Parallel Scavenge 收集器的新生代回收。

- 8192K->1008K：回收后新生代占用从 8192K 降至 1008K。

- 8192K->2000K：整个堆内存占用从 8192K 降至 2000K。
```
































