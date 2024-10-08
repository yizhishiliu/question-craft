<template>
  <div id="addAppPage">
    <h2>创建应用</h2>
    <a-form
      :model="form"
      :style="{ width: '500px' }"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item label="应用名称" field="appName">
        <a-input v-model="form.appName" placeholder="请输入应用名称" />
      </a-form-item>
      <a-form-item label="应用描述" field="appDesc">
        <a-input v-model="form.appDesc" placeholder="请输入应用描述" />
      </a-form-item>
      <a-form-item label="应用图标" field="appIcon">
        <a-input v-model="form.appIcon" placeholder="请输入应用图标" />
      </a-form-item>
      <a-form-item label="应用类型" field="appType">
        <a-select
          v-model="form.appType"
          :style="{ width: '320px' }"
          placeholder="请选择应用类型"
        >
          <a-option
            v-for="(value, key) of APP_TYPE_MAP"
            :key="key"
            :value="Number(key)"
            :label="value"
          ></a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="评分策略" field="scoringStrategy">
        <a-select
          v-model="form.scoringStrategy"
          :style="{ width: '320px' }"
          placeholder="请选择评分策略"
        >
          <a-option
            v-for="(value, key) of APP_SCORING_STRATEGY_MAP"
            :key="key"
            :value="Number(key)"
            :label="value"
          ></a-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 120px"
          >提交
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import {
  addAppUsingPost,
  editAppUsingPost,
  getAppVoByIdUsingGet,
} from "@/api/appController";
import { ref, withDefaults, defineProps, watchEffect } from "vue";
import API from "@/api";
import { APP_SCORING_STRATEGY_MAP, APP_TYPE_MAP } from "@/constant/app";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => {
    return "";
  },
});

const router = useRouter();

const form = ref({
  appDesc: "",
  appIcon: "",
  appName: "",
  appType: 0,
  scoringStrategy: 0,
} as API.AppAddRequest);

const oldApp = ref<API.AppVO>();

/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.id) {
    return;
  }
  const res = await getAppVoByIdUsingGet({
    id: props.id as any,
  });
  if (res.data.code === 0 && res.data.data) {
    oldApp.value = res.data.data as any;
    form.value = res.data.data;
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

// 获取旧数据
watchEffect(() => {
  loadData();
});

// 提交
const handleSubmit = async () => {
  let res: any;
  // 修改
  if (props.id) {
    res = await editAppUsingPost({
      id: props.id as any,
      ...form.value,
    });
  } else {
    // 创建
    res = await addAppUsingPost(form.value);
  }
  if (res.data.code === 0) {
    message.success("操作成功！即将跳转应用详情页...");
    setTimeout(() => {
      router.push(`/app/detail/${props.id || res.data.data}`);
    }, 3000);
  } else {
    message.error("操作失败，" + res.data.message);
  }
};
</script>

<style scoped></style>
