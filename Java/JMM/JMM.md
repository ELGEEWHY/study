# JMM
屏蔽硬件和操作系统的内存访问差异，实现让Java在各平台都能实现一致的并发效果 (类似 JVM 跨平台的特性)
> 线程之间没法直接交 互数据 
## 底层原子操作
- read：从主存读数据
- load：从主存读数据写入工作内存
- use：从工作内存中取数据计算
- assign：将新值重新赋值到工作内存中
- store：将工作内存数据写入主存（还未修改原值）
- write：将store过去的变量赋给主存中的变量
- lock：将主存变量加锁，标识为线程独占状态
- unlock：主存变量解锁，解锁后可以锁定该变量

![Img](JMM_model.png)


## 并发编程三特性
可见性、有序性、原子性 
> 有序性解读：
> 计算机为了提升执行效率，在遵循一定原则的情况下，会对代码指令进行重排序重排序，Java 语言规范（官方文档）中提及的重排序必须遵循的原则如下：
> 
> as-if-serial（保证一致性）：不管怎么重排序（编译器和处理器为了提高并行度），单线程程序的执行结果不能被改变，即不会对存在数据依赖关系的操作做重排序，不考虑多线层的情况，情况过于复杂
> 
> happens-before：规定了一系列原则，辅助保证程序执行的原子性，规定了某些代码必须发生在某些代码之前
> 1. 程序顺序原则
> 2. 锁规则
> 3. volatile原则
> 4. 线程启动规则
> 5. 传递性
> 6. 线程终止规则
> 7. 线程中断规则
> 8. 对象终结规则

重排序 DEMO
DCL创建单例在高并发环境下的问题，出现概率极小

    # dcl 双重校验锁创建单例，示例代码如下
    public class Singleton {  
        private static Singleton singleton;  
        private Singleton (){}  
        public static Singleton getSingleton() {  
        if (singleton == null) {  
            synchronized (Singleton.class) {  
                if (singleton == null) {  
                    singleton = new Singleton();  
                }  
            }  
        }  
        return singleton;  
        }  
    }

    #其中jvm指令为 （来源于 deepseek）
    .method public static getSingleton()LSingleton;
        .limit stack 2
        .limit locals 2

        // 第一次检查（非同步）
        getstatic Singleton/singleton LSingleton;
        ifnonnull L0

        // 进入同步块
        ldc LSingleton;.class
        astore 1          ; 存储Class对象到局部变量1
        monitorenter      ; 获取锁 synchronized 代码块部分开始执行

        // 第二次检查（同步块内）
        getstatic Singleton/singleton LSingleton;
        ifnonnull L1

        // 创建新实例
        new Singleton
        dup

        // ******** 可能出现问题的代码 ********
        invokespecial Singleton/<init>()V     // 初始化
        putstatic Singleton/singleton LSingleton;  // 把初始化好的对象内存地址赋值给静态变量
        // ******** end ********

        // L1: ; 释放锁标签
        aload 1           ; 加载存储的Class对象
        monitorexit       ; 释放锁
        goto L0           ; 跳转到返回

        ; 异常处理部分（保证锁释放）
        astore 2          ; 存储异常对象
        aload 1
        monitorexit
        aload 2
        athrow

        // L0: ; 返回标签
        getstatic Singleton/singleton LSingleton;
        areturn
    .end method

    上面可能出现的代码中，计算机可能会进行指令重排序，高并发情况下，会出现对象半初始化问题：
    1. 重排序后 先执行 putstatic 指令后，导致 synchronized 代码中的对象 ‘singleton’ 已经有了内存地址
    2. 其他线程获取 ‘singleton’ 时，发现不为null，直接返回，但并未执行初始化方法，导致返回的实例对象的值与预期不符


    解决方案：
    申明 singleton 时加 volatile 关键字，添加内存屏障，防止指令重排序
    private volatile static Singleton singleton; 

## volatile
 底层本质缓存一致性协议 (MESI 协议 intel处理器，了解即可)
 > MESI：多个cpu从主存读同一个数据到各自的高速缓存，当其中某个cpu修改了缓存里的数据，该数据会马上同步回主存，其他cpu通过**总线嗅探机制**可以感知到数据的变化从而将自己缓存里的数据失效

底层汇编：添加**lock前缀指令**（汇编代码，不是上面那个原子操作）生效，会锁定这块内存区域的缓存（缓存行锁定）

- 会将当前处理器缓存行的数据立即写回主存
- 引发其他cpu里缓存该内存地址的数据无效（MESI协议）
- 提供内存屏障功能，使 lock 前后指令不能重排序
- 保证**可见性、有序性**，不保证原子性

### 内存屏障 （lock前缀指令，硬件级别实现）
思想：自定义一段标记代码/标签，禁止计算机进行指令重排序
Java 规范定义的内存屏障

| 屏障类型 | 指令类型 | 说明 |
| -- | -- | -- |
| LoadLoad | Load1;LoadLoad;Load2 | 保证操作2执行前，操作1执行完毕 |
| StoreStore | Store1;LoadLoad;Store2 | 保证操作2执行前，操作1执行完毕 |
| LoadStore | Load1;LoadLoad;Store2 | 保证操作2执行前，操作1执行完毕 |
| StoreLoad | Store1;LoadLoad;Load2 | 保证操作2执行前，操作1执行完毕 |




