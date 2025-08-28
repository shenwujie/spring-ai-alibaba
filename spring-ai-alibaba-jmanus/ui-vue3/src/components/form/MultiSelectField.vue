<template>
  <div class="multi-select-container">
    <div class="selected-items" v-if="selectedValues.length > 0">
      <div
        v-for="item in selectedValues"
        :key="item"
        class="selected-item"
      >
        {{ item }}
        <button
          @click="removeItem(item)"
          class="remove-btn"
          type="button"
        >
          ×
        </button>
      </div>
    </div>
    
    <select
      :id="field.fieldName"
      @change="handleAddItem"
      class="form-select"
      :class="{ 'error': hasError }"
    >
      <option value="" disabled>{{ `请选择${field.displayName}` }}</option>
      <option
        v-for="option in availableOptions"
        :key="option"
        :value="option"
      >
        {{ option }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FieldMetadata } from '@/types/mcp'

interface Props {
  field: FieldMetadata
  value: string[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:value': [value: string[]]
  'change': [value: string[]]
}>()

const hasError = computed(() => false)

const selectedValues = computed(() => props.value || [])

const availableOptions = computed(() => {
  if (!props.field.displayOptions) return []
  return props.field.displayOptions.filter(option => !selectedValues.value.includes(option))
})

const handleAddItem = (event: Event) => {
  const target = event.target as HTMLSelectElement
  const value = target.value
  
  if (value && !selectedValues.value.includes(value)) {
    const newValues = [...selectedValues.value, value]
    emit('update:value', newValues)
    emit('change', newValues)
  }
  
  // 重置选择
  target.value = ''
}

const removeItem = (item: string) => {
  const newValues = selectedValues.value.filter(value => value !== item)
  emit('update:value', newValues)
  emit('change', newValues)
}
</script>

<style scoped>
.multi-select-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selected-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.selected-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(102, 126, 234, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 4px;
  color: #a8b3ff;
  font-size: 12px;
}

.remove-btn {
  background: none;
  border: none;
  color: #a8b3ff;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.remove-btn:hover {
  background: rgba(102, 126, 234, 0.3);
}

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
