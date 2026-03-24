<template>
  <div class="min-h-screen flex flex-col bg-slate-950 text-slate-50 font-sans">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-md border-b border-slate-800 px-6 py-4 flex items-center justify-between shadow-sm">
      <div 
        @click="goBack" 
        class="flex items-center text-slate-400 hover:text-purple-400 cursor-pointer transition-colors group"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 transform group-hover:-translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span class="font-medium">返回主页</span>
      </div>
      
      <div class="flex flex-col items-center">
        <h1 class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-purple-400 to-pink-400">
          全能超级智能体
        </h1>
        <span class="text-xs text-slate-500 mt-1 flex items-center">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 mr-1 animate-pulse"></span>
          在线
        </span>
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
          ai-type="super"
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
import { chatWithManus } from '../api'

useHead({
  title: 'AI超级智能体 - AI超级智能体应用平台',
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体是AI超级智能体应用平台的全能助手，能解答各类专业问题，提供精准建议和解决方案'
    },
    {
      name: 'keywords',
      content: 'AI超级智能体,智能助手,专业问答,AI问答,专业建议,AI智能体'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime()
  })
}

const sendMessage = (message) => {
  addMessage(message, true, 'user-question')
  
  if (eventSource) {
    eventSource.close()
  }
  
  connectionStatus.value = 'connecting'
  
  let messageBuffer = [];
  let lastBubbleTime = Date.now();
  let isFirstResponse = true;
  
  const chineseEndPunctuation = ['。', '！', '？', '…'];
  const minBubbleInterval = 800;
  
  const createBubble = (content, type = 'ai-answer') => {
    if (!content.trim()) return;
    
    const now = Date.now();
    const timeSinceLastBubble = now - lastBubbleTime;
    
    if (isFirstResponse) {
      addMessage(content, false, type);
      isFirstResponse = false;
    } else if (timeSinceLastBubble < minBubbleInterval) {
      setTimeout(() => {
        addMessage(content, false, type);
      }, minBubbleInterval - timeSinceLastBubble);
    } else {
      addMessage(content, false, type);
    }
    
    lastBubbleTime = now;
    messageBuffer = [];
  };
  
  eventSource = chatWithManus(message)
  
  eventSource.onmessage = (event) => {
    const data = event.data
    
    if (data && data !== '[DONE]') {
      messageBuffer.push(data);
      
      const combinedText = messageBuffer.join('');
      const lastChar = data.charAt(data.length - 1);
      const hasCompleteSentence = chineseEndPunctuation.includes(lastChar) || data.includes('\n\n');
      const isLongEnough = combinedText.length > 40;
      
      if (hasCompleteSentence || isLongEnough) {
        createBubble(combinedText);
      }
    }
    
    if (data === '[DONE]') {
      if (messageBuffer.length > 0) {
        const remainingContent = messageBuffer.join('');
        createBubble(remainingContent, 'ai-final');
      }
      
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
    
    if (messageBuffer.length > 0) {
      const remainingContent = messageBuffer.join('');
      createBubble(remainingContent, 'ai-error');
    }
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  addMessage('您好！我是全能AI超级智能体。无论您面临任何领域的专业问题，还是需要文案、翻译或数据分析，我都随时为您效劳。', false)
})

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>
