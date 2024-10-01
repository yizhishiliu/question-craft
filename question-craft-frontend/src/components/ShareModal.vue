<template>
  <a-modal v-model:visible="visible" @cancel="closeModal" :footer="false">
    <template #title>
      {{ title }}
    </template>
    <h4 style="margin-top: 0">复制分享链接</h4>
    <a-typography-paragraph copyable>
      {{ link }}
    </a-typography-paragraph>
    <h4>手机扫码查看</h4>
    <img :src="codeImg" />
  </a-modal>
</template>

<script setup lang="ts">
import { defineProps, ref, withDefaults, defineExpose } from "vue";
// @ts-ignore
import QRCode from "qrcode";

// 定义组件属性类型
interface Props {
  // 分享标题
  title: string;
  // 分享连接
  link: string;
}

// 给组件定义初始值
const props = withDefaults(defineProps<Props>(), {
  title: "分享",
  link: "https://github.com/yizhishiliu",
});

// 是否可见
const visible = ref(false);

// 要展示的图片
const codeImg = ref();

// 打开弹窗
const openModal = () => {
  visible.value = true;
};

// 关闭弹窗
const closeModal = () => {
  visible.value = false;
};

// 二维码生成
QRCode.toDataURL(props.link)
  .then((url: string) => {
    codeImg.value = url;
  })
  .catch((err: any) => {
    console.error(err);
  });

// 暴露函数给父组件
defineExpose({
  openModal,
});
</script>

<style scoped></style>
