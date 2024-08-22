<template>
  <div id="globalHeader">
    <a-row
      class="grid-demo"
      align="center"
      :wrap="false"
      style="margin-bottom: 16px"
    >
      <!--  wrap="false" 不换行（缩小） -->
      <a-col flex="auto">
        <a-menu mode="horizontal" theme="light" :default-selected-keys="['1']">
          <a-menu-item
            key="0"
            :style="{ padding: 0, marginRight: '38px' }"
            disabled
          >
            <div class="titleBar">
              <img class="logo" src="../assets/logo.png" alt="" />
              <div class="title">智编题海</div>
            </div>
          </a-menu-item>
          <a-menu-item key="1">Home</a-menu-item>
          <a-menu-item key="2">Solution</a-menu-item>
          <a-menu-item key="3">Cloud Service</a-menu-item>
          <a-menu-item key="4">Cooperation</a-menu-item>
        </a-menu>
      </a-col>
      <a-col flex="100px">
        <a-button type="primary" href="/user/login">登录</a-button>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts"></script>
<style scoped>
#globalHeader {
}

.titleBar {
  display: flex;
  align-items: center; /* 垂直居中 */
}

.title {
  font-weight: bold; /* 加粗 */
  color: black;
  margin-left: 10px;
}

.logo {
  height: 48px;
}
</style>
