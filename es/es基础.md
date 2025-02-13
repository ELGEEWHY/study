# es基础
## Lucene 全文搜索引擎
**分词->构建倒排索引->提供基本搜索功能**，可以理解为单机版的搜索引擎，提供基本的读写功能。写过程就是建索引，而读过程就是利用索引（倒排索引）结构高效检索的过程。

es基于lucene，进一步封装、抽象，通过 **shard** 实现分布式存储和搜索

## Elasticsearch基本概念
### 基于lucene的分布式近实时搜索平台
> 近实时原因：ES 通过异步机制处理数据的写入和索引，当提交一个索引时，数据会先写入内存缓冲区，然后通过 refresh 操作，生成一个 segment 将数据从内存缓冲区刷新到**文件系统缓存**，此时，才能检索到数据，之后会通过 fsync 操作落盘。
> 
> segment： Lucene 中的一个倒排索引文件，包含了文档的数据和索引信息
> 
> refresh：有es中的配置 refresh_interval，默认 1s 刷新一次，也会受到缓冲区大小影响

### 基本概念与关系型数据库对比
| Elasticsearch | MySQL |
| :--: | :--: |
| 索引(index) | 数据库 |
| 类型(type) | 数据表 |
| 文档(doc) | 一行记录 |
| 映射(mapping) | 表结构 |

- es7后弃用 type，仅用 _doc 作为一个索引的类型，因为许多 es 的设计是按照关系型数据库的思路设计的(如上表)，但是 MySQL 中数据表之间是相互独立的，而不同的 type 均依赖一个索引，由于 lucene 底层原因，会影响存储效率，具体如下文章
  CSDN：[一文读懂 elasticsearch 版本升级 type 的变化](https://blog.csdn.net/microGP/article/details/112793859)


### 倒排索引
分词对文档id的映射

    # deepseek生成的demo
    docs = [
        "hello world", 
        "python demo",
        "hello python"
    ]
    倒排索引：
    hello: [0, 2]
    world: [0]
    python: [1, 2]
    demo: [1]
    搜索 'hello': [0, 2]
    搜索 'python': [1, 2]
    搜索 'hello python': [0, 1, 2]




