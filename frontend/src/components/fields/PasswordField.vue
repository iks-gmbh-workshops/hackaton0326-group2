<template>
  <BaseInputField
    :id="id"
    :model-value="modelValue"
    type="password"
    :label="label"
    :required="required"
    :disabled="disabled"
    :placeholder="placeholder"
    :autocomplete="autocomplete"
    :show-label="showLabel"
    @update:model-value="onUpdate"
    @blur="emit('blur', $event)"
    @keydown="emit('keydown', $event)"
  />
</template>

<script setup lang="ts">
import BaseInputField from './BaseInputField.vue'

type Props = {
  id: string
  modelValue: string
  label?: string
  required?: boolean
  disabled?: boolean
  placeholder?: string
  autocomplete?: string
  showLabel?: boolean
}

withDefaults(defineProps<Props>(), {
  label: 'Passwort',
  required: false,
  disabled: false,
  placeholder: '',
  autocomplete: 'current-password',
  showLabel: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'blur', event: FocusEvent): void
  (e: 'keydown', event: KeyboardEvent): void
}>()

const onUpdate = (value: string | boolean) => {
  emit('update:modelValue', typeof value === 'string' ? value : '')
}
</script>
