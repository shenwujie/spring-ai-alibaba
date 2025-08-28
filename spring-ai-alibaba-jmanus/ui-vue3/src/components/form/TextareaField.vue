<template>
  <textarea
    :id="field.fieldName"
    :value="value"
    @input="handleInput"
    class="form-textarea"
    :placeholder="field.displayDescription || `请输入${field.displayName}`"
    :rows="getRows()"
    :class="{ 'error': hasError }"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
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

const getRows = () => {
  // 根据字段类型或描述确定行数
  if (props.field.displayDescription?.includes('多行')) {
    return 5
  }
  return 3
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  const value = target.value
  emit('update:value', value)
  emit('change', value)
}
</script>

<style scoped>
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
