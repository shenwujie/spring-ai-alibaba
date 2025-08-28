<template>
  <input
    :id="field.fieldName"
    :value="value"
    @input="handleInput"
    type="email"
    class="form-input"
    :placeholder="field.displayDescription || `请输入${field.displayName}，例如：user@example.com`"
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

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value
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
</style>
