<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserIcon, LinkIcon, PowerIcon } from '@heroicons/vue/24/outline'
import { useUserStore } from '@/stores/userStore.ts'

const props = defineProps<{ 
  isDiscordLinked: boolean,
  parentButton: HTMLElement | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const menuRef = ref<HTMLElement | null>(null)
const router = useRouter()
const userStore = useUserStore()
const menuWidth = ref('100%')

onMounted(() => {
  if (props.parentButton) {
    const buttonWidth = props.parentButton.offsetWidth
    menuWidth.value = `${buttonWidth}px`
  }
})

const discordLabel = computed(() =>
    props.isDiscordLinked ? 'Discord Linked ✅' : 'Link Discord'
)

const handleEditProfile = () => {
  router.push('/dash/settings#profile')
  emit('close')
}

const handleLinkDiscord = () => {
  router.push('/dash/settings#social-accounts')
  emit('close')
}

const handleLogout = () => {
  localStorage.removeItem('Authorization');
  userStore.clearUser();
  router.push('/login');
  emit('close');
};


const onClickOutside = (e: MouseEvent) => {
  if (menuRef.value && !menuRef.value.contains(e.target as Node)) {
    emit('close')
  }
}

onMounted(() => {
  // Use setTimeout to delay adding the event listener to the next tick
  setTimeout(() => {
    document.addEventListener('click', onClickOutside)
  }, 0)
})

onUnmounted(() => document.removeEventListener('click', onClickOutside))
</script>

<template>
  <div
    ref="menuRef"
    class="account-menu roll-open"
    :style="{ width: menuWidth }"
    @click.stop
  >
    <button @click="handleEditProfile" class="account-menu-item">
      <UserIcon class="account-menu-icon" /> Edit Profile
    </button>

    <button @click="handleLinkDiscord" class="account-menu-item">
      <LinkIcon class="account-menu-icon" /> {{ discordLabel }}
    </button>

    <hr class="account-menu-divider"/>

    <button @click="handleLogout" class="account-menu-item danger">
      <PowerIcon class="account-menu-icon" /> Logout
    </button>
  </div>
</template>

<style scoped>
.account-menu {
  flex-direction: column;
  gap: 0.5rem;
  z-index: 10;
  transform-origin: top center;
  transform: scaleY(0);
  animation: roll-open 0.3s ease-out forwards;
}

.account-menu-item {
  transition: background-color 0.2s;
}

.account-menu-item:hover {
  background-color: var(--menu-item-hover);
}

@keyframes roll-open {
  from {
    transform: scaleY(0);
    opacity: 0;
  }
  to {
    transform: scaleY(1);
    opacity: 1;
  }
}
</style>