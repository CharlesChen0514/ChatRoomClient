# ChatRoomClient


## 功能展示

### 客户端启动

```
java -jar .\ChatRoomClient.jar
```

![StartServer.png](figs/startClient.png)

### 查看个人信息

```
-i
```

![StartServer.png](figs/info.png)

### 连接用户

```
-c@2257   // 默认连接本地 ip
-c@local@2257    // 连接本地 ip 的 2257 端口
-c@192.168.5.1@2257   // 连接 192.168.5.1 的 2257 端口
```

![StartServer.png](figs/connect.png)

### 查看好友

```
-fs
```

![StartServer.png](figs/friends.png)

### 私信用户

```
-pm@lele@ni hao   // 给好友 lele 发送 ni hao 消息
```

发送端

![StartServer.png](figs/pmSend.png)

接收端

![StartServer.png](figs/pmReceive.png)


### 文件传输

```
-ft@lele@D:\SOCKET(2).txt   // 给好友 lele 发送文件 D:\SOCKET(2).txt
```

发送端

![StartServer.png](figs/ftSend.png)

接收端

![StartServer.png](figs/ftReceive.png)

### 删除好友

```
-dc@chen   // 删除好友 chen
```

![StartServer.png](figs/delete.png)

### 设置允许的最大文件大小

```
-mfs@5000000   // 设置允许接收的最大文件大小为 5000000
```

![StartServer.png](figs/mfs.png)

### 查看接收的文件列表

```
-af
```

![StartServer.png](figs/af.png)

### 命令提示

```
-h
```

![StartServer.png](figs/help.png)

### 退出

```
-q
```

![StartServer.png](figs/quit.png)
