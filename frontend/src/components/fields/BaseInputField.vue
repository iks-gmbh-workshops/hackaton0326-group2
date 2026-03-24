<template>
  <div>
    <label v-if="showLabel && label" :for="id" class="mb-1 block text-sm font-medium text-gray-700">
      {{ label }}
    </label>
    <input
      :id="id"
      :value="valueBinding"
      :checked="checkedBinding"
      :type="type"
      :required="required"
      :disabled="disabled"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      :class="resolvedInputClass"
      @input="onInput"
      @change="onInput"
      @blur="emit('blur', $event)"
      @keydown="emit('keydown', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Props = {
  id: string
  modelValue: string | boolean
  type?: string
  label?: string
  required?: boolean
  disabled?: boolean
  placeholder?: string
  autocomplete?: string
  showLabel?: boolean
  inputClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  label: '',
  required: false,
  disabled: false,
  placeholder: '',
  autocomplete: '',
  showLabel: true,
  inputClass: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | boolean): void
  (e: 'blur', event: FocusEvent): void
  (e: 'keydown', event: KeyboardEvent): void
}>()

const valueBinding = computed(() => (typeof props.modelValue === 'string' ? props.modelValue : ''))
const checkedBinding = computed(() => (typeof props.modelValue === 'boolean' ? props.modelValue : undefined))

const resolvedInputClass = computed(() => {
  if (props.inputClass) return props.inputClass
  if (props.type === 'checkbox') return 'mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500'
  return 'w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200'
})

const onInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.type === 'checkbox') {
    emit('update:modelValue', target.checked)
    return
  }
  emit('update:modelValue', target.value)
}
</script>
