<template>
  <div class="radio-container">
    <label
      v-for="option in field.displayOptions"
      :key="option"
      class="radio-label"
    >
      <input
        :id="`${field.fieldName}-${option}`"
        :name="field.fieldName"
        :value="option"
        :checked="value === option"
        @change="handleChange"
        type="radio"
        class="form-radio"
        :class="{ 'error': hasError }"
      />
      <span class="radio-text">{{ option }}</span>
    </label>
  </div>
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
  const target = event.target as HTMLInputElement
  const value = target.value
  emit('update:value', value)
  emit('change', value)
}
</script>

<style scoped>
.radio-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.form-radio {
  width: 16px;
  height: 16px;
  accent-color: #667eea;
  cursor: pointer;
}

.radio-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.form-radio.error {
  accent-color: #ef4444;
}
</style>
