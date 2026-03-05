<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElRow, ElCol, ElCard, ElButton } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { gearApi, type GearListItem } from '@/api/gear'

const router = useRouter()
const authStore = useAuthStore()
const hotGears = ref<GearListItem[]>([])

async function loadHotGears() {
  try {
    const { data } = await gearApi.list({ page: 0, size: 8 })
    hotGears.value = data.content
  } catch {
    hotGears.value = []
  }
}

onMounted(loadHotGears)
</script>

<template>
  <div class="home">
    <ElRow :gutter="20">
      <ElCol :span="24">
        <ElCard shadow="hover" class="welcome-card">
          <h1>户外运动装备推荐系统</h1>
          <p>基于出行计划与用户偏好，为您推荐合适的户外装备</p>
          <div class="actions">
            <ElButton type="primary" size="large" @click="router.push('/gears')">
              浏览装备
            </ElButton>
            <ElButton size="large" @click="router.push('/plans')">
              出行计划
            </ElButton>
            <ElButton
              v-if="authStore.isLoggedIn"
              type="success"
              size="large"
              @click="router.push('/recommend')"
            >
              获取推荐
            </ElButton>
            <ElButton
              v-else
              type="success"
              size="large"
              @click="router.push('/login')"
            >
              登录后获取推荐
            </ElButton>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="16" class="hot-section">
      <ElCol :span="24">
        <ElCard shadow="hover">
          <template #header>
            <span>热门装备</span>
            <ElButton link type="primary" style="float: right" @click="router.push('/gears')">
              查看更多
            </ElButton>
          </template>
          <div v-if="hotGears.length === 0" class="empty-hint">暂无装备数据</div>
          <div v-else class="hot-grid">
            <div
              v-for="item in hotGears"
              :key="item.id"
              class="hot-item"
              @click="router.push(`/gears/${item.id}`)"
            >
              <div class="hot-cover">
                <img
                  v-if="item.coverImage"
                  :src="item.coverImage"
                  :alt="item.name"
                  onerror="this.style.display='none'"
                />
                <div v-else class="cover-placeholder">暂无图片</div>
              </div>
              <div class="hot-info">
                <h4>{{ item.name }}</h4>
                <p>¥ {{ item.price }}</p>
              </div>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.home {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.welcome-card {
  text-align: center;
  padding: 40px 20px;
}
.welcome-card h1 {
  margin-bottom: 16px;
  font-size: clamp(1.5rem, 4vw, 28px);
}
.welcome-card p {
  color: #666;
  margin-bottom: 24px;
}
.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}
.hot-section {
  margin-top: 24px;
}
.empty-hint {
  color: #909399;
  text-align: center;
  padding: 24px;
}
.hot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}
.hot-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}
.hot-item:hover {
  transform: translateY(-2px);
}
.hot-cover {
  height: 120px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hot-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  color: #909399;
  font-size: 12px;
}
.hot-info {
  padding: 8px 0;
}
.hot-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hot-info p {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
}
@media (max-width: 768px) {
  .home {
    padding: 0 12px;
  }
  .welcome-card {
    padding: 24px 16px;
  }
  .actions {
    flex-direction: column;
  }
  .actions .el-button {
    width: 100%;
  }
  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
