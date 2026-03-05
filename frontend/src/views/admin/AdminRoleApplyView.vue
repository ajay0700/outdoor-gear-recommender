<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ElCard,
  ElTable,
  ElTableColumn,
  ElButton,
  ElSelect,
  ElOption,
  ElPagination,
  ElDialog,
  ElInput,
  ElFormItem,
  ElMessage,
} from 'element-plus'
import { adminApi, type RoleApply } from '@/api/admin'

const list = ref<RoleApply[]>([])
const total = ref(0)
const loading = ref(false)
const statusFilter = ref<number | undefined>(undefined)
const page = ref(0)
const size = 20

const reviewVisible = ref(false)
const reviewForm = ref({ id: 0, status: 1, adminNote: '' })
const saving = ref(false)

const statusMap: Record<number, string> = {
  0: '待审核',
  1: '通过',
  2: '拒绝',
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await adminApi.listRoleApplies({
      page: page.value,
      size,
      status: statusFilter.value,
    })
    list.value = data.content
    total.value = data.totalElements
  } catch {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

function openReview(row: RoleApply) {
  reviewForm.value = {
    id: row.id,
    status: 1,
    adminNote: '',
  }
  reviewVisible.value = true
}

async function submitReview() {
  saving.value = true
  try {
    await adminApi.reviewRoleApply(reviewForm.value.id, {
      status: reviewForm.value.status,
      adminNote: reviewForm.value.adminNote,
    })
    ElMessage.success('审核完成')
    reviewVisible.value = false
    loadList()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '审核失败')
  } finally {
    saving.value = false
  }
}

function statusLabel(s: number) {
  return statusMap[s] ?? '未知'
}

onMounted(loadList)
</script>

<template>
  <div class="admin-role-apply">
    <ElCard>
      <template #header>
        <span>角色申请审核</span>
      </template>
      <div class="toolbar">
        <ElSelect
          v-model="statusFilter"
          placeholder="申请状态"
          clearable
          style="width: 120px"
        >
          <ElOption label="待审核" :value="0" />
          <ElOption label="已通过" :value="1" />
          <ElOption label="已拒绝" :value="2" />
        </ElSelect>
        <ElButton type="primary" @click="loadList">刷新</ElButton>
      </div>
      <ElTable :data="list" v-loading="loading" stripe>
        <ElTableColumn prop="id" label="ID" width="70" />
        <ElTableColumn prop="username" label="申请人" width="120" />
        <ElTableColumn prop="nickname" label="昵称" width="120" />
        <ElTableColumn prop="roleCode" label="申请角色" width="120" />
        <ElTableColumn prop="reason" label="申请理由" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'danger'
              "
            >
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="createdAt" label="申请时间" width="170" />
        <ElTableColumn label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <ElButton
              v-if="row.status === 0"
              type="primary"
              link
              size="small"
              @click="openReview(row)"
            >
              审核
            </ElButton>
            <span v-else>-</span>
          </template>
        </ElTableColumn>
      </ElTable>
      <ElPagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px"
        @current-change="loadList"
      />
    </ElCard>

    <ElDialog v-model="reviewVisible" title="审核" width="420px" @close="reviewVisible = false">
      <ElFormItem label="审核结果">
        <ElSelect v-model="reviewForm.status" style="width: 100%">
          <ElOption label="通过" :value="1" />
          <ElOption label="拒绝" :value="2" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput
          v-model="reviewForm.adminNote"
          type="textarea"
          :rows="3"
          placeholder="审核备注（可选）"
        />
      </ElFormItem>
      <template #footer>
        <ElButton @click="reviewVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saving" @click="submitReview">
          提交
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
