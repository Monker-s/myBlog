# myBlog 后端结构说明

## 当前目录

- `config`: 安全、Jackson、线程池、配置属性等配置类。
- `controller`: 所有接口入口，按功能命名控制器。
- `service`: 业务接口定义。
- `service/impl`: 业务接口实现。
- `mapper`: 数据访问层，当前先用 Spring Data JPA 形式占位，命名统一为 `*Mapper`。
- `entity`: 数据库表映射实体。
- `dto`: 前端请求参数对象。
- `vo`: 返回给前端的视图对象。
- `common`: 统一响应、分页对象、枚举、公共模型。
- `util`: 通用工具类。
- `exception`: 自定义异常与全局异常处理。

## 资源目录

- `application.yml`: 主配置，负责激活环境。
- `application-dev.yml`: 开发环境配置。
- `application-prod.yml`: 生产环境配置。
- `db/migration`: Flyway 数据库迁移脚本。
- `mapper`: 预留给 MyBatis XML。
- `static`: 预留静态资源目录。
- `templates`: 预留模板目录。

## 当前阶段说明

- 已根据需求文档和数据库文档把后端工程改成标准分层骨架。
- 目前控制器、服务、Mapper、实体、DTO、VO 已齐全，便于继续落真实业务。
- 鉴权、事务、缓存、限流、通知推送等逻辑当前仍是结构占位，下一步可以继续往下实现。
