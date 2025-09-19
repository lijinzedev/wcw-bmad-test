# 7. REST API 规范 (REST API Spec)

所有提供给前端和移动端的API都将遵循RESTful设计原则，并以OpenAPI 3.0格式进行定义。

## 7.1 通用约定

- **根路径 (Base Path):** 所有API都将以 `/api/v1` 作为根路径。

- **认证 (Authentication):** 除登录接口外，所有API请求的Header中都必须包含一个有效的JWT (JSON Web Token): `Authorization: Bearer <token>`。

- **数据格式 (Data Format):** 所有请求和响应的主体都使用 `application/json` 格式。

- **错误处理 (Error Handling):** 发生错误时，API将返回相应的HTTP状态码（如400, 401, 403, 404, 500），并在响应体中包含统一的错误信息结构：

  ```
  {
    "timestamp": "2025-09-10T12:00:00.000Z",
    "status": 404,
    "error": "Not Found",
    "message": "ID为 'xxx' 的隐患未找到",
    "path": "/api/v1/hazards/xxx"
  }
  ```

- **分页 (Pagination):** 对于返回列表的GET请求，将使用基于 `page` 和 `size` 参数的分页。

## 7.2 OpenAPI 定义 (部分示例)

以下是核心认证和授权API的初步定义，用于演示格式。完整的API将在开发过程中逐步完善。

```
openapi: 3.0.0
info:
  title: 煤矿双预防系统 API
  version: "1.0.0"
  description: 用于PC端和移动端的后端API

paths:
  /api/v1/auth/login:
    post:
      summary: 用户登录
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              properties:
                username:
                  type: string
                password:
                  type: string
      responses:
        '200':
          description: 登录成功
          content:
            application/json:
              schema:
                type: object
                properties:
                  token:
                    type: string
                    description: "JWT Token"
                  user:
                    $ref: '#/components/schemas/User'
        '401':
          description: 认证失败

components:
  schemas:
    User:
      type: object
      properties:
        userId:
          type: string
          format: uuid
        username:
          type: string
        fullName:
          type: string
        organizationName:
          type: string
```
