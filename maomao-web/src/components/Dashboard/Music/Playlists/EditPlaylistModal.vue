<script setup lang="ts">
import { ref, watch, defineEmits, defineProps } from 'vue'
import SongQueueItem from '@/components/Dashboard/Music/Queue/SongQueueItem.vue'
import draggable from 'vuedraggable'
import type { User } from '@/types/user.ts'
import type { Song } from '@/stores/songStore.ts'
import type { Visibility } from '@/types/visibility'
import PlaylistThumbnail from "@/components/Dashboard/Music/Playlists/PlaylistThumbnail.vue";
import UserMini from "@/components/Dashboard/Music/Playlists/UserMini.vue";
import {useUserStore} from "@/stores/userStore.ts";

const props = defineProps<{
  modelValue: boolean
  playlistId?: string
  initialName?: string
  initialVisibility?: Visibility
  initialSongs?: Song[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', payload: { name: string; visibility: Visibility; songs: Song[] }): void
}>()

const userStore = useUserStore()
const creator = userStore.currentUser as User

const name = ref(props.initialName ?? '')
const visibility = ref<Visibility | ''>(props.initialVisibility ?? '')
const songs = ref<Song[]>(props.initialSongs ?? [])

const nameError = ref(false)
const visibilityError = ref(false)
const errorMessage = ref('')

const hasUnsavedChanges = () => {
  const trimmedName = name.value.trim()
  return (
      trimmedName !== (props.initialName?.trim() ?? '') ||
      visibility.value !== (props.initialVisibility ?? '')
  )
}

function validateName(): boolean {
  nameError.value = false

  const trimmedName = name.value.trim()
  if (trimmedName.length === 0) {
    nameError.value = true
    errorMessage.value = 'Playlist name cannot be empty.'
    return false
  }

  if (trimmedName.length > 50) {
    nameError.value = true
    errorMessage.value = 'Playlist name must be under 50 characters.'
    return false
  }

  return true
}

function validateVisibility(): boolean {
  visibilityError.value = false

  const validVisibilities: Visibility[] = ['PRIVATE', 'PUBLIC']
  if (!validVisibilities.includes(visibility.value as Visibility)) {
    visibilityError.value = true
    errorMessage.value = 'Please select a playlist visibility.'
    return false
  }

  return true
}

function validatePlaylist(): boolean {
  errorMessage.value = ''
  const isNameValid = validateName()
  const isVisibilityValid = validateVisibility()
  return isNameValid && isVisibilityValid
}

watch(() => props.modelValue, (open) => {
  if (open) {
    name.value = props.initialName ?? ''
    visibility.value = props.initialVisibility ?? ''
    songs.value = [...(props.initialSongs ?? [])]
    clearErrors()
  }
})

function clearErrors() {
  nameError.value = false
  visibilityError.value = false
  errorMessage.value = ''
}

function close(force = false) {
  if (!force) {
    if (hasUnsavedChanges()) {
      // Show generic unsaved changes error
      errorMessage.value = 'You have unsaved changes!'
      nameError.value = name.value.trim() !== (props.initialName?.trim() ?? '')
      visibilityError.value = visibility.value !== (props.initialVisibility ?? '')
      return
    }
  }

  clearErrors()
  emit('update:modelValue', false)
}

function save() {
  if (!validatePlaylist()) return

  emit('save', {
    name: name.value.trim(),
    visibility: visibility.value as Visibility,
    songs: songs.value,
  })

  close(true)
}

function onCancelClick() {
  close(true)
}

function onNameInput() {
  nameError.value = false
  errorMessage.value = ''
}

function onDragEnd(evt: { oldIndex: number; newIndex: number }) {
  if (evt.oldIndex !== undefined && evt.newIndex !== undefined) {
    const moved = songs.value.splice(evt.oldIndex, 1)[0]
    songs.value.splice(evt.newIndex, 0, moved)
  }
}
</script>

<template>
  <div v-if="modelValue" class="modal-overlay" @click.self="close()">
    <div class="modal-content">

      <!-- Playlist header -->
      <div v-if="props.playlistId"
          class="flex items-center gap-4 mb-6">
        <div class="thumbnail-wrapper">
          <PlaylistThumbnail :songs="songs" />
        </div>
        <div class="flex flex-col">
          <h2 class="text-2xl font-bold truncate"
              style="max-width: 500px;">
            {{name}}
          </h2>
          <UserMini :user="creator" style="max-width: 500px"/>
        </div>
      </div>

      <!-- Title -->
      <h2 class="text-xl font-bold mb-4 spaced-1">Edit Playlist</h2>

      <transition name="slide-fade">
        <div v-if="errorMessage" class="input-error-box">
          {{ errorMessage }}
        </div>
      </transition>

      <!-- Name Field -->
      <div class="spaced-1">
        <label for="playlist-name" class="input-label">Playlist Name</label>
        <input
            id="playlist-name"
            v-model="name"
            @input="onNameInput"
            type="text"
            :class="['input', nameError ? 'error' : '']"
        />
      </div>

      <!-- Visibility Select -->
      <div class="spaced-1">
        <label for="playlist-visibility" class="input-label">Visibility</label>
        <select
            id="playlist-visibility"
            v-model="visibility"
            :class="['input', visibilityError ? 'error' : '']"
        >
          <option disabled value="">Select visibility</option>
          <option value="PRIVATE">Private</option>
          <option value="PUBLIC">Public</option>
        </select>
      </div>

      <!-- Song list -->
      <div v-if="songs.length > 0" class="songs-wrapper mt-4">
        <draggable
            v-model="songs"
            item-key="url"
            animation="200"
            ghost-class="ghost"
            chosen-class="chosen"
            @end="onDragEnd"
        >
          <template #item="{ element: song }">
            <SongQueueItem
                :song="song"
                :is-current="false"
                class="draggable-item spaced-0-5"
            />
          </template>
        </draggable>
      </div>

      <!-- Actions -->
      <div class="mt-6 flex justify-end gap-2 spaced-4">
        <button @click="onCancelClick" class="btn-secondary">Cancel</button>
        <button @click="save" class="btn-primary">Save</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-content {
  background: var(--bg-primary);
  padding: 2rem;
  border-radius: 0.75rem;
  width: 100%;
  max-width: 700px;
  overflow-y: auto;
  max-height: 80vh;
}

.input-label {
  display: block;
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-bottom: 0.5rem;
}

.input {
  width: 100%;
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid var(--auth-input-border);
  background: var(--bg-primary);
  color: var(--text-primary);
  outline: none;
  transition: border 0.2s;
}

.input:focus {
  border-color: var(--accent-1-hover);
}

.input.error {
  border-color: var(--auth-error-border);
}

.songs-wrapper {
  border: 1px solid var(--auth-input-border);
  border-radius: 0.5rem;
  padding: 1rem;
  max-height: 400px;
  overflow-y: auto;
  background: var(--bg-tertiary);
}

.input-error-box {
  background-color: var(--auth-error-bg);
  color: var(--auth-error-text);
  padding: 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
}

.btn-primary {
  background-color: var(--accent-1);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
}

.btn-primary:hover {
  background-color: var(--accent-1-hover);
}

.btn-secondary {
  background-color: var(--accent-2);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
}

.btn-secondary:hover {
  background-color: var(--accent-2-hover);
}

.spaced-0-5 {
  margin-top: 0.5rem;
}

.spaced-1 {
  margin-top: 1rem;
}

.spaced-4 {
  margin-top: 4rem;
}

/* Push the default dropdown arrow inward */
select.input {
  background-image: url("data:image/svg+xml;utf8,<svg fill='%23999' height='16' viewBox='0 0 24 24' width='16' xmlns='http://www.w3.org/2000/svg'><path d='M7 10l5 5 5-5z'/></svg>");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  background-size: 1.5rem 1.5rem;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  padding-right: 2.5rem;
}

.thumbnail-wrapper {
  width: 96px;
  height: 96px;
  flex-shrink: 0;
}

.thumbnail-wrapper :deep(*) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0.5rem;
}

</style>