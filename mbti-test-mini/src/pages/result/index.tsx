import { View, Image } from "@tarojs/components";
import { AtButton } from "taro-ui";
import headerBg from "../../assets/headerBg.png";
import "./index.scss";
import questionResults from "../../data/question_results.json";
import GlobalFooter from "../../components/GlobalFooter";
import Taro from "@tarojs/taro";

/**
 * 测试结果页面
 */
export default () => {
  const result = questionResults[0];

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
