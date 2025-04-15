# RocketMQ
## 基础知识
todo
## 面试场景
### 如何保证消息不丢失
主要是通过**ACK**确认机制保证消息不丢失

发送端：发送端将消息发送到broker，broker返回ACK，
- 如果接收到ACK：发送端确保消息不丢失完成
- 如果没有接收到ACK：发送端根据配置进行重试，如果重试次数内均失败，抛出异常，人工介入处理。

> 重试根据 官方 org.apache,rocketmq.client.producer包中的 DefaultMQProducer 类中的属性去决定重试行为：
> sendMsgTimeout
> retryTimesWhenSendFailed
> retryTimesWhenSendAsyncFaild
> 
发送部分源码：
todo

broker中的顺序由中间件处理

消费端：消费方接收到消息，执行完业务逻辑后，会通过回调向broker发送ACK。

    //具体代码在 Consumer.class 的 start()里，通过注册消息监听者 registerMessageListener,实现回调
    // 5. 注册消息监听器（核心逻辑）
    // 使用MessageListenerConcurrently实现并发消费
    // 如需严格顺序消费，可改用MessageListenerOrderly
    defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
        /**
            * 消息消费回调方法
            * @param msgs 本次拉取的消息列表（默认批量拉取多条）
            * @param context 消费上下文，包含队列偏移量等信息
            * @return 消费状态：
            *   - ConsumeConcurrentlyStatus.CONSUME_SUCCESS：消费成功
            *   - ConsumeConcurrentlyStatus.RECONSUME_LATER：消费失败，稍后重试
            */
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                        ConsumeConcurrentlyContext context) {
            // 遍历处理每条消息
            for (MessageExt msg : msgs) {
                try {
                    // 打印消息基础信息
                    System.out.printf("收到消息: Topic=%s, Tag=%s, MsgId=%s, Body=%s%n",
                            msg.getTopic(),
                            msg.getTags(),
                            msg.getMsgId(),
                            new String(msg.getBody()));

                    // TODO: 实际业务处理逻辑
                    // - 建议做幂等处理（通过msgId或业务唯一键判断是否已处理）
                    // - 避免耗时操作，若处理复杂建议异步执行
                    
                    // 模拟业务处理耗时
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    // 捕获处理异常，记录日志
                    System.err.println("消息处理异常: " + e.getMessage());
                    e.printStackTrace();
                    
                    // 返回RECONSUME_LATER，该消息将会重试
                    // 注意：RocketMQ默认每条消息最多重试16次
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            
            // 全部消息处理成功
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    });



### 保证消息不重复
> MQ本身只保证消费端能成功消费一次，不保证消费消息不重复
解决方案：
- 业务幂等性（推荐，RocketMQ 官方文档提及）

发生的情况：
当消费方执行完逻辑，向Broker发送ACK时，发生宕机，或服务重启，broker获取不到ACK，会重新推送消息，此时会发生消息重复的风险


### 保证顺序消费
> 先总结一下：我认为，想在MQ中保证消息正确性是一件非常困难的事情，可能发生的情况有很多，比如：
> - 本应顺序消费的消息被多个线程发到了不同的队列里（未指定消息组的情况）；
> - 就算指定了消息组，比如通过hashkey的形式，将消息发到指定队列里，但随着队列扩容类似的事件发生，算出来的key可能会发生变化，与老的消息之间顺序依然得不到保障
> - 假如有多个消费端，可能会出现一个消费端先获取到消息，但是卡了，另一个消费端又获取到后续的消息，执行相应的逻辑，所以多消费端的情报，即使保证消息有序，依然不能保证严格的顺序消费
> 
> 结论：消费端直接单消费者，

RocketMQ本身支持顺序消息，发送消息时，可以指定MessageGroup，但需要满足下面两个条件：

- 单一生产者：分布式系统下，即使设置同一个消息组，也无法判定其先后顺序
- 串形发送：无法判定不同线程并行发送消息的先后顺序

指定 MessageQueue 的代码
    
    // DefaultMQProduce.java 中的其中一个send()方法可以指定队列选择器
    public SendResult send (Message msg, MessageQueueSeletor selector, Object arg)
    
    // 其中 MessageQueueSeletor 是一个接口，其中只定义了一个方法
    MessageQueue select(final List<MessageQueue> mqs, final Message msg, final Object arg);

    // 发送消息时指定 MessageGroup（作为参数传递给 MessageQueueSelector）
    SendResult sendResult = producer.send(
        message,
        new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                String messageGroup = (String) arg; // arg 即为 MessageGroup
                int hash = Math.abs(messageGroup.hashCode());
                int index = hash % mqs.size();
                return mqs.get(index);
            }
        },
        "OrderID-20231101-001" // 这里传递用于分组的参数，比如订单id
    );


> 队列信息通过 Java 代码从 NameServer 获取路由信息，并在本地缓存和维护 mqs 列表，具体代码可以看 TopicPublishInfo.java 中
    