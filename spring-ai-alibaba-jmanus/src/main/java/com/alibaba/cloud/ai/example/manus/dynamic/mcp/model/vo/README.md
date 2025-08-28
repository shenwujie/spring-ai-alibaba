# McpConfigVO 完全基于注解的配置系统

## 概述

`McpConfigVO` 类已经重新设计为一个**完全基于注解的配置系统**，实现了您的要求：**新增字段时只需定义字段名和注解，所有功能自动可用，无需修改任何代码！**

## 🎯 核心特性

### 1. 零代码修改
- ✅ 新增字段只需定义字段名和注解
- ✅ `toJson()` 自动支持新字段
- ✅ `fromJson()` 自动支持新字段  
- ✅ 构造函数自动支持新字段
- ✅ 默认值自动设置
- ✅ 类型转换自动处理
- ✅ 验证规则自动应用

### 2. 智能类型转换
- 🔄 `String` ↔ `List<String>` (如args字段)
- 🔄 `String` ↔ `Map<String,String>` (如env、headers字段)
- 🔄 自动识别字段类型并应用相应转换

### 3. 完整注解支持
- 📝 数据库字段映射
- 📝 JSON序列化配置
- 📝 前端显示配置
- 📝 验证规则配置
- 📝 条件显示和必填规则

## 🚀 使用方法

### 第一步：定义字段
```java
// 在McpConfigVO类中添加新字段
@McpConfigField(
    dbField = "custom_field",           // 数据库字段名
    jsonName = "customField",           // JSON中的字段名
    displayName = "自定义字段",         // 前端显示名称
    displayType = FieldType.TEXT,       // 前端控件类型
    displayRequired = true,             // 前端是否必填
    businessRequired = true,            // 业务逻辑是否必填
    displayValidation = "minLength:1",  // 前端验证规则
    displayOrder = 12,                  // 显示顺序
    jsonDefaultValue = "默认值"         // JSON默认值
)
@JsonProperty("customField")
private String customField;
```

### 第二步：添加getter/setter（推荐）
```java
public String getCustomField() { return customField; }
public void setCustomField(String customField) { this.customField = customField; }
```

### 第三步：完成！
🎉 **所有功能自动可用，无需修改任何其他代码！**

## 🔧 自动功能说明

### 1. JSON序列化 (`toJson()`)
```java
McpConfigVO config = new McpConfigVO();
config.setCustomField("test-value");

String json = config.toJson();
// 自动输出: {"customField":"test-value", ...}
```

### 2. JSON反序列化 (`fromJson()`)
```java
String json = "{\"customField\":\"test-value\"}";
config.fromJson(json);

// 自动设置: config.getCustomField() == "test-value"
```

### 3. 构造函数初始化
```java
// 自动设置默认值
McpConfigVO config = new McpConfigVO();
// config.getCustomField() == "默认值" (如果设置了jsonDefaultValue)
```

### 4. Entity映射
```java
// 自动从Entity映射所有字段
McpConfigVO vo = new McpConfigVO(entity, objectMapper);
// 自动映射customField等所有字段
```

### 5. 类型转换
```java
// 特殊字段自动类型转换
config.setArgs("line1\nline2\nline3");
String json = config.toJson();
// 自动输出: {"args":["line1","line2","line3"]}

config.fromJson("{\"args\":[\"a\",\"b\"]}");
// 自动设置: config.getArgs() == "a\nb"
```

## 📋 字段注解配置详解

### 基础配置
```java
@McpConfigField(
    dbField = "数据库字段名",           // 必填：对应的数据库字段
    jsonName = "JSON字段名",           // 必填：JSON序列化时的字段名
    displayName = "显示名称",          // 必填：前端显示的名称
    displayType = FieldType.TEXT       // 必填：前端控件类型
)
```

### 存储配置
```java
@McpConfigField(
    storageType = DbStorageType.DIRECT,    // 直接存储到数据库字段
    // 或者
    storageType = DbStorageType.JSON_KEY,  // 存储到JSON字段的指定key
    dbJsonKey = "json中的key"              // JSON字段的key名
)
```

### 显示配置
```java
@McpConfigField(
    displayRequired = true,                // 前端是否必填
    displayDescription = "字段描述",        // 字段说明
    displayOrder = 1,                      // 显示顺序
    displayOptions = {"选项1", "选项2"},    // 下拉选项
    displayValidation = "minLength:1"      // 验证规则
)
```

### 业务配置
```java
@McpConfigField(
    businessRequired = true,               // 业务逻辑是否必填
    businessValidation = "业务验证规则",     // 业务验证
    jsonDefaultValue = "默认值"            // JSON默认值
)
```

### 条件配置
```java
@McpConfigField(
    displayCondition = "fieldName=value",           // 显示条件
    displayRequiredCondition = "fieldName=value"    // 必填条件
)
```

## 🎨 前端控件类型

```java
FieldType.TEXT          // 文本输入框
FieldType.TEXTAREA      // 多行文本
FieldType.NUMBER        // 数字输入框
FieldType.SELECT        // 下拉选择框
FieldType.CHECKBOX      // 复选框
FieldType.URL           // URL输入框
FieldType.EMAIL         // 邮箱输入框
FieldType.DATE          // 日期选择器
FieldType.JSON_EDITOR   // JSON编辑器
// ... 更多类型
```

## 🔄 特殊字段类型处理

### 1. 数组类型字段 (如args)
```java
// 存储格式：换行分隔的字符串
config.setArgs("arg1\narg2\narg3");

// JSON格式：自动转换为数组
// {"args":["arg1","arg2","arg3"]}
```

### 2. 键值对字段 (如env、headers)
```java
// 存储格式：key:value，每行一个
config.setEnv("KEY1:VALUE1\nKEY2:VALUE2");

// JSON格式：自动转换为对象
// {"env":{"KEY1":"VALUE1","KEY2":"VALUE2"}}
```

## 📝 完整示例

### 新增一个端口字段
```java
@McpConfigField(
    dbField = "connection_config",
    dbJsonKey = "port",
    jsonName = "port",
    displayName = "端口号",
    displayType = FieldType.NUMBER,
    displayRequired = false,
    businessRequired = false,
    displayValidation = "min:1,max:65535",
    displayOrder = 12,
    jsonDefaultValue = "8080"
)
@JsonProperty("port")
private Integer port;

// 添加getter/setter
public Integer getPort() { return port; }
public void setPort(Integer port) { this.port = port; }
```

### 使用新字段
```java
// 创建实例（自动设置默认值）
McpConfigVO config = new McpConfigVO();
// config.getPort() == 8080 (自动设置)

// 设置值
config.setPort(9090);

// 转换为JSON（自动包含port字段）
String json = config.toJson();
// {"port":9090, ...}

// 从JSON解析（自动解析port字段）
config.fromJson("{\"port\":7070}");
// config.getPort() == 7070
```

## 🎯 优势总结

1. **零代码修改** - 新增字段无需修改任何业务逻辑
2. **完全自动化** - 所有功能自动支持新字段
3. **类型安全** - 编译时检查注解配置
4. **灵活扩展** - 支持任意字段类型和验证规则
5. **向后兼容** - 保持现有API不变
6. **开发效率** - 减少重复代码，提高开发速度

## 🚨 注意事项

1. **注解配置** - 确保所有字段都有正确的 `@McpConfigField` 注解
2. **字段类型** - 特殊字段会自动进行类型转换
3. **默认值** - 使用 `jsonDefaultValue` 设置字段默认值
4. **验证规则** - 使用 `displayValidation` 设置前端验证规则
5. **条件显示** - 使用 `displayCondition` 设置字段显示条件

## 🔍 测试验证

运行测试来验证所有功能：
```bash
mvn test -Dtest=McpConfigVOTest
```

测试覆盖：
- ✅ 基本字段操作
- ✅ JSON转换功能
- ✅ 新字段自动支持
- ✅ 类型转换功能
- ✅ 默认值设置
- ✅ Entity映射功能
