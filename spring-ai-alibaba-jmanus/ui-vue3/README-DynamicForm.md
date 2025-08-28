# 动态MCP配置表单系统

## 概述

这是一个基于后端元数据的动态表单系统，能够根据后端的注解配置自动生成前端表单，实现前后端的完全对齐。

## 系统架构

### 后端部分

1. **注解系统**：使用 `@McpConfigField` 注解定义字段属性
2. **元数据生成**：`McpConfigFieldProcessor` 自动生成前端配置
3. **API接口**：提供 `/api/mcp/config/metadata/*` 系列接口

### 前端部分

1. **动态表单组件**：`DynamicMcpConfigForm.vue`
2. **字段组件库**：支持多种控件类型
3. **类型定义**：完整的TypeScript类型支持

## 使用方法

### 1. 替换现有表单

将原来的 `McpConfigForm` 替换为 `DynamicMcpConfigForm`：

```vue
<!-- 原来的硬编码表单 -->
<McpConfigForm
  :form-data="configForm"
  :is-edit-mode="true"
  @connection-type-change="handleConnectionTypeChange"
/>

<!-- 新的动态表单 -->
<DynamicMcpConfigForm
  :form-data="configForm"
  :is-edit-mode="true"
  @update:form-data="(data: any) => Object.assign(configForm, data)"
  @validation-change="handleValidationChange"
/>
```

### 2. 处理表单事件

```typescript
// 处理表单数据更新
const handleFormDataUpdate = (data: any) => {
  Object.assign(configForm, data)
}

// 处理验证状态变化
const handleValidationChange = (isValid: boolean, errors: Record<string, string>) => {
  console.log('Form validation:', { isValid, errors })
}
```

### 3. 表单验证

```typescript
// 在保存时验证表单
const handleSave = async () => {
  const dynamicFormRef = document.querySelector('.dynamic-form')?.__vueParentComponent?.exposed
  if (dynamicFormRef?.validateForm) {
    const validation = dynamicFormRef.validateForm()
    if (!validation.isValid) {
      showMessage(Object.values(validation.errors)[0], 'error')
      return
    }
  }
  
  // 继续保存逻辑...
}
```

## 支持的字段类型

| 后端类型 | 前端组件 | 说明 |
|---------|---------|------|
| `TEXT` | `TextField` | 文本输入框 |
| `TEXTAREA` | `TextareaField` | 多行文本 |
| `NUMBER` | `NumberField` | 数字输入框 |
| `SELECT` | `SelectField` | 下拉选择框 |
| `MULTI_SELECT` | `MultiSelectField` | 多选下拉框 |
| `CHECKBOX` | `CheckboxField` | 复选框 |
| `RADIO` | `RadioField` | 单选框 |
| `URL` | `UrlField` | URL输入框 |
| `EMAIL` | `EmailField` | 邮箱输入框 |
| `JSON_EDITOR` | `JsonEditorField` | JSON编辑器 |
| `ENV_EDITOR` | `EnvEditorField` | 环境变量编辑器 |

## 验证规则

支持以下验证规则：

- `required`：必填验证
- `minLength:X`：最小长度
- `maxLength:X`：最大长度
- `min:X`：最小值
- `max:X`：最大值
- `url`：URL格式验证
- `email`：邮箱格式验证

## 存储类型

- `DIRECT`：直接存储到数据库字段
- `JSON`：存储到JSON字段的特定key
- `VIRTUAL`：虚拟字段，不存储到数据库

## 优势

1. **前后端对齐**：表单完全基于后端配置生成
2. **维护性**：修改字段配置只需更新后端注解
3. **一致性**：统一的验证规则和显示逻辑
4. **扩展性**：支持新的字段类型和验证规则
5. **类型安全**：完整的TypeScript类型支持

## 注意事项

1. 确保后端元数据接口正常工作
2. 字段组件需要正确导入和注册
3. 表单数据格式需要与后端期望的格式一致
4. 验证规则需要前后端保持一致

## 故障排除

### 常见问题

1. **字段不显示**：检查后端元数据是否正确返回
2. **验证失败**：检查验证规则格式是否正确
3. **类型错误**：确保TypeScript类型定义完整

### 调试方法

1. 检查浏览器控制台的错误信息
2. 验证后端API接口返回的数据格式
3. 使用Vue DevTools检查组件状态
