# 6. 外部 API (External APIs)

本系统需要与以下外部系统进行集成。

### **安全监控系统 API**

- **用途:** (`FR9`) 从现有的安全监控系统（如瓦斯、粉尘监测）获取实时异常数据。
- **文档:** (待提供) - 需要安全监控系统供应商提供API接口文档。
- **认证方式:** (待定) - 可能是IP白名单、API Key或OAuth2。
- **关键端点:**
  - `GET /api/v1/alerts`: 获取指定时间范围内的异常报警数据。
- **集成说明:** `int-monitoring` 组件将定期轮询此接口，获取增量报警数据，并将其转化为系统内部的预警事件。
 - **请求头（示例）:** `Accept: application/json`，可选 `X-API-KEY: <key>`

### **通知网关 API (短信/App推送)**

- **用途:** 发送关键事件通知，如重大隐患指派、超时未整改警告、智能预警等。
- **文档:** (待提供) - 需要企业内部的短信网关或App推送服务商提供API文档。
- **认证方式:** (待定) - 通常是基于AppKey/SecretKey的签名机制。
- **关键端点:**
  - `POST /api/v1/send_sms`: 发送短信通知。
  - `POST /api/v1/push_notification`: 推送App通知。
- **集成说明:** `int-notification` 组件将提供统一的接口，供其他业务组件调用，以发送通知。
 - **请求头（示例）:** `Content-Type: application/json`，`X-App-Key`，`X-App-Secret`
