<template>
  <div class="min-h-screen bg-slate-950 text-slate-50 flex flex-col relative overflow-hidden font-sans selection:bg-indigo-500/30">
    <!-- Background Effects -->
    <div class="absolute inset-0 z-0">
      <div class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-indigo-600/20 blur-[120px]"></div>
      <div class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-blue-600/20 blur-[120px]"></div>
      <div class="absolute top-[40%] left-[60%] w-[30%] h-[30%] rounded-full bg-purple-600/20 blur-[100px]"></div>
      <!-- Grid pattern -->
      <div class="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCI+PGRlZnM+PHBhdHRlcm4gaWQ9ImdyaWQiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHBhdGggZD0iTSAwIDQwIEwgNDAgNDAgTCA0MCAwIiBmaWxsPSJub25lIiBzdHJva2U9InJnYmEoMjU1LDI1NSwyNTUsMC4wMykiIHN0cm9rZS13aWR0aD0iMSIvPjwvcGF0dGVybj48L2RlZnM+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0idXJsKCNncmlkKSIvPjwvc3ZnPg==')] z-0"></div>
    </div>

    <!-- Header / Hero Section -->
    <main class="flex-1 flex flex-col items-center justify-center w-full max-w-7xl mx-auto px-6 py-20 z-10">
      <div class="text-center mb-16 animate-fade-in-up">
        <div class="inline-flex items-center px-4 py-2 rounded-full bg-slate-800/50 border border-slate-700 backdrop-blur-sm mb-6">
          <span class="w-2 h-2 rounded-full bg-emerald-400 mr-2 animate-pulse"></span>
          <span class="text-sm font-medium text-slate-300">系统运行正常 - 智能体已就绪</span>
        </div>
        <h1 class="text-5xl md:text-7xl font-extrabold tracking-tight mb-6 bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 via-blue-400 to-purple-400 drop-shadow-sm">
          AI超级智能体
        </h1>
        <p class="text-lg md:text-xl text-slate-400 max-w-2xl mx-auto font-light leading-relaxed">
          探索下一代人工智能的无限可能。为开发者和专业人士打造的高效、智能的对话式应用平台。
        </p>
      </div>

      <!-- App Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-4xl">
        <!-- Coding Assistant Card -->
        <div 
          @click="navigateTo('/coding-assistant')"
          class="group relative bg-slate-900/40 backdrop-blur-xl border border-slate-700/50 rounded-3xl p-8 hover:bg-slate-800/60 hover:border-indigo-500/50 transition-all duration-500 cursor-pointer overflow-hidden shadow-2xl hover:shadow-indigo-500/20 transform hover:-translate-y-2"
        >
          <div class="absolute inset-0 bg-gradient-to-br from-indigo-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
          
          <div class="relative z-10">
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-3xl shadow-lg shadow-indigo-500/30 mb-6 group-hover:scale-110 transition-transform duration-500">
              💻
            </div>
            <h3 class="text-2xl font-bold text-white mb-3">编程知识助手</h3>
            <p class="text-slate-400 mb-8 line-clamp-2">
              您的私人高级研发工程师。快速解答技术难题、提供代码审查、架构建议与最佳实践指导。
            </p>
            
            <div class="flex items-center text-indigo-400 font-medium group-hover:text-indigo-300 transition-colors">
              <span>立即体验</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2 transform group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
              </svg>
            </div>
          </div>
        </div>

        <!-- Super Agent Card -->
        <div 
          @click="navigateTo('/super-agent')"
          class="group relative bg-slate-900/40 backdrop-blur-xl border border-slate-700/50 rounded-3xl p-8 hover:bg-slate-800/60 hover:border-purple-500/50 transition-all duration-500 cursor-pointer overflow-hidden shadow-2xl hover:shadow-purple-500/20 transform hover:-translate-y-2"
        >
          <div class="absolute inset-0 bg-gradient-to-br from-purple-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
          
          <div class="relative z-10">
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-purple-500 to-pink-600 flex items-center justify-center text-3xl shadow-lg shadow-purple-500/30 mb-6 group-hover:scale-110 transition-transform duration-500">
              🤖
            </div>
            <h3 class="text-2xl font-bold text-white mb-3">全能超级智能体</h3>
            <p class="text-slate-400 mb-8 line-clamp-2">
              全领域的AI助理。涵盖文案创作、数据分析、语言翻译与日常问答，解决您的各类专业需求。
            </p>
            
            <div class="flex items-center text-purple-400 font-medium group-hover:text-purple-300 transition-colors">
              <span>立即体验</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 ml-2 transform group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <div class="relative z-10">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'

// 设置页面标题和元数据
useHead({
  title: 'AI超级智能体应用平台 - 首页', // 设置页面标题
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体应用平台提供编程知识助手和AI超级智能体服务，满足您的各种AI对话需求' // 设置页面描述
    },
    {
      name: 'keywords',
      content: 'AI智能体,AI应用,编程知识助手,AI助手,智能对话,AI超级智能体,首页' // 设置页面关键词
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out forwards;
}
</style>
