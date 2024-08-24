<template>
  <div id="basicLayout">
    <a-layout style="height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader></GlobalHeader>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://github.com/yizhishiliu" target="_blank"
          >智编题海 by 一之十六</a
        >
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>

<style scoped>
#basicLayout {
}

#basicLayout .header {
  margin-bottom: 16px;
  box-shadow: #eee 1px 1px 5px;
}

#basicLayout .content {
  margin-bottom: 10px;
  padding: 20px;
  background: linear-gradient(to bottom, #fff, #f0f0f0);
}

.footer {
  margin-bottom: 10px;
  text-align: center;
  background-color: #fff;
}

a {
  text-decoration: none;
}
</style>
