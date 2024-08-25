import ACCESS_ENUM from "@/access/accessEnum";

/**
 * 权限check 判断当前登录用户是否具有某个权限
 * @param loginUser 当前登录用户
 * @param needAccess 需要有的权限
 * @return boolean 有无权限
 */
const checkAccess = (
  loginUser: API.LoginUserVO,
  needAccess: string = ACCESS_ENUM.NOT_LOGIN
) => {
  // 获取当前用户具有的权限，没有 loginUser，则未登录
  const loginUserAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN;

  // 无需登录
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true;
  }

  // 需要登录
  if (needAccess === ACCESS_ENUM.USER) {
    // 如果未登录，则无权限
    if (loginUserAccess === ACCESS_ENUM.NOT_LOGIN) {
      return false;
    }
  }

  // 需要管理员权限
  if (needAccess === ACCESS_ENUM.ADMIN) {
    // 如果不是管理员，则无权限
    if (loginUserAccess !== ACCESS_ENUM.ADMIN) {
      return false;
    }
  }
  return true;
};

export default checkAccess;
