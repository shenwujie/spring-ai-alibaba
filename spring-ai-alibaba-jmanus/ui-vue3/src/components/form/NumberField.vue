<template>
  <input
    :id="field.fieldName"
    :value="value"
    @input="handleInput"
    type="number"
    class="form-input"
    :placeholder="field.displayDescription || `请输入${field.displayName}`"
    :min="getMinValue()"
    :max="getMaxValue()"
    :class="{ 'error': hasError }"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FieldMetadata } from '@/types/mcp'

interface Props {
  field: FieldMetadata
  value: number | string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: number]
  'change': [value: number]
}>()

const hasError = computed(() => false)

const getMinValue = () => {
  if (props.field.displayValidation?.includes('min:')) {
    const match = props.field.displayValidation.match(/min:(\d+)/)
    return match ? parseInt(match[1]) : undefined
  }
  return undefined
}

const getMaxValue = () => {
  if (props.field.displayValidation?.includes('max:')) {
    const match = props.field.displayValidation.match(/max:(\d+)/)
    return match ? parseInt(match[1]) : undefined
  }
  return undefined
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value === '' ? 0 : parseInt(target.value)
  emit('update:value', value)
  emit('change', value)
}
</script>

<style scoped>
.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  transition: all 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-input.error {
  border-color: #ef4444;
}

/* 隐藏数字输入框的上下箭头 */
.form-input::-webkit-outer-spin-button,
.form-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.form-input[type=number] {
  -moz-appearance: textfield;
}
</style>
