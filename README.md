# HelpHands: Reactive System by Lagom

[![stars](https://img.shields.io/github/stars/XiaoQB/HelpHands?style=social&style=flat-square&logoColor=ffffff)]()
[![forks](https://img.shields.io/github/forks/XiaoQB/HelpHands?style=social&style=flat-square&logoColor=ffffff)](https://github.com/doocs/advanced-java/network/members)

>服务提供商为服务消费者提供如保洁、电器维修、虫害控制等家政服务Kumar2018。
-   基础系统包括四类实体：服务消费者、服务提供者、服务和服务订单（数据模型见下图）。
-   服务消费者订阅服务提供者提供的一个或多个服务，服务提供者拥有并以一定的价格为服务消费者提供服务。


![HelpHands-Data Model.png](https://elearning.fudan.edu.cn/users/48843/files/2460933/preview?verifier=MHdziaFEdGfMNEKCDuT1hp90YfEiqtaYHKvyXG7J)

-   功能需求：
    -   服务提供者向系统注册一个或多个服务，这些服务可以由服务消费者订阅。
    -   所有服务都注册了可用的时间段和提供服务的持续时间。
    -   每个服务持续时间和时间段都有一个相关的价格，服务消费者必须支付该价格才能使用该服务。
    -   服务提供商还负责维护系统中服务的可用性状态。
    -   服务消费者可以从一组提供者中搜索可用的服务，并可以选择自己选择的提供者。一旦选择了，服务使用者就可以根据可用性从服务提供者那里安排服务（消息流如下图所示）

![HelpHands-Message Flow.png](https://elearning.fudan.edu.cn/users/48843/files/2460931/preview?verifier=hy3Oh2Ba4dNhkvcdZStAQldymYS5nWydbMWGg4II)

-   非功能属性要求：

    -   所有的实现都必须在GitHub中进行跟踪和版本控制

    -   所有依赖项都必须显式声明

    -   所有服务都必须内置身份验证和授权

    -   必须在外部指定配置，而不是在应用程序中硬编码

    -   应用系统需要体现出柔韧性（resilience）和弹性（elasticity）

    -   必须记录所有事件以了解应用程序的状态并在生产中监视它，最好有可视化大屏显示能力

-   体系结构要求：该原型系统必须是反应式系统（Reactive System）Kuhn2017，上述主体产生的异步事件会得到及时响应和处理，系统具备横向扩展能力以及容错能力
