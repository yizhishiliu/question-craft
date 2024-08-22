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
        <a-menu
          mode="horizontal"
          theme="light"
          :selected-keys="selectedKeys"
          @menu-item-click="doMenuClick"
        >
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
          <a-menu-item v-for="item in visibleMenu" :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </a-menu>
      </a-col>
      <a-col flex="100px">
        <a-button type="primary" href="/user/login">登录</a-button>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { ref } from "vue";

const router = useRouter();

// 当前选中的菜单项
const selectedKeys = ref(["/"]);
// 路由跳转时，更新当前选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

// 可见菜单
const visibleMenu = routes.filter((item) => {
  if (item.meta?.HideInMenu) {
    return false;
  }
  return true;
});

const doMenuClick = (key: string) => {
  // 点击菜单跳转到对应页面
  router.push({
    path: key,
  });
};
</script>
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
