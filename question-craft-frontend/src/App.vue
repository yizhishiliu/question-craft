<template>
  <div id="app">
    <template v-if="route.path.startsWith('/user')">
      <router-view />
    </template>
    <template v-else>
      <BasicLayout />
    </template>
  </div>
</template>
<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout.vue";
import { useRoute } from "vue-router";
import { getLoginUserUsingGet } from "@/api/userController";
import { useLoginUserStore } from "@/store/userStore";

// useRoute获取当前页面信息
const route = useRoute();

const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser();

getLoginUserUsingGet().then((res) => {
  console.log(res);
});
</script>
<style scoped>
#app {
}
</style>
