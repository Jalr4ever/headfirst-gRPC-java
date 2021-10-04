# 前言

gRPC 是我学习的第一个 rpc 协议，前期理解也踩了不少坑，当时想着如果有个特别简单的 head-first 项目 download 下来直接就能跑就好了，于是有了这个工程

# 工程结构

工程基于 Maven 3.6 管理，含有四个 module：

- simple rpc
- server-side-streaming-rpc
- client-side-streaming-rpc
- bidirectional-streaming-rpc

四个 module 分别对应 gRPC 通信的四种模式，但注意每个 module 没有什么关联，相对独立

# 编译

可以对业务 module 分别进行 maven clean compile 使用，也可以对根路径直接编译
