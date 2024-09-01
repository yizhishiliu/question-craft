<template>
  <!-- template 页面部分(内容） -->
  <div id="home">
    {{ message }}
  </div>
</template>

<script setup lang="ts">
// script 脚本部分（行为）

const message = "hello";
</script>

<style scoped>
/* style 样式部分（样式） */
#home {
}
</style>
