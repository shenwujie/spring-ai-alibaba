<template>
  <div class="checkbox-container">
    <label class="checkbox-label">
      <input
        :id="field.fieldName"
        :checked="value"
        @change="handleChange"
        type="checkbox"
        class="form-checkbox"
        :class="{ 'error': hasError }"
      />
      <span class="checkbox-text">{{ field.displayName }}</span>
    </label>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FieldMetadata } from '@/types/mcp'

interface Props {
  field: FieldMetadata
  value: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: boolean]
  'change': [value: boolean]
}>()

const hasError = computed(() => false)

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.checked
  emit('update:value', value)
  emit('change', value)
}
</script>

<style scoped>
.checkbox-container {
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.form-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #667eea;
  cursor: pointer;
}

.checkbox-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.form-checkbox.error {
  accent-color: #ef4444;
}
</style>
