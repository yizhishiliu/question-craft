import { View } from "@tarojs/components";
import {AtButton, AtProgress, AtRadio} from "taro-ui";
import { useEffect, useState } from "react";
import Taro from "@tarojs/taro";
import GlobalFooter from "../../components/GlobalFooter";
import questions from "../../data/questions.json";
import "./index.scss";

/**
 * 做题页面
 */
export default () => {
  // 当前题目序号（从1开始）
  const [current, setCurrent] = useState<number>(1);
  // 当前题目
  const [currentQuestion, setCurrentQuestion] = useState(questions[0]);
  const questionOptions = currentQuestion.options.map((option) => {
    return { label: `${option.key}. ${option.value}`, value: option.key };
  });
  // 当前答案
  const [currentAnswer, setCurrentAnswer] = useState<string>();
  // 回答列表
  const [answerList] = useState<string[]>([]);

  // 序号变化时，切换题目
  useEffect(() => {
    setCurrentQuestion(questions[current - 1]);
    setCurrentAnswer(answerList[current - 1]);
  }, [current]);

  return (
    <View className="doQuestionPage">
      <View className="at-article__h2 title">
        {current}、{currentQuestion.title}
      </View>
      <AtRadio
        className="options-wrapper"
        options={questionOptions}
        value={currentAnswer}
        onClick={(value) => {
          setCurrentAnswer(value);
          // 记录回答
          answerList[current - 1] = value;
        }}
      />
      {current < questions.length && (
        <AtButton
          type="primary"
          circle
          className="controlBtn"
          disabled={!currentAnswer}
          onClick={() => setCurrent(current + 1)}
        >
          下一题
        </AtButton>
      )}
      {current == questions.length && (
        <AtButton
          type="primary"
          circle
          className="controlBtn"
          disabled={!currentAnswer}
          onClick={() => {
            // 传递答案列表
            Taro.setStorage({
              key:"answerList",
              data:answerList
            });
            // 跳转结果页面
            Taro.navigateTo({
              url: "/pages/result/index",
            });
          }}
        >
          查看结果
        </AtButton>
      )}
      {current > 1 && (
        <AtButton
          circle
          className="controlBtn"
          onClick={() => setCurrent(current - 1)}
        >
          上一题
        </AtButton>
      )}
      <AtProgress
        className="progress"
        // 进度条的百分比
        percent={(answerList.length / questions.length) * 100}
        // 进度条的宽度
        strokeWidth={10}
        // 进度条的颜色
        activeColor="#1aad19"
      />
      <GlobalFooter />
    </View>
  );
};
