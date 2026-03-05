<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElCard,
  ElRow,
  ElCol,
  ElButton,
  ElRate,
  ElInput,
  ElMessage,
  ElMessageBox,
} from 'element-plus'
import { gearApi, type GearDetail } from '@/api/gear'
import { userApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { useCompareStore } from '@/stores/compare'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const compareStore = useCompareStore()

const detail = ref<GearDetail | null>(null)
const loading = ref(true)
const ratingScore = ref(5)
const ratingComment = ref('')
const ratingSubmitting = ref(false)
const cartQuantity = ref(1)

const gearId = computed(() => Number(route.params.id))

async function loadDetail() {
  if (!gearId.value || isNaN(gearId.value)) return
  loading.value = true
  try {
    const { data } = await gearApi.getDetail(gearId.value)
    detail.value = data
  } catch {
    ElMessage.error('加载装备详情失败')
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!detail.value) return
  try {
    if (detail.value.isFavorite) {
      await userApi.removeFavorite(detail.value.id)
      if (detail.value) detail.value = { ...detail.value, isFavorite: false }
      ElMessage.success('已取消收藏')
    } else {
      await userApi.addFavorite(detail.value.id)
      if (detail.value) detail.value = { ...detail.value, isFavorite: true }
      ElMessage.success('已收藏')
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

async function addToCart() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!detail.value) return
  try {
    await userApi.addToCart(detail.value.id, cartQuantity.value)
    if (detail.value)
      detail.value = {
        ...detail.value,
        cartQuantity: detail.value.cartQuantity + cartQuantity.value,
      }
    ElMessage.success('已加入购物车')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '加购失败')
  }
}

async function submitRating() {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!detail.value) return
  ratingSubmitting.value = true
  try {
    await gearApi.rate(detail.value.id, {
      score: ratingScore.value,
      comment: ratingComment.value || undefined,
    })
    ElMessage.success('评分成功')
    ratingComment.value = ''
    loadDetail()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '评分失败')
  } finally {
    ratingSubmitting.value = false
  }
}

function goCompare() {
  if (!detail.value) return
  compareStore.add(detail.value.id)
  router.push('/gears/compare')
  ElMessage.success('已加入对比')
}

const imageList = computed(() => {
  const d = detail.value
  if (!d?.imageList) return d?.coverImage ? [d.coverImage] : []
  return d.imageList.split(',').filter(Boolean)
})

onMounted(() => loadDetail())
</script>

<template>
  <div class="gear-detail-page">
    <!-- 面包屑 -->
    <div v-if="detail" class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link to="/gears">装备</router-link>
      <span class="sep">/</span>
      <router-link :to="`/gears?categoryId=${detail.categoryId}`">{{ detail.categoryName }}</router-link>
      <span class="sep">/</span>
      <span class="current">{{ detail.name }}</span>
    </div>
    <ElButton
      v-else-if="!loading"
      link
      type="primary"
      class="back-btn"
      @click="router.back()"
    >
      ← 返回
    </ElButton>

    <template v-if="loading">加载中...</template>

    <template v-else-if="detail">
      <ElRow :gutter="24">
        <ElCol :xs="24" :md="10">
          <div class="cover-area">
            <img
              v-if="detail.coverImage"
              :src="detail.coverImage"
              :alt="detail.name"
              class="main-cover"
            />
            <div v-else class="cover-placeholder">暂无图片</div>
          </div>
        </ElCol>
        <ElCol :xs="24" :md="14">
          <div class="info-area">
            <h1 class="gear-name">{{ detail.name }}</h1>
            <p class="gear-meta">
              {{ detail.categoryName }} · {{ detail.brand || '未知品牌' }}
            </p>
            <p class="gear-price">¥ {{ detail.price }}</p>
            <!-- 标签分组展示（8264 风格） -->
            <div v-if="detail.tagGroups && Object.keys(detail.tagGroups).length" class="tag-groups">
              <div
                v-for="(tags, groupName) in detail.tagGroups"
                :key="groupName"
                class="tag-group"
              >
                <span class="group-label">{{ groupName }}：</span>
                <span v-for="t in tags" :key="t" class="tag">{{ t }}</span>
              </div>
            </div>
            <div v-else-if="detail.tagNames?.length" class="tags">
              <span v-for="t in detail.tagNames" :key="t" class="tag">{{ t }}</span>
            </div>
            <div class="actions">
              <ElButton
                :type="detail.isFavorite ? 'warning' : 'default'"
                @click="toggleFavorite"
              >
                {{ detail.isFavorite ? '已收藏' : '收藏' }}
              </ElButton>
              <ElInputNumber
                v-model="cartQuantity"
                :min="1"
                :max="detail.stock"
                controls-position="right"
                style="width: 120px"
              />
              <ElButton type="primary" @click="addToCart">加入购物车</ElButton>
              <ElButton @click="goCompare">加入对比</ElButton>
            </div>
          </div>
        </ElCol>
      </ElRow>

      <!-- 产品介绍 -->
      <ElRow :gutter="24" class="detail-section">
        <ElCol :span="24">
          <ElCard header="产品介绍" shadow="never">
            <p v-if="detail.description" class="description">{{ detail.description }}</p>
            <p v-else class="description intro-fallback">
              {{ detail.name }} 是一款{{ detail.categoryName }}类户外装备，品牌为 {{ detail.brand || '知名品牌' }}。
              适用{{ detail.season || '四季' }}使用，适合{{ detail.scene || '徒步、露营' }}等场景。
              {{ detail.weight ? `净重约 ${detail.weight} 克，` : '' }}当前库存 {{ detail.stock }} 件。
            </p>
          </ElCard>
        </ElCol>
      </ElRow>

      <ElRow :gutter="24" class="detail-section">
        <ElCol :span="24">
          <ElCard header="规格参数" shadow="never">
            <div class="param-grid">
              <div class="param-item">
                <span class="label">重量</span>
                <span>{{ detail.weight ?? '-' }} g</span>
              </div>
              <div class="param-item">
                <span class="label">季节</span>
                <span>{{ detail.season ?? '-' }}</span>
              </div>
              <div class="param-item">
                <span class="label">场景</span>
                <span>{{ detail.scene ?? '-' }}</span>
              </div>
              <div class="param-item">
                <span class="label">舒适温标</span>
                <span>{{ detail.comfortTemperature ?? '-' }}</span>
              </div>
              <div class="param-item">
                <span class="label">适用人数</span>
                <span>{{ detail.maxUsers ?? '-' }}</span>
              </div>
              <div class="param-item">
                <span class="label">库存</span>
                <span>{{ detail.stock }}</span>
              </div>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>

      <ElRow :gutter="24" class="detail-section">
        <ElCol :span="24">
          <ElCard header="用户评价" shadow="never">
            <div class="rating-summary">
              <span class="avg-score">{{ detail.avgScore?.toFixed(1) ?? '-' }}</span>
              <ElRate
                :model-value="detail.avgScore ?? 0"
                disabled
                show-score
                text-color="#ff9900"
              />
              <span class="rating-count">{{ detail.ratingCount }} 条评价</span>
            </div>

            <div v-if="authStore.isLoggedIn" class="rating-form">
              <ElRate v-model="ratingScore" />
              <ElInput
                v-model="ratingComment"
                type="textarea"
                placeholder="写下你的评价（选填）"
                :rows="3"
                style="margin-top: 8px"
              />
              <ElButton
                type="primary"
                style="margin-top: 8px"
                :loading="ratingSubmitting"
                @click="submitRating"
              >
                提交评分
              </ElButton>
            </div>

            <div class="rating-list">
              <div
                v-for="r in detail.ratings"
                :key="r.id"
                class="rating-item"
              >
                <div class="rating-header">
                  <span class="user">{{ r.userNickname }}</span>
                  <ElRate :model-value="r.score" disabled size="small" />
                  <span class="date">{{ r.createdAt }}</span>
                </div>
                <p v-if="r.comment" class="comment">{{ r.comment }}</p>
              </div>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>

      <!-- 同分类推荐 -->
      <ElRow v-if="detail.relatedGears?.length" :gutter="24" class="detail-section">
        <ElCol :span="24">
          <ElCard header="相关推荐" shadow="never">
            <div class="related-grid">
              <div
                v-for="g in detail.relatedGears"
                :key="g.id"
                class="related-item"
                @click="router.push(`/gears/${g.id}`)"
              >
                <div class="related-cover">
                  <img v-if="g.coverImage" :src="g.coverImage" :alt="g.name" />
                  <div v-else class="cover-placeholder">暂无图片</div>
                </div>
                <div class="related-info">
                  <h4>{{ g.name }}</h4>
                  <p>¥ {{ g.price }}</p>
                </div>
              </div>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>
    </template>

    <template v-else>
      <ElCard>装备不存在或已下架</ElCard>
    </template>
  </div>
</template>

<style scoped>
.gear-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}
.breadcrumb {
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}
.breadcrumb a {
  color: #409eff;
  text-decoration: none;
}
.breadcrumb a:hover {
  text-decoration: underline;
}
.breadcrumb .sep {
  margin: 0 8px;
  color: #c0c4cc;
}
.breadcrumb .current {
  color: #303133;
}
.back-btn {
  margin-bottom: 16px;
}
.tag-groups {
  margin-bottom: 20px;
}
.tag-group {
  margin-bottom: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}
.group-label {
  font-size: 13px;
  color: #909399;
  min-width: 72px;
}
.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}
.related-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}
.related-item:hover {
  transform: translateY(-2px);
}
.related-cover {
  height: 120px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.related-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.related-info {
  padding: 8px 0;
}
.related-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.related-info p {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
}
.intro-fallback {
  color: #606266;
}
.cover-area {
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 1;
}
.main-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.info-area {
  padding: 0 0 0 16px;
}
.gear-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}
.gear-meta {
  margin: 0 0 12px;
  color: #909399;
}
.gear-price {
  font-size: 28px;
  font-weight: 600;
  color: #e6a23c;
  margin: 0 0 16px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.tag {
  padding: 4px 10px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}
.detail-section {
  margin-top: 24px;
}
.param-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px 24px;
}
.param-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.param-item .label {
  font-size: 12px;
  color: #909399;
}
.description {
  margin-top: 16px;
  line-height: 1.6;
  color: #606266;
}
.rating-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}
.avg-score {
  font-size: 32px;
  font-weight: 600;
}
.rating-count {
  color: #909399;
  font-size: 14px;
}
.rating-form {
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
}
.rating-list {
  margin-top: 20px;
}
.rating-item {
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}
.rating-item:last-child {
  border-bottom: none;
}
.rating-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.rating-header .user {
  font-weight: 500;
}
.rating-header .date {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}
.comment {
  margin: 0;
  font-size: 14px;
  color: #606266;
  padding-left: 0;
}
@media (max-width: 768px) {
  .info-area {
    padding: 16px 0 0;
  }
}
</style>
