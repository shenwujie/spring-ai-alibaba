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

import { request } from '@/utils/request'

export interface FieldMetadata {
  fieldName: string
  dbField: string
  dbJsonKey: string
  storageType: string
  jsonName: string
  jsonDefaultValue: string
  jsonRequired: boolean
  displayName: string
  displayDescription: string
  displayRequired: boolean
  displayType: string
  displayOptions: string[]
  displayValidation: string
  displayOrder: number
  businessRequired: boolean
  businessValidation: string
  dynamic: boolean
  fieldType: string
}

export interface FrontendConfig {
  fields: FieldMetadata[]
  validationRules: Record<string, any>
  formSchema: Record<string, any>
}

export class McpMetadataApiService {
  // 获取VO字段元数据
  static async getVoMetadata(): Promise<FrontendConfig> {
    return request.get('/api/mcp/config/metadata/vo')
  }

  // 获取请求字段元数据
  static async getRequestMetadata(): Promise<FrontendConfig> {
    return request.get('/api/mcp/config/metadata/request')
  }

  // 获取所有配置类元数据
  static async getAllMetadata(): Promise<Record<string, FrontendConfig>> {
    return request.get('/api/mcp/config/metadata/all')
  }

  // 获取验证规则
  static async getValidationRules(): Promise<Record<string, any>> {
    return request.get('/api/mcp/config/metadata/validation')
  }

  // 获取表单模式
  static async getFormSchema(): Promise<Record<string, any>> {
    return request.get('/api/mcp/config/metadata/schema')
  }
}
