# 一、前言

gRPC 是我学习的`第一个 rpc 协议`，前期理解也踩了不少坑，当时想着如果有个特别简单的 head-first 项目 download 下来直接就能跑就好了，于是有了这个工程

本工程可以帮助你快速入门 gRPC 协议在 java 的基本编写和实现。git clone、编译、运行成功，理解与入门。就这么简单

# 二、介绍

工程基于 Maven 3.6 管理，含有四个 module：

- simple rpc：功能场景为 「用户登录」，用户登录，并获取成功与否的结果
- server-side-streaming-rpc：功能场景为 「数据的流式下载」，文件数据流式下载
- client-side-streaming-rpc：功能场景为「数据的流式上传」，文件数据流式上传
- bidirectional-streaming-rpc：功能场景为「数据流式脱敏」，将原始数据流式上传的服务端，并流式获取脱敏数据的响应

四个 module 分别对应 gRPC 通信的四种模式，但注意每个 module 没有什么关联，相对独立

# 三、如何使用

## 编译

本工程需要手动编译

- git clone
- 进入工程的根目录运行类似命令：`mvn clean package -Dmaven.test.skip=true --settings $M2_HOME/conf/settings_aliyun.xml`
- 将命令中的 `$M2_HOME/conf/settings_aliyun.xml` 替换为你自己使用的 maven 配置文件路径即可

## 运行

四个子工程都写了相应的单元测试，编译完成，直接运行子模块对应的单元测试即可看到 client 与 server 交互的过程与结果

# 四、后言

最近两个月的工作是使用 gRPC 协议构建我们自己的应用层协议，支撑业务的通信

算是把 gRPC 在 Java 该有的内容都摸了一遍，流量控制，gRPC 反向代理等，总体来说 gRPC 是一款优秀的协议，不过在做大流量的通信的时候，对外提供的处理机制并不是特别友好，因此可能注意会有 `Back Pressure` 问题，需要对此机制做对应的 `Flow Control`

希望本工程对你有帮助

