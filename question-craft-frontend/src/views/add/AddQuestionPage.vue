<template>
  <div id="addAppPage">
    <h2>创建题目</h2>
    <a-form
      :model="questionContent"
      :style="{ width: '500px' }"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item label="应用ID">
        {{ appId }}
      </a-form-item>
      <a-form-item label="题目列表" :content-flex="false" :merge-props="false">
        <a-button @click="addQuestion(questionContent.length)">
          底部添加题目
        </a-button>
        <!-- 遍历每道题目-->
        <div v-for="(question, index) in questionContent" :key="index">
          <a-space direction="vertical" fill>
            <a-space>
              <h3>题目 {{ index + 1 }}</h3>
              <a-button size="small" @click="addQuestion(index + 1)"
                >添加题目
              </a-button>
              <a-button
                size="small"
                status="danger"
                @click="deleteQuestion(index)"
                >删除题目
              </a-button>
            </a-space>
            <a-form-item :label="`题目 ${index + 1} 标题`">
              <a-input v-model="question.title" placeholder="请输入标题" />
            </a-form-item>
            <!-- 题目选项 start -->
            <a-space>
              <h4>题目 {{ index + 1 }} 选项列表</h4>
              <a-button
                size="small"
                @click="addQuestionOption(question, question.options.length)"
                >底部添加选项
              </a-button>
            </a-space>
            <a-form-item
              v-for="(option, optionIndex) in question.options"
              :key="optionIndex"
              :label="`选项 ${optionIndex + 1}`"
              :content-flex="false"
              :merge-props="false"
            >
              <a-form-item label="选项key">
                <a-input v-model="option.key" placeholder="请输入选项key" />
              </a-form-item>
              <a-form-item label="选项value">
                <a-input v-model="option.value" placeholder="请输入选项值" />
              </a-form-item>
              <a-form-item label="选项结果">
                <a-input v-model="option.result" placeholder="请输入选项结果" />
              </a-form-item>
              <a-form-item label="选项得分">
                <a-input-number
                  v-model="option.score"
                  label="请输入选项得分"
                ></a-input-number>
              </a-form-item>
              <a-space size="large">
                <a-button
                  size="mini"
                  @click="addQuestionOption(question, optionIndex + 1)"
                  >添加选项
                </a-button>
                <a-button
                  size="mini"
                  status="danger"
                  @click="deleteQuestionOption(question, optionIndex as any)"
                  >删除选项
                </a-button>
              </a-space>
            </a-form-item>
            <!-- 题目选项 end -->
          </a-space>
        </div>
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
import { ref, withDefaults, defineProps, watchEffect } from "vue";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import {
  addQuestionUsingPost,
  editQuestionUsingPost,
  listQuestionVoByPageUsingPost,
} from "@/api/questionController";

interface Props {
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  appId: () => {
    return "";
  },
});

const router = useRouter();

// 题目结构（题目列别）
const questionContent = ref<API.QuestionContentDTO[]>([]);

/**
 * 添加题目
 * @param index
 */
const addQuestion = (index: number) => {
  questionContent.value.splice(index, 0, {
    title: "",
    options: [],
  });
};

/**
 * 删除题目
 * @param index
 */
const deleteQuestion = (index: number) => {
  questionContent.value.splice(index, 1);
};

/**
 * 添加题目选项
 * @param question
 * @param index
 */
const addQuestionOption = (question: API.QuestionContentDTO, index: number) => {
  if (!question.options) {
    question.options = [];
  }
  question.options.splice(index, 0, {
    key: "",
    value: "",
  });
};

/**
 * 删除题目选项
 * @param question
 * @param index
 */
const deleteQuestionOption = (
  question: API.QuestionContentDTO,
  index: number
) => {
  if (!question.options) {
    question.options = [];
  }
  question.options.splice(index, 1);
};

const oldQuestion = ref<API.QuestionVO>();

/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.appId) {
    return;
  }
  const res = await listQuestionVoByPageUsingPost({
    appId: props.appId as any,
    current: 1,
    pageSize: 1,
    sortField: "createTime",
    sortOrder: "descend",
  });
  if (res.data.code === 0 && res.data.data?.records) {
    oldQuestion.value = res.data.data?.records[0];
    if (oldQuestion.value) {
      questionContent.value = oldQuestion.value.questionContent ?? [];
    }
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
  if (
    !props.appId ||
    !questionContent.value ||
    questionContent.value.length === 0
  ) {
    return;
  }
  let res: any;
  // 修改
  if (oldQuestion.value?.id) {
    res = await editQuestionUsingPost({
      id: oldQuestion.value.id,
      questionContent: questionContent.value,
    });
  } else {
    // 创建
    res = await addQuestionUsingPost({
      appId: props.appId as any,
      questionContent: questionContent.value,
    });
  }
  if (res.data.code === 0) {
    message.success("操作成功！即将跳转应用详情页...");
    setTimeout(() => {
      router.push(`/app/detail/${props.appId ?? res.data.data}`);
    }, 3000);
  } else {
    message.error("操作失败，" + res.data.message);
  }
};
</script>

<style scoped></style>
