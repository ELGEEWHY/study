# lock
## synchronized
synchronized通过以下方式解决线程安全问题：

- 互斥性：同一时刻仅允许一个线程进入被synchronized修饰的代码块或方法。

- 可见性：线程释放锁时，对共享变量的修改会强制刷新到主内存；获取锁时，会从主内存重新加载变量值。

### synchronized 使用方式    
| 使用方式 | 锁对象 | 示例代码 |
| -- | -- | -- |
| 实例方法 | 当前对象（this） | public synchronized void method() { ... } |
| 静态方法 | 类的Class对象 | public static synchronized void method() { ... } |
| 代码块 | 指定任意对象（显式锁） | synchronized (lockObj) { ... } |

上面三种锁法，synchronized 到底锁的是什么？直接上demo：

    // 基本代码如下
    public class SynchronizedDemo {

        public void fun1() {
            System.out.println(Thread.currentThread().getName() + "-111...");
            try {
                TimeUnit.SECONDS.sleep(3L);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "-222...");
        }

        public static void main(String[] args) {
            SynchronizedDemo synchronizedDemo = new SynchronizedDemo();

            new Thread(() -> {
                synchronizedDemo.fun1();
            }).start();
            new Thread(() -> {
                synchronizedDemo.fun1();
            }).start();

        }
    }

    /*
        输出：
        Thread-0-111...
        Thread-1-111...
        等待3s
        Thread-0-222...
        Thread-1-222... 
    */

    //对方法fun1 加synchronized，锁的是实例对象
    public synchronized void fun1() {
        System.out.println(Thread.currentThread().getName() + "-111...");
        try {
            TimeUnit.SECONDS.sleep(3L);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "-222...");
    }

    /*
        输出：
        Thread-0-111...
        等待3s
        Thread-0-222...
        Thread-1-111...
        等待3s
        Thread-1-222...
    */

    //此时将main方法变更为如下形式，两个线程执行方法的对象不同，所以synchronized锁不住
    public static void main(String[] args) {
        SynchronizedDemo synchronizedDemo1 = new SynchronizedDemo();
        SynchronizedDemo synchronizedDemo2 = new SynchronizedDemo();
        new Thread(() -> {
            synchronizedDemo1.fun1();
        }).start();
        new Thread(() -> {
            synchronizedDemo2.fun1();
        }).start();

    }

    /*
        输出：
        Thread-0-111...
        Thread-1-111...
        等待3s
        Thread-0-222...
        Thread-1-222... 
    */

    //将fun1改为静态方法，此时锁的是类
    public static synchronized void fun1() {
        System.out.println(Thread.currentThread().getName() + "-111...");
        try {
             TimeUnit.SECONDS.sleep(3L);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "-222...");
    }

    /*
        上面main方法的输出：
        Thread-0-111...
        等待3s
        Thread-0-222...
        Thread-1-111...
        等待3s
        Thread-1-222...
    */

    //锁代码块场景同理
    public void fun2() {
        synchronized (SynchronizedDemo.class) {
            System.out.println(Thread.currentThread().getName() + "-111...");
        }
    }

    //注意包装类的场景，如 Integer: -128 ~ 127 为常量池，能锁住 128 创建对象后锁不住
    public static void main(String[] args) {

        SynchronizedDemo synchronizedDemo = new SynchronizedDemo();
        new Thread(() -> {
            synchronizedDemo.fun2();
        }).start();
        new Thread(() -> {
            synchronizedDemo.fun2();
        }).start();


    }

    public void fun2() {
        Integer i = 127;
        synchronized (i) {
            System.out.println(Thread.currentThread().getName() + "-111...");
            try {
                TimeUnit.SECONDS.sleep(3L);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "-222...");
        }
    }

    /*
        输出：
        Thread-0-111...
        等待3s
        Thread-0-222...
        Thread-1-111...
        等待3s
        Thread-1-222...
    */

    //锁128
    public void fun2() {
        Integer i = 128;
        synchronized (i) {
            System.out.println(Thread.currentThread().getName() + "-111...");
            try {
                TimeUnit.SECONDS.sleep(3L);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "-222...");
        }
    }
    /*
        输出：
        Thread-0-111...
        Thread-1-111...
        等待3s
        Thread-0-222...
        Thread-1-222... 
    */


    
### synchronized 底层(了解)
**Monitor** 是 JVM 实现同步的核心，每个 Java 对象在底层都关联一个 Monitor 对象（由 C++ 实现，称为 ObjectMonitor）、

    class ObjectMonitor {
        void*     _header;       // Mark Word（存储对象头的拷贝）
        void*     _owner;        // 持有锁的线程
        intptr_t  _count;        // 锁的计数器（可重入性）
        ObjectWaiter* _WaitSet;  // 等待队列（调用 wait() 的线程）
        ObjectWaiter* _EntryList;// 阻塞队列（等待锁的线程）
        // ...
    };
> 锁的竞争流程：
> 尝试获取锁：
>
> 线程通过 CAS（Compare and Swap）操作尝试修改 Mark Word，将锁标志位设置为轻量级锁或偏向锁。
>
> 若成功，直接获取锁；若失败，进入阻塞队列（EntryList）。
>
>锁的升级：
>若轻量级锁竞争失败，锁会膨胀为 重量级锁，线程进入阻塞状态，由操作系统管理线程的唤醒。
>
>锁的释放：
>释放锁时，JVM 会唤醒 EntryList 中的线程重新竞争锁。

JVM 通过以下两条字节码指令实现 synchronized：

- monitorenter：进入同步代码块（尝试获取锁）。

- monitorexit：退出同步代码块（释放锁）。


