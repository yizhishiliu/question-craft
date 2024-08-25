<template>
  <div id="adminUserPage">
    <a-table
      :columns="columns"
      :data="dataList"
      :pagination="{
        showTotal: true,
        pageSize: searchParams.pageSize,
        current: searchParams.current,
        total,
      }"
      @page-change="onPageChange"
    >
      <template #userAvatar="{ record }">
        <a-image width="64" :src="record.userAvatar" />
      </template>
      <template #optional="{ record }">
        <a-button status="primary" @click="doUpdate(record)">修改</a-button>
        <a-button status="danger" @click="doDelete(record)">删除</a-button>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watchEffect } from "vue";
import {
  deleteUserUsingPost,
  listUserByPageUsingPost,
} from "@/api/userController";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";

const searchParams = ref<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
});

const dataList = ref<API.User[]>([]);
const total = ref<number>(0);

/**
 * 加载数据
 */
const loadData = async () => {
  const res = await listUserByPageUsingPost(searchParams.value);
  if (res.data.code === 0) {
    dataList.value = res.data.data?.records || [];
    total.value = res.data.data?.total || 0;
  } else {
    message.error("获取数据失败" + res.data.message);
  }
};

/**
 * 分页改变时触发，改变搜索条件，重新加载数据
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 监听 searchParams 变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadData();
});

const doDelete = async (record: API.User) => {
  if (!record.id) {
    return;
  }

  const res = await deleteUserUsingPost({ id: record.id });

  if (res.data.code === 0) {
    await loadData();
  } else {
    message.error("删除失败" + res.data.message);
  }
};

// 表格列配置
const columns = [
  {
    title: "id",
    dataIndex: "id",
  },
  {
    title: "账号",
    dataIndex: "userAccount",
  },
  {
    title: "用户名",
    dataIndex: "userName",
  },
  {
    title: "用户头像",
    dataIndex: "userAvatar",
    slotName: "userAvatar",
  },
  {
    title: "用户简介",
    dataIndex: "userProfile",
  },
  {
    title: "权限",
    dataIndex: "userRole",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];
</script>
