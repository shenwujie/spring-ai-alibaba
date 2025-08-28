# MCP配置注解驱动系统

## 概述

这是一个基于注解驱动的MCP配置管理系统，通过声明式注解来配置字段的各种属性，实现代码即文档、自动生成前端表单、统一验证规则等功能。

## 系统架构

```
注解层 (Annotations)
    ↓
元数据层 (Metadata)
    ↓
处理器层 (Processor)
    ↓
应用层 (Controllers, Services)
    ↓
前端层 (Dynamic Forms)
```

## 核心组件

### 1. 注解体系

#### @McpConfigField
主要的配置注解，用于声明字段的各种属性：

```java
@McpConfigField(
    dbField = "connection_config",           // 数据库字段名
    dbJsonKey = "command",                  // JSON中的key
    storageType = DbStorageType.JSON_KEY,   // 存储类型
    jsonName = "command",                   // JSON序列化名称
    displayName = "执行命令",               // 前端显示名称
    displayType = FieldType.TEXT,           // 前端控件类型
    displayRequired = true,                 // 前端是否必填
    businessRequired = true,                // 业务逻辑是否必填
    displayValidation = "minLength:1",      // 前端验证规则
    displayOrder = 1                        // 显示顺序
)
private String command;
```

### 2. 基类设计

#### McpConfigBase
所有MCP配置类的基类，包含：
- 公共配置字段（command, url, args, env, timeout, retryCount, headers, status）
- JSON序列化/反序列化方法
- 连接类型自动判断逻辑
- URL验证方法

### 3. 继承类

#### McpConfigVO
前端展示VO，继承McpConfigBase

#### McpServerConfig
内部服务器配置，继承McpConfigBase

### 4. 统一请求类

#### McpConfigRequestVO
支持单个和批量请求的统一类

## 使用方法

### 1. 定义配置类

```java
@McpConfigEntity
public class MyConfig extends McpConfigBase {
    
    @McpConfigField(
        dbField = "custom_field",
        jsonName = "customField",
        displayName = "自定义字段",
        displayType = FieldType.TEXT,
        displayRequired = true,
        displayOrder = 1
    )
    private String customField;
}
```

### 2. 使用注解处理器

```java
@Autowired
private McpConfigFieldProcessor fieldProcessor;

// 生成字段元数据
List<FieldMetadata> fields = fieldProcessor.generateFieldMetadata(MyConfig.class);

// 生成前端配置
Map<String, Object> frontendConfig = fieldProcessor.generateFrontendConfig(MyConfig.class);
```

## API接口

### 元数据接口

- `GET /api/mcp/config/metadata/vo` - 获取VO字段元数据
- `GET /api/mcp/config/metadata/request` - 获取请求字段元数据
- `GET /api/mcp/config/metadata/all` - 获取所有配置类元数据
- `GET /api/mcp/config/metadata/validation` - 获取验证规则
- `GET /api/mcp/config/metadata/schema` - 获取表单模式

## 优势特性

### 1. 声明式配置
- 通过注解清晰描述字段用途
- 代码即文档，易于理解和维护
- 减少硬编码，提高灵活性

### 2. 自动化生成
- 前端表单自动生成
- 验证规则自动生成
- 数据库操作SQL自动生成
- 减少重复代码

### 3. 统一管理
- 所有配置信息集中在一个地方
- 新增字段只需要添加注解
- 修改配置只需要修改注解

### 4. 类型安全
- 编译时检查注解配置
- 运行时动态生成元数据
- 减少运行时错误

### 5. 扩展性强
- 支持新的字段类型
- 支持新的验证规则
- 支持新的存储方式

## 总结

这个注解驱动系统通过声明式配置实现了：
1. **代码即文档** - 注解清晰描述字段用途
2. **自动化生成** - 减少重复代码和手动维护
3. **统一管理** - 集中管理所有配置信息
4. **类型安全** - 编译时检查，运行时验证
5. **扩展性强** - 易于添加新功能和规则

通过这种设计，开发人员可以专注于业务逻辑，而将配置管理、表单生成、验证规则等重复性工作交给系统自动处理，大大提高了开发效率和代码质量。
