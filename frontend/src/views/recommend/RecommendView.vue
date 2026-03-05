<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElCard,
  ElSelect,
  ElOption,
  ElRow,
  ElCol,
  ElButton,
  ElEmpty,
  ElMessage,
  ElInputNumber,
} from 'element-plus'
import { recommendApi } from '@/api/recommend'
import { userApi } from '@/api/user'
import { planApi, type TripPlan } from '@/api/plan'
import type { RecommendItem } from '@/api/recommend'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const plans = ref<TripPlan[]>([])
const selectedPlanId = ref<number | null>(null)
const list = ref<RecommendItem[]>([])
const loading = ref(false)
const cartQuantity = ref<Record<number, number>>({})
function getCartQty(gearId: number) {
  return cartQuantity.value[gearId] ?? 1
}
function setCartQty(gearId: number, v: number) {
  cartQuantity.value[gearId] = v
}

const planIdFromQuery = computed(() => {
  const q = route.query.planId
  if (typeof q === 'string') {
    const n = parseInt(q, 10)
    return !isNaN(n) ? n : null
  }
  return null
})

async function loadPlans() {
  if (!authStore.isLoggedIn) return
  try {
    const { data } = await planApi.list(0, 100)
    plans.value = data.content
    if (plans.value.length > 0 && !selectedPlanId.value) {
      const fromQuery = planIdFromQuery.value
      if (fromQuery && plans.value.some((p) => p.id === fromQuery)) {
        selectedPlanId.value = fromQuery
      } else {
        selectedPlanId.value = plans.value[0].id
      }
    }
  } catch {
    ElMessage.error('加载计划列表失败')
  }
}

async function loadRecommend() {
  if (!selectedPlanId.value) {
    list.value = []
    return
  }
  loading.value = true
  try {
    const { data } = await recommendApi.byPlan(selectedPlanId.value, 20)
    list.value = data
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '获取推荐失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function addFavorite(gearId: number) {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await userApi.addFavorite(gearId)
    ElMessage.success('已收藏')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '收藏失败')
  }
}

async function addToCart(item: RecommendItem) {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  const qty = getCartQty(item.gearId)
  try {
    await userApi.addToCart(item.gearId, qty)
    ElMessage.success('已加入购物车')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '加购失败')
  }
}

function goDetail(gearId: number) {
  router.push(`/gears/${gearId}`)
}

watch(selectedPlanId, loadRecommend)
watch(planIdFromQuery, (id) => {
  if (id) selectedPlanId.value = id
})

onMounted(() => {
  if (authStore.isLoggedIn) {
    loadPlans()
  }
})
</script>

<template>
  <div class="recommend-page">
    <ElCard>
      <template #header>
        <span>装备推荐</span>
      </template>

      <template v-if="!authStore.isLoggedIn">
        <ElEmpty description="请先登录后获取推荐">
          <ElButton type="primary" @click="router.push('/login')">
            去登录
          </ElButton>
        </ElEmpty>
      </template>

      <template v-else-if="plans.length === 0">
        <ElEmpty description="请先创建出行计划">
          <ElButton type="primary" @click="router.push('/plans')">
            去创建计划
          </ElButton>
        </ElEmpty>
      </template>

      <template v-else>
        <div class="plan-select">
          <span>选择计划：</span>
          <ElSelect
            v-model="selectedPlanId"
            placeholder="选择出行计划"
            style="width: 280px"
          >
            <ElOption
              v-for="p in plans"
              :key="p.id"
              :label="`${p.name}${p.destination ? ' - ' + p.destination : ''}`"
              :value="p.id"
            />
          </ElSelect>
        </div>

        <div v-if="loading" class="loading">加载推荐中...</div>

        <ElEmpty
          v-else-if="list.length === 0"
          description="暂无推荐结果，可尝试调整计划参数"
        />

        <ElRow v-else :gutter="16" class="list-row">
          <ElCol
            v-for="item in list"
            :key="item.gearId"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <ElCard class="gear-card" shadow="hover">
              <div class="gear-cover" @click="goDetail(item.gearId)">
                <img
                  v-if="item.coverImage"
                  :src="item.coverImage"
                  :alt="item.gearName"
                  onerror="this.style.display='none'"
                />
                <div v-else class="cover-placeholder">暂无图片</div>
              </div>
              <div class="gear-info">
                <h3 class="gear-name" @click="goDetail(item.gearId)">
                  {{ item.gearName }}
                </h3>
                <p class="gear-meta">
                  {{ item.categoryName }} · {{ item.brand }}
                </p>
                <p class="gear-price">¥ {{ item.price }}</p>
                <p v-if="item.algorithmHint" class="hint">
                  {{ item.algorithmHint }}
                </p>
                <div class="actions">
                  <ElButton size="small" @click="addFavorite(item.gearId)">
                    收藏
                  </ElButton>
                  <ElInputNumber
                    :model-value="getCartQty(item.gearId)"
                    :min="1"
                    :max="99"
                    size="small"
                    controls-position="right"
                    style="width: 100px"
                    @update:model-value="(v: number) => setCartQty(item.gearId, v)"
                  />
                  <ElButton
                    type="primary"
                    size="small"
                    @click="addToCart(item)"
                  >
                    加购
                  </ElButton>
                </div>
              </div>
            </ElCard>
          </ElCol>
        </ElRow>
      </template>
    </ElCard>
  </div>
</template>

<style scoped>
.recommend-page {
  max-width: 1200px;
  margin: 0 auto;
}
.plan-select {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.loading {
  padding: 40px;
  text-align: center;
  color: #909399;
}
.list-row {
  min-height: 200px;
}
.gear-card {
  margin-bottom: 16px;
}
.gear-cover {
  height: 160px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gear-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  color: #909399;
  font-size: 14px;
}
.gear-info {
  padding-top: 12px;
}
.gear-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.gear-name:hover {
  color: #409eff;
}
.gear-meta {
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
}
.gear-price {
  margin: 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #e6a23c;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin: 4px 0 8px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
</style>
