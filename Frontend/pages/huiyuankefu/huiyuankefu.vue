<template>
  <view class="page-container">
    <!-- 带返回按钮的导航栏 -->
  
     
   
    
    <!-- 聊天消息区域 -->
    <scroll-view class="chat-container" scroll-y="true" :scroll-top="scrollTop" scroll-with-animation="true">
      <!-- 历史记录 -->
      <view v-for="(item, index) in chatHistory" :key="index" class="message-wrapper" :class="item.type + '-wrapper'">
        <view class="message-bubble" :class="item.type + '-bubble'">
          <text class="message-content">{{ item.content }}</text>
        </view>
        <!-- 功能按钮 - 仅为AI回复显示 -->
        <view v-if="item.type === 'ai'" class="action-buttons">
          <view class="action-btn" @click="speakText(item.content)">
            <image class="action-icon" src="../../static/assets/user/icons/语音播报.png" mode="aspectFit"></image>
          </view>
          <view class="action-btn" @click="regenerateResponse">
            <image class="action-icon" src="../../static/assets/user/icons/重新加载.png" mode="aspectFit"></image>
          </view>
          <view class="action-btn" @click="copyText(item.content)">
            <image class="action-icon" src="../../static/assets/user/icons/复制文字.png" mode="aspectFit"></image>
          </view>
        </view>
      </view>
      
      <!-- 当前回复 -->
      <view v-if="doubaoAnswer && !chatHistory.some(item => item.content === doubaoAnswer)" class="message-wrapper ai-wrapper">
        <view class="message-bubble ai-bubble">
          <text class="message-content">{{ doubaoAnswer }}</text>
        </view>
        <!-- 功能按钮 -->
        <view class="action-buttons">
          <view class="action-btn" @click="speakText(doubaoAnswer)">
            <image class="action-icon" src="../../static/assets/user/icons/语音播报.png" mode="aspectFit"></image>
          </view>
          <view class="action-btn" @click="regenerateResponse">
            <image class="action-icon" src="../../static/assets/user/icons/重新加载.png" mode="aspectFit"></image>
          </view>
          <view class="action-btn" @click="copyText(doubaoAnswer)">
            <image class="action-icon" src="../../static/assets/user/icons/复制文字.png" mode="aspectFit"></image>
          </view>
        </view>
      </view>
      
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-wrapper">
        <view class="loading-bubble">
          <text class="loading-text">正在思考 |</text>
          <view class="loading-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
        <!-- 停止按钮 -->
        <view class="stop-btn" @click="stopGeneration">
          <image class="stop-icon" src="../../static/assets/user/icons/停止生成.png" mode="aspectFit"></image>
        </view>
      </view>
    </scroll-view>
    
    <!-- 智能联想话题区域 -->
    <view class="quick-replies" v-if="quickReplies.length > 0">
      <view class="topic-header">
        <text class="topic-title">你可能想问：</text>
      </view>
      <scroll-view class="quick-scroll" scroll-x="true">
        <view class="quick-item" v-for="(reply, index) in quickReplies" :key="index" @click="selectQuickReply(reply)">
          <text class="quick-text">{{ reply }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 底部输入区域 -->
     <view class="input-container">
       <view class="input-row">
         <!-- 左侧清除按钮 -->
         <button @click="clearHistory" class="clear-btn">
           <image class="clear-icon" src="../../static/assets/user/icons/清理对话历史.png" mode="aspectFit"></image>
         </button>
         
         <!-- 中间输入框 -->
         <view class="input-wrapper">
           <textarea 
             v-model="userQuestion" 
             placeholder="有什么问题尽管问我～" 
             class="message-input"
             :auto-height="true"
             :maxlength="500"
             @input="onInputChange"
           ></textarea>
         </view>
         
         <!-- 右侧发送按钮 -->
         <button 
           @click="sendDoubaoRequest" 
           :disabled="isLoading || !userQuestion.trim()"
           class="send-btn"
           :class="{ 'send-btn-active': userQuestion.trim() && !isLoading }"
         >
           <text class="send-icon">➤</text>
         </button>
       </view>
     </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CustomNavBar from '@/components/CustomNavBar.vue'
import { chatApi } from '@/common/api/index.js'
import { isLogin, getUser } from '@/common/utils/auth.js'

// 页面状态
const userQuestion = ref('')
const doubaoAnswer = ref('')
const isLoading = ref(false)
const chatHistory = ref([]) // 聊天历史记录
const scrollTop = ref(0) // 滚动位置
const quickReplies = ref([]) // 智能联想话题
const shouldStop = ref(false) // 停止标志
const kefuSessionId = ref(null) // 会员客服会话 ID（后端维护历史）

// 从本地存储加载历史记录
const loadChatHistory = () => {
  try {
    const savedHistory = uni.getStorageSync('chatHistory')
    if (savedHistory) {
      chatHistory.value = savedHistory
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  }
}

// 保存历史记录到本地存储
const saveChatHistory = () => {
  try {
    uni.setStorageSync('chatHistory', chatHistory.value)
  } catch (error) {
    console.error('保存历史记录失败:', error)
  }
}

// 会员入口拦截：未登录去登录，非会员引导开通
const guardVipEntry = () => {
  if (!isLogin()) {
    uni.showModal({
      title: '登录提醒',
      content: '会员客服需登录后使用',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/login' })
        } else {
          uni.navigateBack({ delta: 1 })
        }
      },
      fail: () => uni.navigateBack({ delta: 1 })
    })
    return
  }
  const u = getUser() || {}
  if (!u.vip) {
    uni.showModal({
      title: '会员专享',
      content: '会员客服为 VIP 专属功能，开通会员即可使用',
      confirmText: '去开通',
      cancelText: '返回',
      success: (res) => {
        if (res.confirm) {
          uni.switchTab({ url: '/pages/user/user' })
        } else {
          uni.navigateBack({ delta: 1 })
        }
      },
      fail: () => uni.navigateBack({ delta: 1 })
    })
  }
}

// 页面加载时读取历史记录，并校验会员身份
onMounted(() => {
  loadChatHistory()
  guardVipEntry()
})

// 返回按钮处理
const handleBack = () => {
  uni.navigateBack({
    delta: 1
  })
}

// 联想话题选择
const selectQuickReply = (reply) => {
  userQuestion.value = reply
  // 清空联想话题
  quickReplies.value = []
  // 直接发送请求
  sendDoubaoRequest()
}

// 生成智能联想话题（走后端大模型代理，避免暴露 API Key）
const generateRelatedTopics = async (userQuestion, aiResponse) => {
  try {
    const topicPrompt = `用户问题："${userQuestion}"\n\nAI回复："${aiResponse}"\n\n请根据用户的问题和AI的回复，生成2-3个用户可能会继续询问的相关问题。要求：\n1. 问题要简洁明了，每个问题不超过20个字\n2. 问题要与原话题相关但有所延伸\n3. 直接返回问题列表，每行一个问题，不要其他说明文字\n4. 格式示例：\n问题1\n问题2\n问题3`
    const kefuRes = await chatApi.kefu({ sessionId: null, content: topicPrompt })
    // 适配下游解析逻辑（保持原有 choices 结构）
    const response = { data: { choices: [ { message: { content: (kefuRes && kefuRes.content) || '' } } ] } }
    
    if (response.data.choices && response.data.choices.length > 0) {
      const choice = response.data.choices[0]
      if (choice.message && choice.message.content) {
        let content = ''
        if (typeof choice.message.content === 'string') {
          content = choice.message.content
        } else if (Array.isArray(choice.message.content) && choice.message.content.length > 0) {
          content = choice.message.content[0].text || choice.message.content[0]
        }
        
        // 解析生成的问题
        const topics = content.split('\n')
          .map(topic => topic.trim())
          .filter(topic => topic && topic.length > 0 && topic.length <= 30)
          .slice(0, 3) // 最多取3个
        
        if (topics.length > 0) {
          quickReplies.value = topics
        }
      }
    }
  } catch (error) {
    console.error('生成联想话题失败:', error)
    // 如果生成失败，使用默认的通用话题
    quickReplies.value = [
      '还有其他相关问题吗？',
      '能详细说说吗？',
      '有什么需要注意的？'
    ]
  }
}

// 输入变化处理
const onInputChange = () => {
  // 可以在这里添加输入处理逻辑
}

// 滚动到底部
const scrollToBottom = () => {
  uni.createSelectorQuery().select('.chat-container').boundingClientRect((rect) => {
    if (rect) {
      scrollTop.value = rect.height
    }
  }).exec()
}

// 发送请求给豆包（危险：API Key暴露在前端）
const sendDoubaoRequest = async () => {
  if (!userQuestion.value.trim()) {
    uni.showToast({ title: '请输入问题', icon: 'none' })
    return
  }
  
  const currentQuestion = userQuestion.value.trim()
  
  // 添加用户问题到历史记录
  chatHistory.value.push({
    type: 'user',
    content: currentQuestion,
    timestamp: new Date().toLocaleTimeString()
  })
  saveChatHistory() // 保存到本地存储
  
  // 滚动到底部
  setTimeout(() => {
    scrollToBottom()
  }, 100)

  isLoading.value = true
  shouldStop.value = false
  userQuestion.value = '' // 清空输入框

  try {
    if (shouldStop.value) {
      return
    }
    
    // 调用后端会员客服代理接口（/api/kefu/chat），历史消息由后端根据 sessionId 维护
    const kefuRes = await chatApi.kefu({
      sessionId: kefuSessionId.value,
      content: currentQuestion
    })
    if (kefuRes && kefuRes.sessionId) {
      kefuSessionId.value = kefuRes.sessionId
    }
    // 适配下游解析逻辑（保持原有 choices 结构）
    const response = { data: { choices: [ { message: { content: (kefuRes && kefuRes.content) || '' } } ] } }

    // 检查是否需要停止
    if (shouldStop.value) {
      return
    }
    
    // 调试：打印完整响应结构
    console.log('豆包API完整响应:', response)
    console.log('响应数据:', response.data)
    console.log('第一个选择:', response.data.choices[0])
    
    // 根据实际API响应结构解析豆包的回复
    if (response.data.choices && response.data.choices.length > 0) {
      const choice = response.data.choices[0]
      console.log('消息内容:', choice.message)
      
      // 根据实际结构获取回复内容
       if (choice.message && choice.message.content) {
         // 再次检查是否需要停止
         if (shouldStop.value) {
           return
         }
         
         let content = ''
         // 如果content是字符串
         if (typeof choice.message.content === 'string') {
           content = choice.message.content
         }
         // 如果content是数组
         else if (Array.isArray(choice.message.content) && choice.message.content.length > 0) {
           content = choice.message.content[0].text || choice.message.content[0]
         }
         
         // 清理Markdown格式符号
         content = content
           .replace(/\*\*([^*]+)\*\*/g, '$1')  // 移除加粗 **text**
           .replace(/\*([^*]+)\*/g, '$1')     // 移除斜体 *text*
           .replace(/#{1,6}\s*/g, '')        // 移除标题 # ## ###
           .replace(/`([^`]+)`/g, '$1')      // 移除行内代码 `code`
           .replace(/```[\s\S]*?```/g, '')   // 移除代码块
           .replace(/\n\s*[-*+]\s+/g, '\n')  // 移除列表符号
           .replace(/\n\s*\d+\.\s+/g, '\n')  // 移除有序列表
           .trim()
         
         // 最后检查是否需要停止
         if (shouldStop.value) {
           return
         }
         
         doubaoAnswer.value = content
         
         // 添加AI回复到历史记录
          chatHistory.value.push({
            type: 'ai',
            content: content,
            timestamp: new Date().toLocaleTimeString()
          })
          saveChatHistory() // 保存到本地存储
          
          // 滚动到底部
          setTimeout(() => {
            scrollToBottom()
          }, 100)
          
          // 生成智能联想话题
          setTimeout(() => {
            generateRelatedTopics(currentQuestion, content)
          }, 500)
       }
    } else {
      const errorMsg = '未收到有效回复'
      doubaoAnswer.value = errorMsg
      chatHistory.value.push({
         type: 'ai',
         content: errorMsg,
         timestamp: new Date().toLocaleTimeString()
       })
       saveChatHistory() // 保存到本地存储
    }
  } catch (err) {
    // 如果是主动停止，不显示错误
    if (shouldStop.value) {
      return
    }

    // 后端判定非会员（4292）：引导开通后退出本页
    if (err && err.code === 4292) {
      uni.showModal({
        title: '会员专享',
        content: '会员客服为 VIP 专属功能，开通会员即可使用',
        confirmText: '去开通',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            uni.switchTab({ url: '/pages/user/user' })
          } else {
            uni.navigateBack({ delta: 1 })
          }
        }
      })
      return
    }

    console.error('请求失败:', err)
    const errorMsg = '请求失败，请稍后重试'
    doubaoAnswer.value = errorMsg
    chatHistory.value.push({
      type: 'ai',
      content: errorMsg,
      timestamp: new Date().toLocaleTimeString()
    })
    uni.showToast({ title: '请求失败', icon: 'none' })
  } finally {
    // 只有在非停止状态下才重置加载状态
    if (!shouldStop.value) {
      isLoading.value = false
    }
  }
}

// 清除历史记录
const clearHistory = () => {
  chatHistory.value = []
  doubaoAnswer.value = ''
  quickReplies.value = [] // 清空联想话题
  kefuSessionId.value = null // 重置会话，下次重新开启
  try {
    uni.removeStorageSync('chatHistory')
  } catch (error) {
    console.error('清除本地存储失败:', error)
  }
  uni.showToast({
    title: '历史记录已清除',
    icon: 'success'
  })
}

// 语音播报
const speakText = (text) => {
  // #ifdef H5
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'zh-CN'
    speechSynthesis.speak(utterance)
  } else {
    uni.showToast({
      title: '当前环境不支持语音播报',
      icon: 'none'
    })
  }
  // #endif
  
  // #ifdef APP-PLUS || MP
  uni.showToast({
    title: '当前环境不支持语音播报',
    icon: 'none'
  })
  // #endif
}

// 重新生成回复
const regenerateResponse = () => {
  if (chatHistory.value.length > 0) {
    // 找到最后一个用户问题
    for (let i = chatHistory.value.length - 1; i >= 0; i--) {
      if (chatHistory.value[i].type === 'user') {
        userQuestion.value = chatHistory.value[i].content
        // 移除该问题之后的所有消息
        chatHistory.value = chatHistory.value.slice(0, i)
        saveChatHistory()
        sendDoubaoRequest()
        break
      }
    }
  }
}

// 复制文本
 const copyText = (text) => {
   // #ifdef H5
   if (navigator.clipboard) {
     navigator.clipboard.writeText(text).then(() => {
       uni.showToast({
         title: '复制成功',
         icon: 'success'
       })
     }).catch(() => {
       uni.showToast({
         title: '复制失败',
         icon: 'none'
       })
     })
   } else {
     // 降级方案
     const textArea = document.createElement('textarea')
     textArea.value = text
     document.body.appendChild(textArea)
     textArea.select()
     try {
       document.execCommand('copy')
       uni.showToast({
         title: '复制成功',
         icon: 'success'
       })
     } catch (err) {
       uni.showToast({
         title: '复制失败',
         icon: 'none'
       })
     }
     document.body.removeChild(textArea)
   }
   // #endif
   
   // #ifdef APP-PLUS || MP
   uni.setClipboardData({
     data: text,
     success: () => {
       uni.showToast({
         title: '复制成功',
         icon: 'success'
       })
     },
     fail: () => {
       uni.showToast({
         title: '复制失败',
         icon: 'none'
       })
     }
   })
   // #endif
 }
 
 // 停止生成
  const stopGeneration = () => {
    shouldStop.value = true
    isLoading.value = false
    doubaoAnswer.value = ''
    
    // 删除最后一个用户问题
    if (chatHistory.value.length > 0) {
      // 从后往前查找最后一个用户问题并删除
      for (let i = chatHistory.value.length - 1; i >= 0; i--) {
        if (chatHistory.value[i].type === 'user') {
          chatHistory.value.splice(i, 1)
          saveChatHistory() // 保存到本地存储
          break
        }
      }
    }
    
    // 如果有正在进行的请求，尝试中断
    // 注意：uni.request 不支持直接取消，但我们通过标志位来忽略响应
    
    uni.showToast({
      title: '已停止生成',
      icon: 'success'
    })
  }
</script>

<style>
.page-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  /* 移除单色背景，使用全局渐变背景 */
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  padding: 20rpx;
  /* 移除单色背景，使用全局渐变背景 */
}

/* 消息包装器 */
.message-wrapper {
  display: flex;
  margin-bottom: 20rpx;
  padding: 0 20rpx;
}

.user-wrapper {
  justify-content: flex-end;
}

.ai-wrapper {
  flex-direction: column;
  align-items: flex-start;
}

/* 消息气泡 */
.message-bubble {
  max-width: 70%;
  padding: 24rpx;
  border-radius: 24rpx;
  word-wrap: break-word;
  position: relative;
}

.user-bubble {
  background: linear-gradient(135deg, rgba(102, 105, 255, 1) 0%, rgba(52, 155, 255, 1) 100% 100%);
  color: white;
  border-bottom-right-radius: 8rpx;
}

.ai-bubble {
  background: white;
  color: #333;
  border-bottom-left-radius: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  border:3rpx solid #080808
}

/* 功能按钮容器 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 12rpx;
  padding: 8rpx 16rpx;
  background: white;
  border-radius: 50rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  border: 1rpx solid #e9ecef;
  width: fit-content;
  
  margin-left: 100;
}

/* 功能按钮 */
.action-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  margin-left: 15rpx;
  margin-right: 15rpx;
}

.action-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
}

.action-btn:active {
  transform: scale(0.95);
}

/* 按钮分割线 */
.action-btn:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -15rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 0.5rpx;
  height: 40rpx;
  background-color: #cbd0d6;
}

/* 功能按钮图标 */
.action-icon {
  width: 40rpx;
  height: 40rpx;
}

.message-content {
  font-size: 32rpx;
  line-height: 1.4;
  word-break: break-word;
}

/* 加载状态 */
.loading-wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 0 20rpx;
  gap: 16rpx;
}

.loading-bubble {
  background: white;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  border-bottom-left-radius: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.loading-text {
  font-size: 32rpx;
  background: linear-gradient(135deg, rgba(79, 82, 255, 1) 0%, rgba(52, 155, 255, 1) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.loading-dots {
  display: flex;
  gap: 8rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  background: #ccc;
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes loading {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 停止按钮 */
.stop-btn {
  width: 72rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  border: 2rpx solid #ffffff;
  border-radius: 90rpx;
  background-color: #feffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15), 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.stop-btn:hover {
  transform: scale(1.1);
  background-color: #feffff;

}

.stop-btn:active {
  transform: scale(0.95);
  background-color: #feffff;
}

.stop-icon {
  width: 28rpx;
  height: 28rpx;
  background-color: #feffff;
}

/* 智能联想话题 */
.quick-replies {
  padding: 20rpx;
  background: white;
  border-top: 1rpx solid #e5e5e5;
}

.topic-header {
  margin-bottom: 16rpx;
}

.topic-title {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
}

.quick-scroll {
  white-space: nowrap;
}

.quick-item {
  display: inline-block;
  margin-right: 20rpx;
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border: 1rpx solid #d1d9ff;
  border-radius: 40rpx;
  font-size: 28rpx;
  color: #4a5568;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.quick-item:active {
  background: linear-gradient(135deg, #e6edff 0%, #dae4ff 100%);
  transform: scale(0.98);
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.1);
}

.quick-text {
  font-weight: 500;
}

/* 输入容器 */
.input-container {
  background: white;
  border-top: 1rpx solid #e5e5e5;
  padding: 20rpx;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
}

.input-wrapper {
  flex: 1;
  padding: 16rpx 20rpx;
  background: rgba(247, 247, 247, 1);
  border-radius: 30rpx;
}

.message-input {
  width: 100%;
  min-height: 50rpx;
  max-height: 200rpx;
  font-size: 32rpx;
  line-height: 1.4;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
}

/* 左侧清除按钮 */
.clear-btn {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.clear-btn:active {
  transform: scale(0.95);
}

.clear-icon {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
}

.send-btn {
  width: 83rpx;
  height: 83rpx;
  border-radius: 50%;
  background: #ccc;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.send-btn-active {
  background: linear-gradient(135deg, #60666c 0%, #313435 100%);
}

.send-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}


</style>



