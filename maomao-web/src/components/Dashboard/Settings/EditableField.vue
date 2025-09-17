<script setup lang="ts">
import { ref, watch } from "vue";

const props = defineProps<{
  modelValue: string;
  label: string;
  placeholder?: string;
  editable: boolean;
  saveStatus?: 'idle' | 'saving' | 'saved' | 'error';
}>();

const emit = defineEmits(['update:modelValue', 'save']);

const savingState = ref<null | 'saving' | 'saved' | 'error'>(null);
const localValue = ref(props.modelValue || '');
const isFocused = ref(false);

// Reactive border color variable
const borderColor = ref('border-neutral-300');

// Update localValue if props.modelValue changes
watch(
    () => props.modelValue,
    (val) => {
      localValue.value = val;
    }
);

// Watch savingState and update borderColor accordingly
watch(savingState, (newState) => {
  updateBorderColor(newState, isFocused.value);
});

// Update savingState when saveStatus prop changes
watch(
    () => props.saveStatus,
    (newStatus) => {
      if (newStatus === 'saving') {
        savingState.value = 'saving';
      } else if (newStatus === 'saved') {
        savingState.value = 'saved';
        setTimeout(() => (savingState.value = null), 2000);
      } else if (newStatus === 'error') {
        savingState.value = 'error';
        setTimeout(() => (savingState.value = null), 3000);
      } else {
        savingState.value = null;
      }
    }
);

function onFocus() {
  isFocused.value = true;
  updateBorderColor(savingState.value, true);
}
function onBlur() {
  isFocused.value = false;
  updateBorderColor(savingState.value, false);
}

function updateBorderColor(state: null | 'saving' | 'saved' | 'error', focused: boolean) {
  if (state === 'error') {
    borderColor.value = 'border-red-500';
  } else if (state === 'saved') {
    borderColor.value = 'border-green-500';
  } else if (state === 'saving') {
    borderColor.value = 'border-yellow-400';
  } else {
    borderColor.value = focused ? 'border-white' : 'border-neutral-300';
  }
}

function handleSave() {
  const trimmedInput = localValue.value.trim();
  const placeholderTrimmed = (props.placeholder || '').trim();

  if (!props.editable) return;

  if (trimmedInput === placeholderTrimmed || trimmedInput === '') {
    console.warn('No changes to save.');
    return;
  }

  emit('update:modelValue', localValue.value);
  emit('save', localValue.value);
}
</script>

<template>
  <div class="mb-4 relative">
    <label :for="label" class="text-sm text-muted">{{ label }}</label>

    <input
        v-model="localValue"
        :readonly="!editable"
        :disabled="!editable"
        :placeholder="placeholder"
        @focus="onFocus"
        @blur="onBlur"
        @keydown.enter.prevent="handleSave"
        :class="[
          'mt-1 rounded px-2 py-1 w-full border transition-colors duration-300 outline-none',
          !editable && 'opacity-60 cursor-not-allowed',
          savingState === 'saved' && '!border-green-500',
          savingState === 'error' && '!border-red-500',
          savingState === 'saving' && '!border-yellow-400',
        ]"
    />

    <!-- Status Message -->
    <div class="min-h-[1rem] mt-1 relative">
      <transition name="fade" mode="out-in">
        <p
            v-if="savingState"
            class="absolute left-0 text-xs"
            :class="{
            'text-muted': savingState === 'saving',
            'text-green-600': savingState === 'saved',
            'text-red-600': savingState === 'error'
          }"
        >
          {{
            savingState === 'saving'
                ? 'Saving...'
                : savingState === 'saved'
                    ? 'Saved!'
                    : 'Failed to save.'
          }}
        </p>
      </transition>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 1s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
