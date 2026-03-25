<template>
  <div>
    <label :for="id" class="mb-1 block text-sm font-medium text-gray-700">
      {{ label }}
    </label>
    <input
      :id="id"
      :name="name || id"
      :type="type"
      :value="modelValue"
      :required="required"
      :disabled="disabled"
      :autocomplete="autocomplete || undefined"
      :inputmode="inputmode || undefined"
      :placeholder="placeholder || undefined"
      :aria-invalid="Boolean(error)"
      :aria-describedby="describedBy"
      class="w-full rounded-md border px-3 py-2 text-gray-900 outline-none focus:ring-2"
      :class="error ? 'border-red-300 focus:border-red-500 focus:ring-red-200' : 'border-gray-300 focus:border-blue-500 focus:ring-blue-200'"
      @input="onInput"
      @blur="onBlur"
    />

    <p
      v-if="helperText"
      :id="`${id}-hint`"
      class="mt-1 text-xs text-gray-500"
    >
      {{ helperText }}
    </p>
    <p
      v-if="error"
      :id="`${id}-error`"
      class="mt-1 text-xs text-red-600"
    >
      {{ error }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Props = {
  id: string
  label: string
  modelValue: string
  type?: string
  name?: string
  autocomplete?: string
  inputmode?: 'none' | 'text' | 'tel' | 'url' | 'email' | 'numeric' | 'decimal' | 'search'
  placeholder?: string
  required?: boolean
  disabled?: boolean
  helperText?: string
  error?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  name: '',
  autocomplete: '',
  placeholder: '',
  required: false,
  disabled: false,
  helperText: '',
  error: ''
})

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void
  (event: 'blur', value: FocusEvent): void
}>()

const describedBy = computed(() => {
  const ids: string[] = []

  if (props.helperText) {
    ids.push(`${props.id}-hint`)
  }

  if (props.error) {
    ids.push(`${props.id}-error`)
  }

  return ids.length > 0 ? ids.join(' ') : undefined
})

const onInput = (event: Event) => {
  emit('update:modelValue', (event.target as HTMLInputElement).value)
}

const onBlur = (event: FocusEvent) => {
  emit('blur', event)
}
</script>
