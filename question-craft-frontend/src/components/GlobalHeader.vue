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
          <!-- 根据路由渲染菜单 -->
          <a-menu-item v-for="item in visibleMenu" :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </a-menu>
      </a-col>
      <a-col flex="100px">
        <div v-if="loginUserStore.loginUser.id">
          {{ loginUserStore.loginUser.userName ?? "匿名用户" }}
        </div>
        <div v-else>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useLoginUserStore } from "@/store/userStore";
import checkAccess from "@/access/checkAccess";

const loginUserStore = useLoginUserStore();

const router = useRouter();

// 当前选中的菜单项
const selectedKeys = ref(["/"]);
// 路由跳转时，更新当前选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

// 可见菜单
const visibleMenu = computed(() => {
  return routes.filter((item) => {
    if (item.meta?.HideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (!checkAccess(loginUserStore.loginUser, item.meta?.access as string)) {
      return false;
    }
    return true;
  });
});

// 路由跳转事件
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
  align-items: center;
}

.title {
  margin-left: 10px;
  color: black;
  font-weight: bold;
}

.logo {
  height: 48px;
}
</style>
