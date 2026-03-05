<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElButton,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElEmpty,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElDatePicker,
  ElSelect,
  ElOption,
  ElMessage,
  ElMessageBox,
} from 'element-plus'
import { planApi, type TripPlan, type TripPlanCreateRequest } from '@/api/plan'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const list = ref<TripPlan[]>([])
const total = ref(0)
const loading = ref(false)
const page = ref(0)
const size = 20

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref<TripPlanCreateRequest>({
  name: '',
  destination: '',
  startDate: undefined,
  endDate: undefined,
  days: undefined,
  peopleCount: undefined,
  budget: undefined,
  season: '',
  activityType: '',
  difficultyLevel: undefined,
  note: '',
  requirementText: '',
})

async function loadList() {
  if (!authStore.isLoggedIn) return
  loading.value = true
  try {
    const { data } = await planApi.list(page.value, size)
    list.value = data.content
    total.value = data.totalElements
  } catch {
    ElMessage.error('加载计划列表失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = {
    name: '',
    destination: '',
    startDate: undefined,
    endDate: undefined,
    days: undefined,
    peopleCount: undefined,
    budget: undefined,
    season: '',
    activityType: '',
    difficultyLevel: undefined,
    note: '',
    requirementText: '',
  }
  dialogVisible.value = true
}

function openEdit(plan: TripPlan) {
  editingId.value = plan.id
  form.value = {
    name: plan.name,
    destination: plan.destination ?? '',
    startDate: plan.startDate ?? undefined,
    endDate: plan.endDate ?? undefined,
    days: plan.days ?? undefined,
    peopleCount: plan.peopleCount ?? undefined,
    budget: plan.budget ?? undefined,
    season: plan.season ?? '',
    activityType: plan.activityType ?? '',
    difficultyLevel: plan.difficultyLevel ?? undefined,
    note: plan.note ?? '',
    requirementText: plan.requirementText ?? '',
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请输入计划名称')
    return
  }
  try {
    if (editingId.value) {
      await planApi.update(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await planApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

async function handleDelete(plan: TripPlan) {
  try {
    await ElMessageBox.confirm(
      `确定删除计划「${plan.name}」吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await planApi.delete(plan.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function goRecommend(plan: TripPlan) {
  router.push({ path: '/recommend', query: { planId: String(plan.id) } })
}

function handlePageChange(p: number) {
  page.value = p - 1
  loadList()
}

function formatDate(d: string | null) {
  return d ? d.slice(0, 10) : '-'
}

onMounted(() => {
  if (authStore.isLoggedIn) loadList()
})
</script>

<template>
  <div class="plan-list-page">
    <ElCard>
      <template #header>
        <span>出行计划</span>
        <ElButton
          v-if="authStore.isLoggedIn"
          type="primary"
          style="float: right"
          @click="openCreate"
        >
          新建计划
        </ElButton>
      </template>

      <template v-if="!authStore.isLoggedIn">
        <ElEmpty description="请先登录后创建出行计划">
          <ElButton type="primary" @click="router.push('/login')">
            去登录
          </ElButton>
        </ElEmpty>
      </template>

      <template v-else-if="loading && list.length === 0">
        加载中...
      </template>

      <template v-else-if="list.length === 0">
        <ElEmpty description="暂无出行计划">
          <ElButton type="primary" @click="openCreate">创建第一个计划</ElButton>
        </ElEmpty>
      </template>

      <template v-else>
        <ElTable :data="list" stripe>
          <ElTableColumn prop="name" label="计划名称" min-width="120" />
          <ElTableColumn prop="destination" label="目的地" min-width="100" />
          <ElTableColumn label="时间" min-width="120">
            <template #default="{ row }">
              {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="peopleCount" label="人数" width="80" />
          <ElTableColumn label="预算" width="100">
            <template #default="{ row }">
              {{ row.budget != null ? `¥${row.budget}` : '-' }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="season" label="季节" width="80" />
          <ElTableColumn prop="activityType" label="活动类型" width="100" />
          <ElTableColumn label="操作" width="200">
            <template #default="{ row }">
              <ElButton link type="primary" @click="goRecommend(row)">
                获取推荐
              </ElButton>
              <ElButton link type="primary" @click="openEdit(row)">
                编辑
              </ElButton>
              <ElButton link type="danger" @click="handleDelete(row)">
                删除
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div v-if="total > size" class="pagination-wrap">
          <ElPagination
            :current-page="page + 1"
            :page-size="size"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </ElCard>

    <ElDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑计划' : '新建计划'"
      width="500px"
      destroy-on-close
    >
      <ElForm :model="form" label-width="80px">
        <ElFormItem label="计划名称" required>
          <ElInput v-model="form.name" placeholder="如：五一徒步" />
        </ElFormItem>
        <ElFormItem label="目的地">
          <ElInput v-model="form.destination" placeholder="如：稻城亚丁" />
        </ElFormItem>
        <ElFormItem label="开始日期">
          <ElDatePicker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem label="结束日期">
          <ElDatePicker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </ElFormItem>
        <ElFormItem label="人数">
          <ElInputNumber v-model="form.peopleCount" :min="1" :max="20" />
        </ElFormItem>
        <ElFormItem label="预算(元)">
          <ElInputNumber v-model="form.budget" :min="0" />
        </ElFormItem>
        <ElFormItem label="季节">
          <ElSelect v-model="form.season" placeholder="选择季节" clearable style="width: 100%">
            <ElOption label="春" value="春" />
            <ElOption label="夏" value="夏" />
            <ElOption label="秋" value="秋" />
            <ElOption label="冬" value="冬" />
            <ElOption label="四季" value="四季" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="活动类型">
          <ElInput v-model="form.activityType" placeholder="如：徒步、露营" />
        </ElFormItem>
        <ElFormItem label="需求描述">
          <ElInput
            v-model="form.requirementText"
            type="textarea"
            :rows="3"
            placeholder="如：五一去稻城亚丁徒步，需要帐篷、睡袋、登山杖，预算5000"
          />
          <div v-if="form.requirementText?.trim()" class="form-tip">
            系统将自动提取关键词用于推荐
          </div>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="form.note" type="textarea" :rows="3" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitForm">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.plan-list-page {
  max-width: 1200px;
  margin: 0 auto;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style>
