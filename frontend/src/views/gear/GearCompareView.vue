<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElCard, ElButton, ElEmpty, ElMessage } from 'element-plus'
import { gearApi, type GearDetail } from '@/api/gear'
import { useCompareStore } from '@/stores/compare'

const router = useRouter()
const compareStore = useCompareStore()
const list = ref<GearDetail[]>([])
const loading = ref(true)

const ids = computed(() => compareStore.ids)

async function loadCompare() {
  const idList = ids.value
  if (idList.length < 2) {
    list.value = []
    loading.value = false
    return
  }
  loading.value = true
  try {
    const { data } = await gearApi.compare(idList)
    list.value = data
  } catch {
    ElMessage.error('加载对比数据失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

function removeFromCompare(id: number) {
  compareStore.remove(id)
}

function goDetail(id: number) {
  router.push(`/gears/${id}`)
}

watch(ids, loadCompare, { immediate: true })
</script>

<template>
  <div class="compare-page">
    <ElButton link type="primary" class="back-btn" @click="router.push('/gears')">
      ← 返回装备列表
    </ElButton>

    <ElCard v-if="ids.length < 2" shadow="never">
      <ElEmpty
        :description="
          ids.length === 1
            ? '已添加 1 个装备，请再添加至少 1 个进行对比'
            : '请选择 2～4 个装备进行对比'
        "
      >
        <p class="hint">在装备详情页点击「加入对比」添加装备</p>
        <ElButton type="primary" @click="router.push('/gears')">
          去选装备
        </ElButton>
      </ElEmpty>
    </ElCard>

    <template v-else>
      <div v-if="loading">加载中...</div>
      <div v-else class="compare-grid">
        <div
          v-for="g in list"
          :key="g.id"
          class="compare-card"
        >
          <ElButton
            class="remove-btn"
            link
            type="danger"
            size="small"
            @click.stop="removeFromCompare(g.id)"
          >
            移除
          </ElButton>
          <div class="cover" @click="goDetail(g.id)">
            <img
              v-if="g.coverImage"
              :src="g.coverImage"
              :alt="g.name"
            />
            <div v-else class="placeholder">暂无图片</div>
          </div>
          <h3 @click="goDetail(g.id)">{{ g.name }}</h3>
          <p class="meta">{{ g.categoryName }} · {{ g.brand }}</p>
          <p class="price">¥ {{ g.price }}</p>
          <div class="params">
            <div>重量: {{ g.weight ?? '-' }} g</div>
            <div>季节: {{ g.season ?? '-' }}</div>
            <div>场景: {{ g.scene ?? '-' }}</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.compare-page {
  max-width: 1200px;
  margin: 0 auto;
}
.back-btn {
  margin-bottom: 16px;
}
.hint {
  color: #909399;
  font-size: 14px;
  margin: 12px 0;
}
.compare-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.compare-card {
  position: relative;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: box-shadow 0.2s;
}
.compare-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 1;
}
.compare-card h3 {
  cursor: pointer;
}
.cover {
  height: 180px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.compare-card h3 {
  margin: 0 0 4px;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}
.price {
  margin: 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #e6a23c;
}
.params {
  font-size: 13px;
  color: #606266;
}
.params div {
  margin-bottom: 4px;
}
</style>
