### http 内网穿透工具
  
![jdk](https://img.shields.io/badge/jdk-graalvm22.3.1-blue) ![spring boot](https://img.shields.io/badge/spring%20boot-3.0.2-blue)  ![netty](https://img.shields.io/badge/netty-4.1.31.Final-blue "netty")  ![kryo](https://img.shields.io/badge/kryo-5.4.0-blue)  ![GitHub](https://img.shields.io/github/license/fishlikewater/http-pierce)

***通过公网服务器将内网http服务映射到公网，可实现服务端通过统一端口，根据注册名称路由到各客户端，也可实现服务端动态创建http服务与客户端注册服务一一映射***

* 使用方式

> 1、服务端-部署在公网服务器

```properties
#启动服务类型
http.pierce.boot-type=server

#服务端与客户端通信端口
http.pierce.server.transfer-port=8082
#服务端默认http服务端口
http.pierce.server.http-server-port=8081
#服务端监听ip地址
http.pierce.server.address=0.0.0.0
#是否开启日志
http.pierce.server.logger=false
#服务端验证key
http.pierce.server.token=123456
#心跳包检测间隔 单位s
http.pierce.server.timeout=30
```

> 2、客户端端-部署在内网服务器

```properties
#启动服务类型
http.pierce.boot-type=client

#客户端连接服务端配置
#是否开启日志
http.pierce.client.logger=false
#服务端ip地址
http.pierce.client.server-address=127.0.0.1
#服务端端口
http.pierce.client.server-port=8082
#心跳包发送间隔 单位s
http.pierce.client.timeout=30
#服务端验证key
http.pierce.client.token=123456
#服务断开重试间隔 单位s
http.pierce.client.retry-time=30

#mapping 内网服务映射
#内网服务ip
http.pierce.client.http-mappings[0].address=192.168.5.225
#内网服务端口
http.pierce.client.http-mappings[0].port=8088
#内网服务注册服务名
http.pierce.client.http-mappings[0].register-name=etc
#内网服务的url路劲是否包含注册名
http.pierce.client.http-mappings[0].del-register-name=true
#内网服务是否需要在服务端单独开启一个http服务来映射
http.pierce.client.http-mappings[0].new-server-port=false
#new-server-port 为true时该值有效，表示服务端新开的服务端口
http.pierce.client.http-mappings[0].new-port=8083
```