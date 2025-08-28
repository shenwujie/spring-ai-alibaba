<template>
  <div class="json-editor-container">
    <textarea
      :id="field.fieldName"
      :value="value"
      @input="handleInput"
      class="form-textarea json-editor"
      :placeholder="field.displayDescription || `请输入${field.displayName}（JSON格式）`"
      :rows="8"
      :class="{ 'error': hasError }"
    />
    <div class="json-actions">
      <button
        @click="formatJson"
        type="button"
        class="format-btn"
        :disabled="!isValidJson"
      >
        <Icon icon="carbon:settings" />
        格式化
      </button>
      <button
        @click="validateJson"
        type="button"
        class="validate-btn"
      >
        <Icon icon="carbon:checkmark" />
        验证
      </button>
    </div>
    <div v-if="jsonError" class="json-error">
      {{ jsonError }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Icon } from '@iconify/vue'
import type { FieldMetadata } from '@/types/mcp'

interface Props {
  field: FieldMetadata
  value: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: string]
  'change': [value: string]
}>()

const hasError = computed(() => false)
const jsonError = ref<string>('')

const isValidJson = computed(() => {
  if (!props.value) return true
  try {
    JSON.parse(props.value)
    return true
  } catch {
    return false
  }
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  const value = target.value
  jsonError.value = ''
  emit('update:value', value)
  emit('change', value)
}

const formatJson = () => {
  if (!props.value) return
  
  try {
    const parsed = JSON.parse(props.value)
    const formatted = JSON.stringify(parsed, null, 2)
    emit('update:value', formatted)
    emit('change', formatted)
    jsonError.value = ''
  } catch (error) {
    jsonError.value = 'JSON格式无效，无法格式化'
  }
}

const validateJson = () => {
  if (!props.value) {
    jsonError.value = ''
    return
  }
  
  try {
    JSON.parse(props.value)
    jsonError.value = 'JSON格式正确'
    setTimeout(() => {
      jsonError.value = ''
    }, 2000)
  } catch (error) {
    jsonError.value = `JSON格式错误: ${error instanceof Error ? error.message : '未知错误'}`
  }
}
</script>

<style scoped>
.json-editor-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.json-editor {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.4;
}

.json-actions {
  display: flex;
  gap: 8px;
}

.format-btn,
.validate-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 4px;
  color: #a8b3ff;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.format-btn:hover:not(:disabled),
.validate-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.5);
}

.format-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.json-error {
  font-size: 12px;
  color: #ef4444;
  padding: 8px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 4px;
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  line-height: 1.4;
  transition: all 0.3s;
  resize: vertical;
  font-family: inherit;
}

.form-textarea:focus {
  outline: none;
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.form-textarea::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-textarea.error {
  border-color: #ef4444;
}
</style>
