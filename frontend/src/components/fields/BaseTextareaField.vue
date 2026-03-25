<template>
  <div>
    <label v-if="showLabel && label" :for="id" class="mb-1 block text-sm font-medium text-gray-700">
      {{ label }}
    </label>
    <textarea
      :id="id"
      :value="modelValue"
      :required="required"
      :disabled="disabled"
      :placeholder="placeholder"
      :rows="rows"
      :class="resolvedTextareaClass"
      @input="onInput"
      @blur="emit('blur', $event)"
      @keydown="emit('keydown', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Props = {
  id: string
  modelValue: string
  label?: string
  required?: boolean
  disabled?: boolean
  placeholder?: string
  rows?: number
  showLabel?: boolean
  textareaClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  required: false,
  disabled: false,
  placeholder: '',
  rows: 4,
  showLabel: true,
  textareaClass: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'blur', event: FocusEvent): void
  (e: 'keydown', event: KeyboardEvent): void
}>()

const resolvedTextareaClass = computed(() => {
  if (props.textareaClass) return props.textareaClass
  return 'w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200'
})

const onInput = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}
</script>
