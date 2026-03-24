<template>
  <div class="min-h-screen flex flex-col bg-slate-950 text-slate-50 font-sans">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 px-6 py-4 flex items-center justify-between shadow-sm">
      <div 
        @click="goBack" 
        class="flex items-center text-slate-400 hover:text-indigo-400 cursor-pointer transition-colors group"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 transform group-hover:-translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span class="font-medium">返回主页</span>
      </div>
      
      <div class="flex flex-col items-center">
        <h1 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 to-blue-400">
          编程知识助手
        </h1>
        <span class="text-xs text-slate-500 mt-1 font-mono">ID: {{ chatId }}</span>
      </div>
      
      <!-- Placeholder for layout balance -->
      <div class="w-24"></div>
    </header>
    
    <!-- Main Chat Area -->
    <main class="flex-1 flex flex-col w-full max-w-5xl mx-auto p-4 md:p-6 overflow-hidden">
      <div class="flex-1 relative bg-slate-900/50 border border-slate-800 rounded-2xl shadow-xl overflow-hidden backdrop-blur-sm flex flex-col">
        <ChatRoom 
          :messages="messages" 
          :connection-status="connectionStatus"
          ai-type="coding"
          @send-message="sendMessage"
          class="h-full"
        />
      </div>
    </main>
    
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithLoveApp } from '../api'

// 设置页面标题和元数据
useHead({
  title: '编程知识助手 - AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: '编程知识助手是AI超级智能体应用平台的专业编程顾问，帮你解答各种编程问题，提供技术建议'
    },
    {
      name: 'keywords',
      content: '编程知识助手,编程顾问,技术解答,AI聊天,编程问题,AI智能体'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

const generateChatId = () => {
  return 'coding_' + Math.random().toString(36).substring(2, 10)
}

const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

const sendMessage = (message) => {
  addMessage(message, true)
  
  if (eventSource) {
    eventSource.close()
  }
  
  const aiMessageIndex = messages.value.length
  addMessage('', false)
  
  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(message, chatId.value)
  
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }
    
    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  chatId.value = generateChatId()
  addMessage('您好！我是您的专属高级研发工程师。无论是代码审查、架构设计还是Bug排查，我都随时准备为您提供专业的解答与建议。', false)
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>
