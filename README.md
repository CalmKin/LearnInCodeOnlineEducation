## 项目介绍
本项目是一个面向大众的编程学习在线教育平台，通过接入第三方教学机构向用户提供在线课程。项目基于B2B2C的业务模式，培训机构可以在平台入驻、发布课程，运营人员对发布的课程进行审核，审核通过后课程才可以发布成功，课程包括免费和收费两种形式，对于免费课程可以直接选课学习，对于收费课程在选课后需要支付成功才可以继续学习。形成了机构发布、用户选课、支付、在线学习的业务闭环




## 开发工具版本
| **开发工具** | **版本号** | **安装位置** |
| --- | --- | --- |
| IntelliJ-IDEA | 2021.x以上版本 | 个人电脑 |
| JDK | 1.8.x | 个人电脑 |
| Maven | 3.8.x以上版本 | 个人电脑 |
| Git | 2.37.x | 个人电脑 |
| VMware-workstation | 16.x | 个人电脑 |
| CentOS | 7.x | 虚拟机 |
| Docker | 18.09.0 | 虚拟机 |
| Mysql | 8.x | docker |
| nacos | 1.4.1 | docker |
| rabbitmq | 3.8.34 | docker |
| redis | 6.2.7 | docker |
| xxl-job-admin | 2.3.1 | docker |
| minio | RELEASE.2022-09-07 | docker |
| elasticsearch | 7.12.1 | docker |
| kibana | 7.12.1  | docker |
| gogs | 0.13.0 | docker |
| nginx |  1.12.2 | docker |



## 整体业务流程

本项目主要包括三类用户角色：学生、教学机构的老师、平台运营人员，核心业务流程包括课程发布流程、选课学习流程。

### 课程编辑与发布

课程发布流程：
1、教学机构的老师登录教学管理平台，编辑课程信息，发布自己的课程。
2、平台运营人员登录运营平台审核课程、视频等信息，审核通过后课程方可发布。
![image](https://github.com/CalmKin/LearnInCodeOnlineEducation/assets/87215319/8c355a19-86cf-4dfd-88bc-54eef60b4eba)

### 用户选课、在线学习

免费课程可直接学习，收费课程需要下单购买。
学生选课流程如下：
![image](https://github.com/CalmKin/LearnInCodeOnlineEducation/assets/87215319/91589cbd-b778-4ca4-af11-01df203ca523)

## 关键部分逻辑

### 断点续传

![](https://calmkin-blog-markdown-note.oss-cn-hangzhou.aliyuncs.com/Typora/imgs202403142151373.png)

### 视频处理

![](https://calmkin-blog-markdown-note.oss-cn-hangzhou.aliyuncs.com/Typora/imgs202403142152288.png)

### 课程发布

![](https://calmkin-blog-markdown-note.oss-cn-hangzhou.aliyuncs.com/Typora/imgs202403142152700.png)

### 添加选课

![](https://calmkin-blog-markdown-note.oss-cn-hangzhou.aliyuncs.com/Typora/imgs202403142152284.png)

### 扫码支付

![](https://calmkin-blog-markdown-note.oss-cn-hangzhou.aliyuncs.com/Typora/imgs202403142152540.png)





