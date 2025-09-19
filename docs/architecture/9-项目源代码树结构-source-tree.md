# 9. 项目源代码树结构 (Source Tree)

这是我们基于Nx Monorepo的最佳实践设计的项目文件夹结构。

```
/
├── apps/                           # 存放可独立部署的应用
│   ├── backend/                    # 后端Java Spring Boot应用
│   │   ├── src/main/java/com/shanergy/bprev/
│   │   │   ├── BprevApplication.java
│   │   │   ├── common/             # 对应 core-common
│   │   │   ├── config/             # 应用配置
│   │   │   ├── controller/         # API控制器 (表现层)
│   │   │   ├── integration/        # 集成层 (int-monitoring, int-notification 等)
│   │   │   ├── model/              # 数据实体
│   │   │   ├── repository/         # 数据仓库 (持久层)
│   │   │   ├── security/           # 对应 core-security
│   │   │   └── service/            # 业务逻辑层 (存放各业务组件实现)
│   │   └── pom.xml
│   ├── frontend-pc/                # 前端PC Vue.js应用
│   │   ├── src/
│   │   │   ├── assets/
│   │   │   ├── components/
│   │   │   ├── router/
│   │   │   ├── stores/
│   │   │   ├── views/
│   │   │   └── main.js
│   │   └── package.json
│   └── frontend-mobile/            # 前端移动端 Uni-app应用
│       ├── src/
│       │   ├── pages/
│       │   ├── static/
│       │   └── main.js
│       └── package.json
│
├── libs/                           # 存放共享库和业务逻辑模块
│   └── shared-types/               # 前后端共享的TypeScript类型定义
│       └── src/
│           ├── index.ts
│           └── lib/
│
├── tools/                          # 存放构建、部署等脚本
│   ├── jenkins/
│   │   └── Jenkinsfile             # Jenkins CI/CD流水线定义
│   └── scripts/
│
├── docs/                           # 存放所有项目文档
│   ├── prd.md
│   └── architecture.md
│
├── nx.json                         # Nx Monorepo 配置文件
├── package.json                    # 根项目的package.json
└── README.md
```
