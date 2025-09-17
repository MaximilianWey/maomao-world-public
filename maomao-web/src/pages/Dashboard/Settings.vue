<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRoute, useRouter } from "vue-router";
import ProfileSection from "@/components/Dashboard/Settings/ProfileSection.vue";
import SocialAccountsSection from "@/components/Dashboard/Settings/SocialAccountsSection.vue";

const route = useRoute();
const router = useRouter();

function scrollToHashOrTop() {
  const hash = route.hash.replace('#', '');
  if (hash) {
    const target = document.getElementById(hash);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth' });
    } else {
      // Invalid section, remove the hash and scroll to top
      router.replace({ hash: '' });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  } else {
    // No hash, scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}

onMounted(() => {
  scrollToHashOrTop();
});

watch(() => route.hash, () => {
  scrollToHashOrTop();
});

</script>

<template>
  <ProfileSection />
  <SocialAccountsSection />
</template>

<style scoped>

</style>