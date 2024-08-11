import { View, Image } from "@tarojs/components";
import { AtButton } from "taro-ui";
import headerBg from "../../assets/headerBg.png";
import "./index.scss";
import GlobalFooter from "../../components/GlobalFooter";

/**
 * 主页
 */
export default () => {
  return (
    <View className="indexPage">
      <View className="at-article__h1 title">MBTI 性格测试</View>
      <View className="at-article__h2 subTitle">只需2分钟，就能“惊人般准确”地描述出你是谁，以及你为何以这样的方式行事。</View>
      <AtButton type="primary" circle className="enterBtn">
        开始测试
      </AtButton>
      <Image className="headerBg" src={headerBg} />
      <GlobalFooter />
    </View>
  );
};
