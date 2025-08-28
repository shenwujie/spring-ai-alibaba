<template>
  <div class="env-editor-container">
    <textarea
      v-model="envText"
      class="env-textarea"
      :placeholder="placeholder"
      rows="4"
      @input="handleInput"
      @blur="handleBlur"
    />
    
    <div class="env-format">
      <button
        @click="formatAsJson"
        type="button"
        class="format-btn"
      >
        <Icon icon="carbon:document" />
        转换为JSON格式
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
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

const envText = ref('')
const placeholder = '环境变量配置，格式：key:value，每行一个'

// 监听外部value变化，同步到本地状态
watch(() => props.value, (newValue) => {
  if (newValue) {
    try {
      // 尝试解析为JSON对象
      const parsed = JSON.parse(newValue)
      if (typeof parsed === 'object' && !Array.isArray(parsed)) {
        // 如果是JSON对象，转换为key:value格式
        envText.value = Object.entries(parsed)
          .map(([key, value]) => `${key}:${value}`)
          .join('\n')
      } else {
        // 如果不是预期的JSON格式，直接使用原值
        envText.value = newValue
      }
    } catch {
      // 如果解析失败，直接使用原值
      envText.value = newValue
    }
  } else {
    envText.value = ''
  }
}, { immediate: true })

const handleInput = () => {
  // 实时更新值
  emit('update:value', envText.value)
  emit('change', envText.value)
}

const handleBlur = () => {
  // 失去焦点时确保值已更新
  emit('update:value', envText.value)
  emit('change', envText.value)
}

const formatAsJson = () => {
  if (!envText.value.trim()) {
    emit('update:value', '{}')
    emit('change', '{}')
    return
  }
  
  try {
    // 将key:value格式转换为JSON对象
    const envObj: Record<string, string> = {}
    const lines = envText.value.split('\n').filter(line => line.trim())
    
    for (const line of lines) {
      const colonIndex = line.indexOf(':')
      if (colonIndex > 0) {
        const key = line.substring(0, colonIndex).trim()
        const value = line.substring(colonIndex + 1).trim()
        if (key && value) {
          envObj[key] = value
        }
      }
    }
    
    const jsonValue = JSON.stringify(envObj, null, 2)
    emit('update:value', jsonValue)
    emit('change', jsonValue)
  } catch (error) {
    console.error('Failed to format as JSON:', error)
    // 如果转换失败，保持原值
  }
}

// 监听value变化，同步到本地状态
watch(() => props.value, () => {
  // 当外部value变化时，重新计算envVars
  // 这里不需要额外处理，因为computed会自动处理
}, { immediate: true })
</script>

<style scoped>
.env-editor-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.env-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  line-height: 1.5;
  resize: vertical;
  transition: all 0.3s;
}

.env-textarea:focus {
  outline: none;
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.env-textarea::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.env-format {
  display: flex;
  justify-content: flex-end;
}

.format-btn {
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

.format-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.5);
}
</style>
