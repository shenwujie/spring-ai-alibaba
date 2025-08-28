<!--
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
-->
<template>
  <div class="dynamic-form">
    <div v-if="loading" class="loading-state">
      <Icon icon="carbon:loading" class="loading-icon" />
      {{ t('common.loading') }}
    </div>
    
    <div v-else-if="error" class="error-state">
      <Icon icon="carbon:warning" class="error-icon" />
      <p>{{ error }}</p>
      <button 
        @click="retryLoadMetadata" 
        class="retry-btn"
        :disabled="loading"
      >
        <Icon icon="carbon:renew" />
        重试
      </button>
    </div>
    
    <div v-else class="form-content">
      <!-- 动态生成的表单字段 -->
      <div
        v-for="field in sortedFields"
        :key="field.fieldName"
        v-show="isFieldVisible(field)"
        class="form-item"
        :class="{ 'required': isFieldRequired(field) }"
      >
        <label :for="field.fieldName" class="form-label">
          {{ field.displayName }}
          <span v-if="isFieldRequired(field)" class="required-mark">*</span>
        </label>
        
        <div v-if="field.displayDescription" class="field-description">
          {{ field.displayDescription }}
        </div>
        
        <!-- 根据字段类型渲染不同的控件 -->
        <component
          :is="getFieldComponent(field.displayType)"
          :field="field"
          :value="getFieldValue(field)"
          @update:value="updateFieldValue(field, $event)"
          @change="handleFieldChange(field, $event)"
        />
        
        <!-- 验证错误提示 -->
        <div v-if="fieldErrors[field.fieldName]" class="field-error">
          {{ fieldErrors[field.fieldName] }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { useRequest } from '@/composables/useRequest'
import { McpApiService } from '@/api/mcp-api-service'
import type { FieldMetadata, FormMetadata } from '@/types/mcp'

// 导入字段组件
import TextField from '@/components/form/TextField.vue'
import TextareaField from '@/components/form/TextareaField.vue'
import NumberField from '@/components/form/NumberField.vue'
import SelectField from '@/components/form/SelectField.vue'
import MultiSelectField from '@/components/form/MultiSelectField.vue'
import CheckboxField from '@/components/form/CheckboxField.vue'
import RadioField from '@/components/form/RadioField.vue'
import UrlField from '@/components/form/UrlField.vue'
import EmailField from '@/components/form/EmailField.vue'
import JsonEditorField from '@/components/form/JsonEditorField.vue'
import EnvEditorField from '@/components/form/EnvEditorField.vue'

// Props
interface Props {
  formData: Record<string, any>
  isEditMode?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isEditMode: false
})

// Emits
const emit = defineEmits<{
  'update:form-data': [data: Record<string, any>]
  'field-change': [field: FieldMetadata, value: any]
  'validation-change': [isValid: boolean, errors: Record<string, string>]
}>()

// Internationalization
const { t } = useI18n()

// Use composition functions
const { loading } = useRequest()

// Reactive data
const formMetadata = ref<{
  fields: FieldMetadata[]
  validationRules: Record<string, any>
  formSchema: Record<string, any>
} | null>(null)
const error = ref<string>('')
const fieldErrors = ref<Record<string, string>>({})

// Computed properties
const sortedFields = computed(() => {
  if (!formMetadata.value?.fields) return []
  return [...formMetadata.value.fields].sort((a, b) => a.displayOrder - b.displayOrder)
})

// Methods
const loadFormMetadata = async () => {
  try {
    loading.value = true
    error.value = ''
    
    console.log('Loading form metadata for mode:', props.isEditMode)
    
    // 根据模式选择不同的元数据接口
    const endpoint = props.isEditMode ? '/api/mcp/config/metadata/vo' : '/api/mcp/config/metadata/request'
    console.log('Fetching metadata from endpoint:', endpoint)
    
    const response = await fetch(endpoint)
    
    if (!response.ok) {
      throw new Error(`Failed to load form metadata: ${response.statusText}`)
    }
    
    const metadata = await response.json()
    console.log('Received metadata:', metadata)
    
    formMetadata.value = metadata
  } catch (err) {
    console.error('Failed to load form metadata:', err)
    error.value = err instanceof Error ? err.message : 'Unknown error'
    
    // 如果后端API不可用，显示错误信息
    if (err instanceof Error && err.message.includes('Failed to fetch')) {
      error.value = '无法连接到后端服务，请检查网络连接或联系管理员'
    }
  } finally {
    loading.value = false
  }
}

const retryLoadMetadata = () => {
  error.value = ''
  loadFormMetadata()
}

const getFieldComponent = (fieldType: string) => {
  switch (fieldType) {
    case 'TEXT':
      return TextField
    case 'TEXTAREA':
      return TextareaField
    case 'NUMBER':
      return NumberField
    case 'SELECT':
      return SelectField
    case 'MULTI_SELECT':
      return MultiSelectField
    case 'CHECKBOX':
      return CheckboxField
    case 'RADIO':
      return RadioField
    case 'URL':
      return UrlField
    case 'EMAIL':
      return EmailField
    case 'JSON_EDITOR':
      return JsonEditorField
    case 'ENV_EDITOR':
      return EnvEditorField
    default:
      return TextField
  }
}

// 判断字段是否应该显示
const isFieldVisible = (field: FieldMetadata) => {
  // 如果字段有显示条件，根据条件判断
  if (field.displayCondition) {
    const visible = evaluateCondition(field.displayCondition, props.formData)
    console.log(`Field ${field.fieldName} visible: ${visible} (condition: ${field.displayCondition})`)
    return visible
  }
  
  // 如果没有条件，默认显示
  return true
}

// 判断字段是否必填
const isFieldRequired = (field: FieldMetadata) => {
  // 如果字段有必填条件，根据条件判断
  if (field.displayRequiredCondition) {
    return evaluateCondition(field.displayRequiredCondition, props.formData)
  }
  
  // 如果没有条件，使用默认值
  return field.displayRequired || field.businessRequired
}

// 评估条件表达式
const evaluateCondition = (condition: string, formData: Record<string, any>): boolean => {
  // 特殊条件：NEVER 表示永远不显示
  if (condition === 'NEVER') {
    return false
  }
  
  // 格式：fieldName=value1,value2
  const parts = condition.split('=')
  if (parts.length !== 2) {
    return true // 格式错误时默认显示
  }
  
  const fieldName = parts[0]
  const expectedValues = parts[1].split(',')
  const actualValue = formData[fieldName]
  
  console.log(`Evaluating condition: ${fieldName}=${expectedValues}, actual: ${actualValue}`)
  
  return expectedValues.includes(actualValue)
}

const getFieldValue = (field: FieldMetadata) => {
  console.log(`Getting value for field: ${field.fieldName}, storageType: ${field.storageType}, dbField: ${field.dbField}, dbJsonKey: ${field.dbJsonKey}`)
  
  // 根据字段的存储类型获取值
  if (field.storageType === 'VIRTUAL') {
    const value = props.formData[field.jsonName] || ''
    console.log(`VIRTUAL field ${field.fieldName}: ${value}`)
    return value
  }
  
  if (field.storageType === 'JSON') {
    // 从JSON字段中提取特定key的值
    const jsonValue = props.formData[field.dbField]
    console.log(`JSON field ${field.fieldName}, raw value: ${jsonValue}`)
    
    if (jsonValue && field.dbJsonKey) {
      try {
        const parsed = JSON.parse(jsonValue)
        const value = parsed[field.dbJsonKey] || ''
        console.log(`JSON field ${field.fieldName}, parsed value: ${value}`)
        return value
      } catch (error) {
        console.error(`Failed to parse JSON for field ${field.fieldName}:`, error)
        return ''
      }
    }
    return jsonValue || ''
  }
  
  // 直接字段
  const value = props.formData[field.jsonName] || ''
  console.log(`DIRECT field ${field.fieldName}: ${value}`)
  return value
}

const updateFieldValue = (field: FieldMetadata, value: any) => {
  console.log(`Updating field: ${field.fieldName}, value: ${value}, storageType: ${field.storageType}`)
  
  const newFormData = { ...props.formData }
  
  if (field.storageType === 'VIRTUAL') {
    if (field.jsonName) {
      newFormData[field.jsonName] = value
      console.log(`Updated VIRTUAL field ${field.fieldName}: ${value}`)
    }
  } else if (field.storageType === 'JSON') {
    // 更新JSON字段中的特定key
    let jsonValue = props.formData[field.dbField] || '{}'
    console.log(`Updating JSON field ${field.fieldName}, current JSON: ${jsonValue}`)
    
    try {
      const parsed = JSON.parse(jsonValue)
      if (typeof parsed === 'object' && parsed !== null && field.dbJsonKey) {
        parsed[field.dbJsonKey] = value
        newFormData[field.dbField] = JSON.stringify(parsed)
        console.log(`Updated JSON field ${field.fieldName}, new JSON: ${newFormData[field.dbField]}`)
      }
    } catch (error) {
      console.error(`Failed to parse JSON for field ${field.fieldName}:`, error)
      // 如果解析失败，创建新的JSON
      if (field.dbJsonKey) {
        const newJson: Record<string, any> = { [field.dbJsonKey]: value }
        newFormData[field.dbField] = JSON.stringify(newJson)
        console.log(`Created new JSON for field ${field.fieldName}: ${newFormData[field.dbField]}`)
      }
    }
  } else {
    // 直接字段
    if (field.jsonName) {
      newFormData[field.jsonName] = value
      console.log(`Updated DIRECT field ${field.fieldName}: ${value}`)
    }
  }
  
  emit('update:form-data', newFormData)
  validateField(field, value)
}

const handleFieldChange = (field: FieldMetadata, value: any) => {
  emit('field-change', field, value)
}

const validateField = (field: FieldMetadata, value: any) => {
  const errors: string[] = []
  
  // 必填验证
  if (isFieldRequired(field) && (!value || value.toString().trim() === '')) {
    errors.push(`${field.displayName}不能为空`)
  }
  
  // 长度验证
  if (value && field.displayValidation) {
    const validation = field.displayValidation
    
    if (validation.includes('minLength:')) {
      const minLength = parseInt(validation.match(/minLength:(\d+)/)?.[1] || '0')
      if (value.toString().length < minLength) {
        errors.push(`${field.displayName}长度不能少于${minLength}个字符`)
      }
    }
    
    if (validation.includes('maxLength:')) {
      const maxLength = parseInt(validation.match(/maxLength:(\d+)/)?.[1] || '0')
      if (value.toString().length > maxLength) {
        errors.push(`${field.displayName}长度不能超过${maxLength}个字符`)
      }
    }
  }
  
  // 数值范围验证
  if (field.displayType === 'NUMBER' && value && field.displayValidation) {
    const validation = field.displayValidation
    
    if (validation.includes('min:')) {
      const min = parseInt(validation.match(/min:(\d+)/)?.[1] || '0')
      if (parseInt(value) < min) {
        errors.push(`${field.displayName}不能小于${min}`)
      }
    }
    
    if (validation.includes('max:')) {
      const max = parseInt(validation.match(/max:(\d+)/)?.[1] || '0')
      if (parseInt(value) > max) {
        errors.push(`${field.displayName}不能大于${max}`)
      }
    }
  }
  
  // URL格式验证
  if (field.displayType === 'URL' && value) {
    try {
      new URL(value)
    } catch {
      errors.push(`${field.displayName}格式无效`)
    }
  }
  
  // 更新字段错误
  if (errors.length > 0) {
    fieldErrors.value[field.fieldName] = errors[0]
  } else {
    delete fieldErrors.value[field.fieldName]
  }
  
  // 触发验证状态变化
  const isValid = Object.keys(fieldErrors.value).length === 0
  emit('validation-change', isValid, fieldErrors.value)
}

const validateForm = () => {
  // 验证所有字段
  sortedFields.value.forEach(field => {
    const value = getFieldValue(field)
    validateField(field, value)
  })
  
  const isValid = Object.keys(fieldErrors.value).length === 0
  return { isValid, errors: fieldErrors.value }
}

// Expose methods for parent component
defineExpose({
  validateForm,
  loadFormMetadata
})

// Lifecycle
onMounted(() => {
  loadFormMetadata()
})

// Watch for form data changes
watch(() => props.formData, () => {
  // 当表单数据变化时，重新验证
  if (formMetadata.value) {
    validateForm()
  }
}, { deep: true })

// Watch for connection type changes to trigger field visibility updates
watch(() => props.formData.connectionType, () => {
  // 当连接类型改变时，强制重新渲染
  console.log('Connection type changed to:', props.formData.connectionType)
}, { immediate: true })
</script>

<style scoped>
.dynamic-form {
  width: 100%;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
}

.loading-icon,
.error-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.error-state {
  color: #ef4444;
}

.retry-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 4px;
  color: #a8b3ff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.retry-btn:hover:not(:disabled) {
  background: rgba(102, 126, 234, 0.3);
  border-color: rgba(102, 126, 234, 0.5);
}

.retry-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item.required .form-label {
  color: rgba(255, 255, 255, 0.9);
}

.form-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.required-mark {
  color: #ef5350;
  font-weight: bold;
}

.field-description {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.4;
}

.field-error {
  font-size: 12px;
  color: #ef4444;
  margin-top: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-content {
    gap: 16px;
  }
  
  .form-item {
    gap: 6px;
  }
}
</style>
