<template>
  <!-- template 页面部分(内容） -->
  <div id="AppStatisticPage">
    <h2>热门应用统计</h2>
    <v-chart :option="appAnswerCountDataOption" style="height: 300px" />
    <h2>应用结果统计</h2>
    <div class="search-bar">
      <a-input-search
        :style="{ width: '320px' }"
        placeholder="请输入 appId"
        size="large"
        search-button
        @search="(value) => loadAppAnswerResultCountData(value)"
      />
    </div>
    <div style="margin-bottom: 16px"></div>
    <v-chart :option="appAnswerResultCountDataOption" style="height: 300px" />
  </div>
</template>

<script setup lang="ts">
// script 脚本部分（行为）
import { computed, ref, watchEffect } from "vue";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import {
  getAnswerCountUsingGet,
  getAnswerResultCountUsingGet,
} from "@/api/appStatisticController";
import "echarts";
import VChart from "vue-echarts";

const appAnswerList = ref<API.AppAnswerCountDTO[]>([]);
const appAnswerResultList = ref<API.AppAnswerResultCountDTO[]>([]);

/**
 * 加载数据 应用答题数量统计
 */
const loadAppAnswerCountData = async () => {
  const res = await getAnswerCountUsingGet();
  if (res.data.code === 0) {
    appAnswerList.value = res.data.data || [];
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

/**
 * 加载数据 应用答题结果统计
 * @param appId
 */
const loadAppAnswerResultCountData = async (appId: string) => {
  if (!appId) {
    return;
  }
  const res = await getAnswerResultCountUsingGet({
    appId: appId as any,
  });
  if (res.data.code === 0) {
    appAnswerResultList.value = res.data.data || [];
  } else {
    message.error("获取数据失败，" + res.data.message);
  }
};

// 应用答题数量统计图（柱状图）
const appAnswerCountDataOption = computed(() => {
  return {
    xAxis: {
      type: "category",
      data: appAnswerList.value.map((item) => item.appId),
      name: "appId",
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        data: appAnswerList.value.map((item) => item.answerCount),
        name: "做题用户数",
        type: "bar",
        showBackground: true,
        backgroundStyle: {
          color: "rgba(180, 180, 180, 0.2)",
        },
      },
    ],
  };
});

// 应用答题结果统计图（饼图）
const appAnswerResultCountDataOption = computed(() => {
  return {
    tooltip: {
      trigger: "item",
    },
    legend: {
      top: "5%",
      left: "center",
    },
    series: [
      {
        name: "Access From",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        padAngle: 5,
        itemStyle: {
          borderRadius: 10,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 40,
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: appAnswerResultList.value.map((item) => {
          return {
            value: item.resultCount,
            name: item.resultName,
          };
        }),
      },
    ],
  };
});

/**
 * 监听函数内使用到的变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  loadAppAnswerCountData();
});

watchEffect(() => {
  loadAppAnswerResultCountData("");
});
</script>

<style scoped></style>
