<template>
  <view class="container">
    <CustomNavBar :showBack="true" title="旅行助手小趣" @back="handleBack" />
    
    <!-- 对话历史显示区域 -->
    <scroll-view 
      class="chat-messages" 
      scroll-y="true" 
      :scroll-top="scrollTop"
      @scroll="onScroll"
    >
      <!-- 系统提示消息 -->
      <view class="system-message" v-if="messages.length === 1 && messages[0].role === 'system'">
        {{ messages[0].content }}
      </view>
      
      <!-- 对话消息 -->
      <view 
        v-for="(message, index) in validMessages" 
        :key="index" 
        class="message-item"
        :class="{
          'user-message': message.role === 'user',
          'assistant-message': message.role === 'assistant'
        }"
      >
        <view class="avatar">
          <image 
            :src="message.role === 'user' ? '/static/chat/user-avatar.png' : '/static/chat/assistant-avatar.jpeg'" 
            mode="widthFix"
          ></image>
        </view>
        <view class="message-content">
          <!-- 消息气泡 -->
          <view class="message-bubble">
            {{ message.content }}
          </view>
          <!-- 操作按钮组：复制和重新生成 -->
          <view class="action-buttons" v-if="message.isComplete">
            <button 
              class="copy-btn" 
              @click.stop="copyToClipboard(message.content)"
              :class="message.role === 'user' ? 'user-btn' : 'assistant-btn'"
            >
              <image src="/static/chat/copy-icon.png" mode="contain"></image>
            </button>
            
            <!-- 重新生成按钮：仅助手消息显示 -->
            <button 
              class="regenerate-btn" 
              @click.stop="regenerateMessage(index)"
              :class="message.role === 'user' ? 'user-btn' : 'assistant-btn'"
              v-if="message.role === 'assistant' && !isLoading"
            >
              <image src="/static/chat/regenerate-icon.png" mode="contain"></image>
            </button>
          </view>
        </view>
      </view>
      
      <!-- 加载指示器 -->
      <view v-if="isLoading" class="loading-indicator">
        <view class="loading-dots">
          <view class="dot"></view>
          <view class="dot" style="animation-delay: 150ms"></view>
          <view class="dot" style="animation-delay: 300ms"></view>
        </view>
        <text>小趣正在思考...</text>
      </view>
    </scroll-view>
    
    <!-- 输入区域 -->
    <view class="input-area">
      <input 
        v-model="userInput" 
        type="text" 
        class="message-input"
        placeholder="有什么旅行问题想咨询小趣？"
        @confirm="sendMessage"
        @input="autoResizeInput"
        :style="{height: inputHeight}"
      >
      <button 
        @click="sendMessage"
        class="send-btn"
        :disabled="!userInput.trim() || isLoading"
      >
        <image src="/static/chat/send-icon.png" mode="widthFix" v-if="userInput.trim() && !isLoading"></image>
        <view class="loading-small" v-else-if="isLoading">
          <view class="dot-small"></view>
        </view>
      </button>
    </view>
  </view>
</template>

<script>
import CustomNavBar from '@/components/CustomNavBar.vue'

export default {
  components: {
    CustomNavBar
  },
  data() {
    return {
      userInput: '',
      messages: [
        { role: 'system', content: '您好！我是旅行助手小趣，可为您提供行程规划、景点推荐、美食介绍等服务，您有什么想问的都可以为您解答~' }
      ],
      isLoading: false,
      scrollTop: 0,
      inputHeight: '80rpx',
      minInputHeight: '80rpx',
      maxInputHeight: '200rpx',
      apiKey: 'ms-8b15efbb-777c-484b-bc53-800fdaf78b73',
      modelName: 'Qwen/Qwen3-235B-A22B-Instruct-2507',
      baseUrl: 'https://api-inference.modelscope.cn/v1'
    };
  },
  computed: {
    validMessages() {
      return this.messages.filter(msg => {
        return msg && typeof msg === 'object' && 'role' in msg && 'content' in msg;
      }).filter(msg => msg.role !== 'system');
    }
  },
  onReady() {
    this.scrollToBottom();
  },
  methods: {
    handleBack() {
      uni.navigateBack({ delta: 1 });
    },
    
    sendMessage() {
      const input = this.userInput.trim();
      if (!input || this.isLoading) return;

      // 用户消息：发送后立即标记为"完整"
      this.messages.push({ 
        role: 'user', 
        content: input,
        isComplete: true
      });
      this.userInput = '';
      this.isLoading = true;
      this.scrollToBottom();
      
      // 构造请求数据（包含所有有效对话）
      const requestData = {
        model: this.modelName,
        messages: this.validMessages,
        stream: true
      };

      // 添加新的助手消息占位
      const assistantMessage = { 
        role: 'assistant', 
        content: '',
        isComplete: false
      };
      this.messages.push(assistantMessage);
      const assistantMessageIndex = this.messages.length - 1;

      this.fetchAssistantResponse(requestData, assistantMessageIndex);
    },
    
    // 重新生成消息 - 核心修改部分
    regenerateMessage(assistantMsgIndex) {
      // 找到对应的用户消息（助手消息前一条应该是用户消息）
      const userMsgIndex = assistantMsgIndex - 1;
      if (userMsgIndex < 0 || this.validMessages[userMsgIndex].role !== 'user') {
        this.showToast('无法找到对应的问题');
        return;
      }
      
      // 避免重复操作
      if (this.isLoading) return;
      
      // 1. 移除旧的助手消息（从原始messages数组中）
      // 计算在原始messages数组中的索引（因为validMessages过滤了system消息）
      const originalMsgIndex = this.messages.findIndex(
        (msg, idx) => idx > 0 && this.validMessages[assistantMsgIndex] === msg
      );
      
      if (originalMsgIndex !== -1) {
        this.messages.splice(originalMsgIndex, 1);
      }
      
      // 2. 准备新的请求数据（包含到该用户消息为止的所有对话）
      const requestMessages = this.validMessages.slice(0, userMsgIndex + 1);
      const requestData = {
        model: this.modelName,
        messages: requestMessages,
        stream: true
      };
      
      // 3. 添加新的助手消息占位
      const newAssistantMessage = { 
        role: 'assistant', 
        content: '',
        isComplete: false
      };
      this.messages.push(newAssistantMessage);
      const newAssistantIndex = this.messages.length - 1;
      
      // 4. 发起请求获取新回答
      this.isLoading = true;
      this.scrollToBottom();
      this.fetchAssistantResponse(requestData, newAssistantIndex);
    },
    
    // 提取公共的请求方法
    fetchAssistantResponse(requestData, messageIndex) {
      uni.request({
        url: `${this.baseUrl}/chat/completions`,
        method: 'POST',
        data: requestData,
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`
        },
        responseType: 'text',
        success: (response) => {
          if (response.statusCode === 200) {
            this.handleStreamResponse(response.data, messageIndex);
          } else {
            this.handleError(`API返回错误: ${response.statusCode}`);
          }
        },
        fail: (error) => {
          this.handleError(`请求失败: ${error.errMsg}`);
        }
      });
    },
    
    handleStreamResponse(data, index) {
      try {
        if (index < 0 || index >= this.messages.length) {
          throw new Error(`无效的消息索引: ${index}`);
        }
        
        const dataStr = typeof data === 'string' ? data : String(data);
        const lines = dataStr.split('\n').filter(line => line.trim() !== '');
        
        for (const line of lines) {
          const dataStr = line.replace(/^data: /, '');
          if (dataStr === '[DONE]') break;
          
          try {
            const json = JSON.parse(dataStr);
            const content = json.choices[0]?.delta?.content;
            if (content && this.messages[index]) {
              this.messages[index].content += content;
              this.scrollToBottom();
              this.$forceUpdate();
            }
          } catch (e) {
            console.error('解析响应片段出错:', e, '内容:', dataStr);
          }
        }
      } catch (e) {
        console.error('处理响应出错:', e);
        this.handleError(`处理响应时出错: ${e.message}`);
      } finally {
        // 流式响应结束：标记为完整
        if (this.messages[index]) {
          this.messages[index].isComplete = true;
        }
        this.isLoading = false;
        this.scrollToBottom();
      }
    },
    
    handleError(message) {
      console.error(message);
      this.messages.push({ 
        role: 'assistant', 
        content: `抱歉，出现错误: ${message}`,
        isComplete: true
      });
      this.isLoading = false;
      this.scrollToBottom();
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const query = uni.createSelectorQuery().in(this);
        query.select('.chat-messages').boundingClientRect(data => {
          if (data) {
            this.scrollTop = data.scrollHeight;
          }
        }).exec();
      });
    },
    
    onScroll(e) {
      this.scrollTop = e.detail.scrollTop;
    },
    
    autoResizeInput() {
      const input = uni.createSelectorQuery().in(this).select('.message-input');
      input.fields({ size: true }, data => {
        if (data) {
          let height = data.height;
          if (height < this.minInputHeight) {
            this.inputHeight = this.minInputHeight;
          } else if (height > this.maxInputHeight) {
            this.inputHeight = this.maxInputHeight;
          } else {
            this.inputHeight = `${height}px`;
          }
        }
      }).exec();
    },
    
    copyToClipboard(content) {
      uni.setClipboardData({
        data: content,
        success: () => {
          this.showToast('复制成功');
        },
        fail: (err) => {
          this.showToast('复制失败，请重试');
          console.error('复制失败:', err);
        }
      });
    },
    
    showToast(title) {
      uni.showToast({
        title: title,
        icon: 'none',
        duration: 2000
      });
    }
  }
};
</script>

<style lang="scss" scoped>
/* 样式部分与之前保持一致 */
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
  box-sizing: border-box;
  padding-bottom: env(safe-area-inset-bottom);
}

.chat-messages {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
  overflow: auto;
}

.system-message {
  background-color: #2eb6ff;
  color: #ffffff;
  font-size: 40rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  margin: 20rpx auto;
  max-width: 80%;
  text-align: start;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
  max-width: 85%;
  
  &.user-message {
    flex-direction: row-reverse;
    margin-left: auto;
  }
  
  &.assistant-message {
    margin-right: auto;
  }
}

.avatar {
  width: 64rpx;
  height: 64rpx;
  margin: 0 16rpx;
  flex-shrink: 0;
  
  image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
  }
}

.message-content {
  display: inline-block; 
  max-width: calc(100% - 80rpx);
  position: relative;
  padding-bottom: 30rpx;
}

.message-bubble {
  display: inline-block;
  padding: 20rpx 24rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
  max-width: 100%;
}

// 操作按钮组
.action-buttons {
  display: flex;
  position: absolute;
  gap: 10rpx; // 按钮之间的间距
}

// 复制按钮样式
.copy-btn {
  width: 44rpx;
  height: 44rpx;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  opacity: 0.7;
  transition: opacity 0.3s;
  z-index: 10;
  
  &:hover {
    opacity: 1;
  }
  
  image {
    width: 24rpx;
    height: 24rpx;
    display: block;
  }
  
  &::after {
    border: none;
  }
}

// 重新生成按钮样式
.regenerate-btn {
  width: 44rpx;
  height: 44rpx;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  opacity: 0.7;
  transition: opacity 0.3s;
  z-index: 10;
  
  &:hover {
    opacity: 1;
  }
  
  image {
    width: 24rpx;
    height: 24rpx;
    display: block;
  }
  
  &::after {
    border: none;
  }
}

// 用户消息的按钮位置
.user-btn {
  bottom: -15rpx;
}

.user-message .action-buttons {
  left: 10rpx;
}

// 助手消息的按钮位置
.assistant-btn {
  bottom: -15rpx;
}

.assistant-message .action-buttons {
  right: 5rpx;
}

// 按钮图标颜色调整
.user-btn image {
  filter: brightness(1.5);
}

.assistant-btn image {
  filter: brightness(1.5);
}

.user-message .message-bubble {
  background-color: #1677ff;
  color: #fff;
  border-top-right-radius: 6rpx;
}

.assistant-message .message-bubble {
  background-color: #fff;
  color: #333;
  border-top-left-radius: 6rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.input-area {
  display: flex;
  align-items: flex-end;
  padding: 16rpx;
  background-color: #fff;
  border-top: 1px solid #eee;
  box-sizing: border-box;
}

.message-input {
  flex: 1;
  min-height: 80rpx;
  max-height: 200rpx;
  border: 2rpx solid #eee;
  border-radius: 40rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  line-height: 1.5;
  resize: none;
  overflow-y: auto;
  box-sizing: border-box;
}

.send-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: #1677ff;
  color: #fff;
  border: none;
  margin-left: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  image {
    width: 40rpx;
    height: 40rpx;
  }
  
  &:disabled {
    background-color: #eee;
  }
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20rpx 0;
  color: #999;
  font-size: 26rpx;
}

.loading-dots {
  display: flex;
  margin-right: 16rpx;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  background-color: #1677ff;
  border-radius: 50%;
  margin: 0 4rpx;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-small {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot-small {
  width: 12rpx;
  height: 12rpx;
  background-color: #fff;
  border-radius: 50%;
  animation: spin 1s infinite linear;
}

@keyframes bounce {
  0%, 100% { transform: scale(0); }
  50% { transform: scale(1); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>