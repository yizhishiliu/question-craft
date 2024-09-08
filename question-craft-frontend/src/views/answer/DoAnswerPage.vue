<template>
  <div id="doAnswerPage">
    <a-card>
      <h1>{{ app.appName }}</h1>
      <p>{{ app.appDesc }}</p>
      <h2 style="margin-bottom: 16px">
        {{ currentQuestion?.title }}
      </h2>
      <div>
        <a-radio-group
          direction="vertical"
          v-model="currentAnswer"
          :options="questionOptions"
          @change="doRadioChange"
        />
      </div>
      <div style="margin-top: 24px">
        <a-space>
          <a-button
            type="primary"
            circle
            v-if="current < questionContent.length"
            :disabled="!currentAnswer"
            @click="current += 1"
            >下一题
          </a-button>
          <a-button
            type="primary"
            circle
            v-if="current === questionContent.length"
            :disabled="!currentAnswer"
            @click="doSubmit"
            >提交
          </a-button>
          <a-button v-if="current > 1" @click="current -= 1">上一题</a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import {
  ref,
  withDefaults,
  defineProps,
  watchEffect,
  computed,
  reactive,
} from "vue";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import { getAppVoByIdUsingGet } from "@/api/appController";
import { addUserAnswerUsingPost } from "@/api/userAnswerController";

interface Props {
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const router = useRouter();

const app = ref<API.AppVO>({});

// 题目结构（题目列别）
const questionContent = ref<API.QuestionContentDTO[]>([]);

// 当前题目序号（从1开始）
const current = ref(1);

// 当前题目
const currentQuestion = ref<API.QuestionContentDTO>({});

// 当前题目选项
const questionOptions = computed(() => {
  return currentQuestion.value?.options
    ? currentQuestion.value.options.map((option) => {
        return {
          label: `${option.key}.${option.value}`,
          value: option.key,
        };
      })
    : [];
});

// 当前答案
const currentAnswer = ref<string>();

// 回答列表
const answerList = reactive<string[]>([]);

/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.appId) {
    return;
  }
  // 获取应用信息
  let res: any = await getAppVoByIdUsingGet({
    id: props.appId as any,
  });
  if (res.data.code === 0) {
    app.value = res.data.data as any;
  } else {
    message.error("获取应用失败，" + res.data.message);
  }
  // 获取题目信息
  res = await listQuestionVoByPageUsingPost({
    appId: props.appId as any,
    current: 1,
    pageSize: 1,
    sortField: "createTime",
    sortOrder: "descend",
  });
  if (res.data.code === 0 && res.data.data?.records) {
    questionContent.value = res.data.data.records[0].questionContent;
  } else {
    message.error("获取题目失败，" + res.data.message);
  }
};

// 获取旧数据
watchEffect(() => {
  loadData();
});

// 当前题目序号current变化时，更新当前题目和当前答案
watchEffect(() => {
  currentQuestion.value = questionContent.value[current.value - 1];
  currentAnswer.value = answerList[current.value - 1];
});

/**
 * 选择选项后，保存答案
 * @param value
 */
const doRadioChange = (value: string) => {
  answerList[current.value - 1] = value;
};

// 提交
const doSubmit = async () => {
  if (!props.appId || !answerList) {
    return;
  }
  const res = await addUserAnswerUsingPost({
    appId: props.appId as any,
    choices: answerList,
  });
  if (res.data.code === 0 && res.data.data) {
    router.push(`/answer/result/${res.data.data}`);
  } else {
    message.error("提交失败，" + res.data.message);
  }
};
</script>
