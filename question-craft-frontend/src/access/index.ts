import router from "@/router";
import { useLoginUserStore } from "@/store/userStore";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";

// 进入页面前，进行权限校验
router.beforeEach(async (to, from, next) => {
  // 获取当前登录用户
  const loginUserStore = useLoginUserStore();
  let loginUser = loginUserStore.loginUser;

  // 如果之前没有尝试获取过登录信息，才自动登录
  if (!loginUser || !loginUser.userRole) {
    // await 等待用户登录成功并获取到登录信息后，才执行后续操作
    await loginUserStore.fetchLoginUser();
    loginUser = loginUserStore.loginUser;
  }

  // 当前页面需要的权限
  const needAccess = to.meta?.access ?? ACCESS_ENUM.NOT_LOGIN;

  // 要跳转的页面需要登录权限
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 如果当前用户未登录，则跳转到登录页面
    if (
      !loginUser ||
      !loginUser.userRole ||
      loginUser.userRole === ACCESS_ENUM.NOT_LOGIN
    ) {
      next("/user/login?redirect=" + to.fullPath);
    }

    // 如果当前用户已登录，则判断权限是否足够，不足则跳转无权限页面
    if (!checkAccess(loginUser, needAccess as string)) {
      next("/noAuth");
      return;
    }
  }
  next();
});
