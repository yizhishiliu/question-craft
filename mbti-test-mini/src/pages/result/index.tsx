import { View, Image } from "@tarojs/components";
import { AtButton } from "taro-ui";
import Taro from "@tarojs/taro";
import headerBg from "../../assets/headerBg.png";
import "./index.scss";
import questionResults from "../../data/question_results.json";
import questions from "../../data/questions.json";
import GlobalFooter from "../../components/GlobalFooter";
import {getBestQuestionResult} from "../../utis/bitUtils";

/**
 * 测试结果页面
 */
export default () => {
  // 获取从答题页面传递过来的答案列表
  const answerList = Taro.getStorageSync("answerList");
  if (!answerList || answerList.length < 0) {
    Taro.showToast({
      title: "答案为空！",
      icon: "error",
      duration: 2000
    });
  }
  // 根据答案列表获取对应的测试结果
  const result = getBestQuestionResult(answerList, questions, questionResults);

  return (
    <View className="resultPage">
      <View className="at-article__h1 title">{result.resultName}</View>
      <View className="at-article__h2 subTitle">{result.resultDesc}</View>
      <AtButton type="primary" circle className="enterBtn" onClick={() => {
        // 关闭所有页面，打开到应用内的某个页面
        Taro.reLaunch({
          url: '/pages/index/index'
        })
      }}
      >
        返回主页
      </AtButton>
      <Image className="headerBg" src={headerBg} />
      <GlobalFooter />
    </View>
  );
};
