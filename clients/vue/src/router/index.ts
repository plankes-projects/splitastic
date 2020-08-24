import Vue from "vue";
import VueRouter, { RouteConfig } from "vue-router";
import { RouterNames } from "@/untils/RouterNames";

Vue.use(VueRouter);

const routes: Array<RouteConfig> = [
  {
    path: "/",
    name: RouterNames.HOME,
    component: () => import("../views/Home.vue"),
  },
  {
    path: "/about",
    name: RouterNames.ABOUT,
    component: () => import("../views/About.vue"),
  },
  {
    path: "/login",
    name: RouterNames.LOGIN,
    component: () => import("../views/Login.vue"),
  },
  {
    path: "/logout",
    name: RouterNames.LOGOUT,
    component: () => import("../views/Logout.vue"),
  },
  {
    path: "/createGroup",
    name: RouterNames.CREATE_GROUP,
    component: () => import("../views/CreateGroup.vue"),
  },
  {
    path: "/showAllGroups",
    name: RouterNames.SHOW_ALL_GROUPS,
    component: () => import("../views/ShowAllGroups.vue"),
  },
  {
    path: "/group/:groupId/edit",
    name: RouterNames.EDIT_GROUP,
    component: () => import("../views/EditGroup.vue"),
  },
  {
    path: "/group/:groupId/show",
    name: RouterNames.SHOW_GROUP,
    component: () => import("../views/ShowGroup.vue"),
  },
  {
    path: "/group/:groupId/chore",
    name: RouterNames.CHORE,
    component: () => import("../views/Chore.vue"),
  },
  {
    path: "/chore/:choreId/edit",
    name: RouterNames.EDIT_CHORE,
    component: () => import("../views/EditChore.vue"),
  },
  {
    path: "/chore/:choreId/show",
    name: RouterNames.SHOW_CHORE,
    component: () => import("../views/ShowChore.vue"),
  },
  {
    path: "/group/:groupId/finance",
    name: RouterNames.FINANCE,
    component: () => import("../views/Finance.vue"),
  },
  {
    path: "/group/:groupId/finance/add",
    name: RouterNames.ADD_FINANCE,
    component: () => import("../views/AddFinance.vue"),
  },
  {
    path: "/group/:groupId/finance/:financeId/view",
    name: RouterNames.VIEW_FINANCE,
    component: () => import("../views/ViewFinance.vue"),
  },
  {
    path: "/error",
    name: RouterNames.ERROR,
    component: () => import("../views/Error.vue"),
  },
  {
    path: "/profile",
    name: RouterNames.PROFILE,
    component: () => import("../views/Profile.vue"),
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
