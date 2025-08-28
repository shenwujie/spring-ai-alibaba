/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * MCP配置字段元数据接口
 */
export interface FieldMetadata {
  fieldName: string
  fieldType: string
  dbField: string
  dbJsonKey?: string
  storageType: string
  jsonName: string
  jsonDefaultValue?: string
  jsonRequired: boolean
  displayName: string
  displayDescription?: string
  displayRequired: boolean
  displayType: string
  displayOptions?: string[]
  displayValidation?: string
  displayOrder: number
  businessRequired: boolean
  businessValidation?: string
  dynamic: boolean
  displayCondition?: string
  displayRequiredCondition?: string
}

/**
 * 表单元数据接口
 */
export interface FormMetadata {
  fields: FieldMetadata[]
  validationRules: Record<string, any>
  formSchema: Record<string, any>
}

/**
 * 验证规则接口
 */
export interface ValidationRule {
  required?: boolean
  message?: string
  minLength?: number
  maxLength?: number
  min?: number
  max?: number
  url?: boolean
  email?: boolean
  pattern?: string
}

/**
 * JSON验证结果接口
 */
export interface JsonValidationResult {
  isValid: boolean
  errors?: string[]
}

/**
 * MCP配置表单数据接口
 */
export interface McpConfigFormData {
  mcpServerName: string
  connectionType: 'STUDIO' | 'SSE' | 'STREAMING'
  command?: string
  url?: string
  args?: string
  env?: string
  status: 'ENABLE' | 'DISABLE'
}

/**
 * MCP服务器保存请求接口
 */
export interface McpServerSaveRequest {
  id?: number
  connectionType: 'STUDIO' | 'SSE' | 'STREAMING'
  mcpServerName: string
  command?: string
  url?: string
  args?: string[]
  env?: Record<string, string>
  timeout?: number
  retryCount?: number
  headers?: Record<string, string>
  status: 'ENABLE' | 'DISABLE'
}
