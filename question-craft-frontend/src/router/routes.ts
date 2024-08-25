import { RouteRecordRaw } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import ACCESS_ENUM from "@/access/accessEnum";
import NoAuthPage from "@/views/NoAuthPage.vue";
import UserRegisterPage from "@/views/UserRegisterPage.vue";
import UserLoginPage from "@/views/UserLoginPage.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: HomeView,
  },
  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthPage,
    meta: {
      HideInMenu: true,
    },
  },
  {
    path: "/admin",
    name: "管理页面",
    component: HomeView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
    },
  },
  {
    path: "/hide",
    name: "隐藏页面",
    component: HomeView,
    meta: {
      HideInMenu: true,
    },
  },
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      {
        path: "/user/login",
        name: "登录",
        component: UserLoginPage,
      },
      {
        path: "/user/register",
        name: "注册",
        component: UserRegisterPage,
      },
    ],
    meta: {
      HideInMenu: true,
    },
  },
  {
    path: "/about",
    name: "about",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
];
