# JavaWeb-CanteenSystem
USST JavaWeb大作业：上海理工大学食堂点评交流社区

> **Warning** 重要：关于内网前端开发时无法获取后端Cookie的解决办法

由于本地开发时，很可能发生与后端服务器域名不一致的情况，在这种情况下，就会发生跨域问题，导致Cookie无法正常设置。因此，强烈建议使用`frpc`端口穿透服务将你的开发环境映射至公共开发服务器上。相关配置方法如下：

1. 下载[frpmgr](https://github.com/koho/frpmgr/releases/tag/v1.15.1)
2. 新建客户端，配置名称随意，服务端地址`192.168.19.10`，端口`7000`，**`TLS`关闭**，其余默认即可。
3. 在新建的配置内，点击`添加`，新增一个需要转发的端口，本地地址`127.0.0.1`，本地端口为你的前端项目端口（例如`3000`等），远程端口可以为`60001`至`61000`之间的任意端口，不过请勿与他人的重复。
4. 配置完毕后运行服务，访问`192.168.19.10:xxxxx`(xxxxx为选择的远程端口)，测试访问是否正常。

例如：目前已经绑定的端口有`60001`，它是由`127.0.0.1:8080`映射而来的，请求时，则请求`192.168.19.10:60001`即可。


## 系统架构

前后端分离，2个前端 + 1个后端。

### 前端1：点评交流社区前端

适用普通用户，使用`react`开发。

### 前端2：管理员后台前端

适用于系统管理员和食堂管理员，使用`vue`开发，根据不同角色，渲染相应的管理功能。

### 后端

后端使用Java开发，使用分层架构。数据库使用`Redis`和`Mysql`。

## 目录结构

- `canteen_community.sql`：数据库表结构文件
- JavaBackend：Java后端项目目录
- CommunityFrontend: 社区前端
- AdminFrontend: 管理员后台前端
- nginx: Nginx配置文件
- mariadb: 数据库文件映射目录（保留目录）
- minio-autoscript: Minio自动文件处理脚本

## 开发环境

- 内网开发数据库：`192.168.19.2:3306`，用户名：`root`，密码：`password`
- 内网开发请使用`Wireguard`连接至开发环境。

## 部署方案

- 使用`Docker-Compose`部署，每一个子项目都应当有独立的`Dockerfile`。

## 文件存储服务

文件存储服务使用基于`lua`脚本编写的`openresty-file-server`，代码见：[openresty-file-server](https://github.com/vvbbnn00/openresty-file-server)
