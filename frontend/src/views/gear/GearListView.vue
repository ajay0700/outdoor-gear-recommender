<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElCard,
  ElRow,
  ElCol,
  ElSelect,
  ElOption,
  ElInput,
  ElInputNumber,
  ElButton,
  ElPagination,
  ElEmpty,
  ElMessage,
} from 'element-plus'
import { gearApi, type GearListItem, type GearCategory } from '@/api/gear'

const route = useRoute()
const router = useRouter()
const categories = ref<GearCategory[]>([])
const list = ref<GearListItem[]>([])
const total = ref(0)
const loading = ref(false)

const filters = ref({
  categoryId: undefined as number | undefined,
  name: '',
  brand: '',
  priceMin: undefined as number | undefined,
  priceMax: undefined as number | undefined,
  season: '',
  scene: '',
})
const page = ref(0)
const size = 20

async function loadCategories() {
  try {
    const { data } = await gearApi.getCategories()
    categories.value = data
  } catch {
    ElMessage.error('加载分类失败')
  }
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await gearApi.list({
      ...filters.value,
      page: page.value,
      size,
    })
    list.value = data.content
    total.value = data.totalElements
  } catch {
    ElMessage.error('加载装备列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 0
  loadList()
}

function handleReset() {
  filters.value = {
    categoryId: undefined,
    name: '',
    brand: '',
    priceMin: undefined,
    priceMax: undefined,
    season: '',
    scene: '',
  }
  page.value = 0
  loadList()
}

function handlePageChange(p: number) {
  page.value = p - 1
  loadList()
}

function goDetail(id: number) {
  router.push(`/gears/${id}`)
}

onMounted(() => {
  loadCategories()
  const q = route.query.categoryId
  if (typeof q === 'string') {
    const id = parseInt(q, 10)
    if (!isNaN(id)) filters.value.categoryId = id
  }
  loadList()
})

watch(() => route.query.categoryId, (v) => {
  if (typeof v === 'string') {
    const id = parseInt(v, 10)
    filters.value.categoryId = !isNaN(id) ? id : undefined
    page.value = 0
    loadList()
  }
})
</script>

<template>
  <div class="gear-list-page">
    <!-- 8264 风格分类侧栏 -->
    <div class="category-sidebar">
      <div class="sidebar-title">装备分类</div>
      <router-link
        v-for="c in categories"
        :key="c.id"
        :to="{ path: '/gears', query: { categoryId: c.id } }"
        class="category-link"
        :class="{ active: filters.categoryId === c.id }"
      >
        {{ c.name }}
      </router-link>
    </div>

    <div class="main-content">
    <ElCard class="filter-card">
      <ElRow :gutter="16" align="middle">
        <ElCol :xs="24" :sm="12" :md="6">
          <ElSelect
            v-model="filters.categoryId"
            placeholder="分类"
            clearable
            style="width: 100%"
          >
            <ElOption
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </ElSelect>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="6">
          <ElInput
            v-model="filters.name"
            placeholder="装备名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="6">
          <ElInput
            v-model="filters.brand"
            placeholder="品牌"
            clearable
            @keyup.enter="handleSearch"
          />
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="6">
          <ElSelect v-model="filters.season" placeholder="季节" clearable style="width: 100%">
            <ElOption label="春" value="春" />
            <ElOption label="夏" value="夏" />
            <ElOption label="秋" value="秋" />
            <ElOption label="冬" value="冬" />
            <ElOption label="四季" value="四季" />
          </ElSelect>
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="4">
          <ElInputNumber
            v-model="filters.priceMin"
            placeholder="最低价"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="4">
          <ElInputNumber
            v-model="filters.priceMax"
            placeholder="最高价"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </ElCol>
        <ElCol :xs="24" :sm="12" :md="4">
          <ElButton type="primary" @click="handleSearch">搜索</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </ElCol>
      </ElRow>
    </ElCard>

    <ElRow :gutter="16" class="list-row">
      <ElCol
        v-for="item in list"
        :key="item.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <ElCard
          class="gear-card"
          shadow="hover"
          @click="goDetail(item.id)"
        >
          <div class="gear-cover">
            <img
              v-if="item.coverImage"
              :src="item.coverImage"
              :alt="item.name"
              onerror="this.style.display='none'"
            />
            <div v-else class="cover-placeholder">暂无图片</div>
          </div>
          <div class="gear-info">
            <h3 class="gear-name">{{ item.name }}</h3>
            <p class="gear-meta">
              {{ item.categoryName }} · {{ item.brand || '未知品牌' }}
            </p>
            <p class="gear-price">¥ {{ item.price }}</p>
            <div v-if="item.tagNames?.length" class="gear-tags">
              <span
                v-for="t in item.tagNames.slice(0, 3)"
                :key="t"
                class="tag"
              >
                {{ t }}
              </span>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElEmpty v-if="!loading && list.length === 0" description="暂无装备" />

    <div v-if="total > size" class="pagination-wrap">
      <ElPagination
        :current-page="page + 1"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
    </div>
  </div>
</template>

<style scoped>
.gear-list-page {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 24px;
}
.category-sidebar {
  flex-shrink: 0;
  width: 160px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: fit-content;
  position: sticky;
  top: 80px;
}
.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}
.category-link {
  display: block;
  padding: 8px 12px;
  font-size: 14px;
  color: #606266;
  text-decoration: none;
  border-radius: 4px;
  margin-bottom: 4px;
}
.category-link:hover {
  background: #ecf5ff;
  color: #409eff;
}
.category-link.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}
.main-content {
  flex: 1;
  min-width: 0;
}
.filter-card {
  margin-bottom: 20px;
}
.list-row {
  min-height: 200px;
}
.gear-card {
  cursor: pointer;
  margin-bottom: 16px;
  transition: transform 0.2s;
}
.gear-card:hover {
  transform: translateY(-2px);
}
.gear-cover {
  height: 160px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
.gear-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.tag {
  font-size: 11px;
  padding: 2px 6px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 2px;
}
.pagination-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
@media (max-width: 768px) {
  .gear-list-page {
    flex-direction: column;
  }
  .category-sidebar {
    width: 100%;
    position: static;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
  }
  .sidebar-title {
    width: 100%;
    margin-bottom: 8px;
  }
  .category-link {
    padding: 6px 12px;
    margin-bottom: 0;
  }
  .filter-card .el-col {
    margin-bottom: 8px;
  }
}
</style>
