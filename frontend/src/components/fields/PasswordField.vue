<template>
  <BaseInputField
    :id="id"
    :label="label"
    :model-value="modelValue"
    type="password"
    :name="name"
    :autocomplete="autocomplete"
    :placeholder="placeholder"
    :required="required"
    :disabled="disabled"
    :helper-text="helperText"
    :error="error"
    @update:modelValue="onUpdate"
    @blur="onBlur"
  />
</template>

<script setup lang="ts">
import BaseInputField from './BaseInputField.vue'

type Props = {
  modelValue: string
  id?: string
  label?: string
  name?: string
  autocomplete?: string
  placeholder?: string
  required?: boolean
  disabled?: boolean
  helperText?: string
  error?: string
}

withDefaults(defineProps<Props>(), {
  id: 'password',
  label: 'Passwort',
  name: '',
  autocomplete: 'current-password',
  placeholder: '',
  required: true,
  disabled: false,
  helperText: '',
  error: ''
})

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void
  (event: 'blur', value: FocusEvent): void
}>()

const onUpdate = (value: string) => {
  emit('update:modelValue', value)
}

const onBlur = (event: FocusEvent) => {
  emit('blur', event)
}
</script>
