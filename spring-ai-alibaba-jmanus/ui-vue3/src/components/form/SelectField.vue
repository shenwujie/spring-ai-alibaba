<template>
  <select
    :id="field.fieldName"
    :value="value"
    @change="handleChange"
    class="form-select"
    :class="{ 'error': hasError }"
  >
    <option value="" disabled>{{ `请选择${field.displayName}` }}</option>
    <option
      v-for="option in field.displayOptions"
      :key="option"
      :value="option"
    >
      {{ option }}
    </option>
  </select>
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

const handleChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const value = target.value
  emit('update:value', value)
  emit('change', value)
}
</script>

<style scoped>
.form-select {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  transition: all 0.3s;
  cursor: pointer;
}

.form-select:focus {
  outline: none;
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.form-select option {
  background: #1f2937;
  color: rgba(255, 255, 255, 0.9);
}

.form-select.error {
  border-color: #ef4444;
}
</style>
