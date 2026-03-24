import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - AI超级智能体应用平台',
      description: 'AI超级智能体应用平台提供编程知识助手和AI超级智能体服务，满足您的各种AI对话需求'
    }
  },
  {
    path: '/coding-assistant',
    name: 'CodingAssistant',
    component: () => import('../views/LoveMaster.vue'),
    meta: {
      title: '编程知识助手 - AI超级智能体应用平台',
      description: '编程知识助手是AI超级智能体应用平台的专业编程顾问，帮你解答各种编程问题，提供技术建议'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI超级智能体 - AI超级智能体应用平台', // 设置页面标题
      description: 'AI超级智能体是AI超级智能体应用平台的全能助手，能解答各类专业问题，提供精准建议和解决方案' // 设置页面描述
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 