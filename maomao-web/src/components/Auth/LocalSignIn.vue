<template>
  <form @submit.prevent="onSubmit" class="space-y-4">
    <!-- error box -->
    <transition name="slide-fade">
      <div
          v-if="errorMessage"
          class="auth-error-box"
      >
        {{ errorMessage }}
      </div>
    </transition>

    <!-- Username -->
    <div>
      <label for="login" class="auth-input-label">
        Email or Username
      </label>
      <input
          id="login"
          v-model="loginValue"
          @input="resetUsernameError"
          type="text"
          :class="[
          'auth-input',
          usernameError ? 'error' : ''
        ]"
      />
    </div>

    <!-- Password -->
    <transition name="slide-fade">
      <div v-if="showPassword">
        <label for="password" class="auth-input-label">
          Password
        </label>
        <input
            id="password"
            v-model="password"
            @input="resetPasswordError"
            type="password"
            :class="[
            'auth-input',
            passwordError ? 'error' : ''
          ]"
        />
      </div>
    </transition>

    <button
        type="submit"
        class="auth-button"
    >
      {{ showPassword ? "Sign In" : "Next" }}
    </button>
  </form>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { apiPost } from '@/services/authService';
import { AUTH_URLS } from '@/config';
import { useUserStore } from '@/stores/userStore.ts';

const router = useRouter()
const userStore = useUserStore()
const loginValue = ref('')
const password = ref('')
const showPassword = ref(false)
const errorMessage = ref('')
const usernameError = ref(false)
const passwordError = ref(false)

async function onNext() {
  clearErrors()

  const res = await fetch(AUTH_URLS.checkUser(loginValue.value))

  if (res.status === 200) {
    const payload = await res.json()
    if (payload.exists) {
      showPassword.value = true

      await nextTick(() => {
        const passwordInput = document.getElementById('password') as HTMLInputElement
        if (passwordInput) passwordInput.focus()
      })

      return
    }
  }

  if (res.status === 404) {
    errorMessage.value = 'User not found'
    usernameError.value = true
    return
  }
}

async function onSignIn() {
  clearErrors();

  // Check if the user exists (this part can remain as it is)
  const resUser = await fetch(AUTH_URLS.checkUser(loginValue.value));
  if (resUser.status !== 200) {
    errorMessage.value = 'User not found';
    usernameError.value = true;
    return;
  }

  try {
    const res = await apiPost(AUTH_URLS.signIn(), {
      identifier: loginValue.value,
      password: password.value
    });

    if (res.status === 200) {
      const { token } = res.data;

      if (token) {
        localStorage.setItem('Authorization', token);

        await userStore.fetchUserData();

        await router.push('/dash/home');
      } else {
        errorMessage.value = 'No token received';
      }
    } else if (res.status === 401) {
      errorMessage.value = 'Invalid password';
      passwordError.value = true;
    } else {
      errorMessage.value = 'An error occurred during login';
    }
  } catch (e) {
    // for some reason axios throws an error when the response is 401??
    console.error('Login error', e);
    errorMessage.value = 'Invalid password';
  }
}

function onSubmit() {
  if (!showPassword.value) onNext()
  else onSignIn()
}

function clearErrors() {
  errorMessage.value = ''
  usernameError.value = false
  passwordError.value = false
}

function resetUsernameError() {
  usernameError.value = false
}

function resetPasswordError() {
  passwordError.value = false
}
</script>

<style scoped>
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>