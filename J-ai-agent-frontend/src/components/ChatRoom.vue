<template>
  <div class="flex flex-col h-full bg-slate-900/40 relative">
    <!-- 聊天记录区域 -->
    <div 
      class="flex-1 overflow-y-auto p-4 md:p-6 space-y-6 scroll-smooth" 
      ref="messagesContainer"
    >
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        class="flex w-full animate-fade-in-up"
        :class="msg.isUser ? 'justify-end' : 'justify-start'"
      >
        <!-- AI 消息 -->
        <div v-if="!msg.isUser" class="flex max-w-[85%] md:max-w-[75%] items-start space-x-3">
          <div class="flex-shrink-0 w-8 h-8 md:w-10 md:h-10 rounded-full flex items-center justify-center bg-gradient-to-br shadow-lg mt-1"
               :class="aiType === 'coding' ? 'from-indigo-500 to-blue-600 shadow-indigo-500/20' : 'from-purple-500 to-pink-600 shadow-purple-500/20'">
            <span class="text-white text-xs md:text-sm">{{ aiType === 'coding' ? '💻' : '🤖' }}</span>
          </div>
          
          <div class="flex flex-col space-y-1">
            <div class="bg-slate-800 text-slate-200 px-4 py-3 rounded-2xl rounded-tl-sm shadow-sm border border-slate-700/50 leading-relaxed whitespace-pre-wrap break-words text-sm md:text-base">
              {{ msg.content }}
              <span v-if="connectionStatus === 'connecting' && index === messages.length - 1" class="inline-block w-1.5 h-4 ml-1 bg-indigo-400 animate-pulse align-middle"></span>
            </div>
            <div class="text-xs text-slate-500 ml-1">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
        
        <!-- 用户消息 -->
        <div v-else class="flex max-w-[85%] md:max-w-[75%] items-start space-x-3 flex-row-reverse space-x-reverse">
          <div class="flex-shrink-0 w-8 h-8 md:w-10 md:h-10 rounded-full flex items-center justify-center bg-slate-700 shadow-lg mt-1 border border-slate-600">
            <span class="text-slate-300 text-xs md:text-sm">我</span>
          </div>
          
          <div class="flex flex-col space-y-1 items-end">
            <div class="bg-indigo-600 text-white px-4 py-3 rounded-2xl rounded-tr-sm shadow-md shadow-indigo-600/20 leading-relaxed whitespace-pre-wrap break-words text-sm md:text-base">
              {{ msg.content }}
            </div>
            <div class="text-xs text-slate-500 mr-1">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
      </div>
      
      <!-- 底部占位，防止最后一条消息被输入框遮挡 -->
      <div class="h-2"></div>
    </div>

    <!-- 输入区域 -->
    <div class="p-4 bg-slate-900/80 backdrop-blur-md border-t border-slate-800">
      <div class="relative flex items-end max-w-4xl mx-auto bg-slate-800 border border-slate-700 rounded-2xl shadow-inner overflow-hidden focus-within:border-indigo-500/50 focus-within:ring-1 focus-within:ring-indigo-500/50 transition-all">
        <textarea 
          v-model="inputMessage" 
          @keydown.enter.prevent="handleEnter"
          placeholder="输入您的消息... (Enter 发送，Shift+Enter 换行)" 
          class="w-full bg-transparent text-slate-200 placeholder-slate-500 px-4 py-4 max-h-32 min-h-[56px] resize-none outline-none text-sm md:text-base scrollbar-thin scrollbar-thumb-slate-600 scrollbar-track-transparent"
          :disabled="connectionStatus === 'connecting'"
          rows="1"
          @input="autoResize"
          ref="textareaRef"
        ></textarea>
        
        <div class="p-2 flex-shrink-0">
          <button 
            @click="sendMessage" 
            class="p-2 rounded-xl flex items-center justify-center transition-all duration-200"
            :class="canSend ? 'bg-indigo-600 text-white hover:bg-indigo-500 shadow-md shadow-indigo-600/30' : 'bg-slate-700 text-slate-500 cursor-not-allowed'"
            :disabled="!canSend"
            title="发送"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 transform rotate-90" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
            </svg>
          </button>
        </div>
      </div>
      
      <div class="text-center mt-2">
        <span class="text-[10px] text-slate-500">AI 可能会产生不准确的信息，请核实重要内容。</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'coding'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)
const textareaRef = ref(null)

const canSend = computed(() => {
  return inputMessage.value.trim().length > 0 && props.connectionStatus !== 'connecting'
})

const autoResize = () => {
  if (!textareaRef.value) return
  textareaRef.value.style.height = 'auto'
  textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 128) + 'px'
}

const handleEnter = (e) => {
  if (e.shiftKey) {
    // 允许换行
    return
  }
  sendMessage()
}

const sendMessage = () => {
  if (!canSend.value) return
  
  emit('send-message', inputMessage.value)
  inputMessage.value = ''
  
  // 重置高度
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
  })
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTo({
      top: messagesContainer.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages.map(m => m.content).join(''), () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out forwards;
}

/* 自定义滚动条 */
.scrollbar-thin::-webkit-scrollbar {
  width: 4px;
}
.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background-color: #475569;
  border-radius: 20px;
}
</style>
