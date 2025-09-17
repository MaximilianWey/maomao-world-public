<script setup lang="ts">
import AuthHeader from "../../components/Auth/AuthHeader.vue";
import LocalSignIn from "../../components/Auth/LocalSignIn.vue";
import OrDivider from "../../components/Auth/OrDivider.vue";
import SocialProviders from "../../components/Auth/SocialProviders.vue";
import AuthFooter from "../../components/Auth/AuthFooter.vue";
</script>

<template>
  <div class="auth-container flex flex-col space-y-8">
    <AuthHeader />
    <LocalSignIn />
    <OrDivider />
    <SocialProviders />
    <AuthFooter />
  </div>
</template>

<style scoped></style>