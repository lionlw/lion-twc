# lion-twc

## 特点
基于netty开发，实现client/server数据双向通信，同时提供manage与所有server交互，对client进行消息统一发送。
使用场景：分布式集群应用的双向数据通信。

## 说明

### client
客户端，可以理解为分布式集群应用的工作节点。


### server
服务端，可以理解为分布式集群应用的控制节点，比如leader。


### management
管理端，某些分布式集群应用，需要有个统一的控制端，向server或者client发送指令消息。此时可通过集成管理端来实现。

