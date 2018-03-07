# 自制HTTP服务器（包括HTTP服务器和Servlet容器）
## 具备的功能(均为简化版的实现)：

- HTTP Protocol
- Servlet
- ServletContext
- Request
- Response
- Dispatcher
- Static Resources & File Download
- Error Notification
- Get & Post & Put & Delete
- web.xml parse
- Forward
- Redirect
- Simple TemplateEngine
- session
- cookie

## 使用技术

基于Java BIO、多线程、Socket网络编程、XML解析、log4j/slf4j日志
只引入了junit,lombok(简化POJO开发),slf4j,log4j,dom4j(解析xml),mime-util(用于判断文件类型)依赖，与web相关内容全部自己完成。

## 打包
- 必须使用maven的assembly插件，它可以把依赖的jar包打进来并且解压
- 需要指定resources/webapp等，把除了源码之外的资源文件包含进来
- class.getResource方法不推荐使用，因为在jar包中的文件路径是有空格的，但是getResource方法得到的是URL，是没有空格的。如果一定要在jar包中使用getResource，那么必须将URL中的文件路径中的%20替换为空格`getClass().getResource("/a.txt").getPath().replaceAll("%20", " ")``
- 或者直接使用getResourceAsStream方法，可以避免这个问题

## BIO
一个Acceptor阻塞式获取socket连接，然后线程池阻塞式等待socket读事件，处理业务逻辑，最后写回
每个HTTP连接结束后由客户端关闭TCP连接

20个线程，每个线程循环访问100次，吞吐量为205个请求/sec，平均响应时间为2ms（tps：2ms/transaction）



## NIO Reactor
多个（1个或2个）Acceptor阻塞式获取socket连接，然后多个Poller（处理器个数个）非阻塞式轮询socket读事件，检测到读事件时将socket交给线程池处理业务逻辑
实现HTTP的keep-alive

20个线程，每个线程循环访问100次，吞吐量为206个请求/sec，平均响应时间为2ms（tps：2ms/transaction）


## 未来希望添加的功能：
- NIO实现多路复用
- 手写WebSocket服务器，实现HTTP长连接
- Filter
- Listener


## 另附CSDN相关博客
http://blog.csdn.net/songxinjianqwe/article/details/75670552