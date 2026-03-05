<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ElCard,
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput,
  ElPagination,
  ElDialog,
  ElForm,
  ElFormItem,
  ElSelect,
  ElOption,
  ElSwitch,
  ElMessage,
} from 'element-plus'
import { adminApi, type UserProfile, type SysRole } from '@/api/admin'

const list = ref<UserProfile[]>([])
const total = ref(0)
const loading = ref(false)
const username = ref('')
const page = ref(0)
const size = 20
const roles = ref<SysRole[]>([])
const editVisible = ref(false)
const editForm = ref<Partial<UserProfile> & { roleIds?: number[] }>({})
const saving = ref(false)

async function loadRoles() {
  try {
    const { data } = await adminApi.getRoles()
    roles.value = data
  } catch {
    ElMessage.error('加载角色失败')
  }
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await adminApi.listUsers({
      page: page.value,
      size,
      username: username.value || undefined,
    })
    list.value = data.content
    total.value = data.totalElements
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 0
  loadList()
}

function openEdit(row: UserProfile) {
  editForm.value = {
    id: row.id,
    nickname: row.nickname ?? '',
    phone: row.phone ?? '',
    email: row.email ?? '',
    status: row.status,
    roleIds: roles.value.filter((r) => row.roles?.includes(r.code)).map((r) => r.id),
  }
  editVisible.value = true
}

async function saveEdit() {
  if (!editForm.value.id) return
  saving.value = true
  try {
    await adminApi.updateUser(editForm.value.id, {
      nickname: editForm.value.nickname,
      phone: editForm.value.phone,
      email: editForm.value.email,
      status: editForm.value.status,
      roleIds: editForm.value.roleIds,
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    loadList()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function statusLabel(s: number) {
  return s === 1 ? '正常' : '禁用'
}

onMounted(() => {
  loadRoles()
  loadList()
})
</script>

<template>
  <div class="admin-user">
    <ElCard>
      <template #header><span>用户管理</span></template>
      <div class="toolbar">
        <ElInput v-model="username" placeholder="搜索用户名" clearable style="width: 200px" @keyup.enter="handleSearch" />
        <ElButton type="primary" @click="handleSearch">搜索</ElButton>
      </div>
      <ElTable :data="list" v-loading="loading" stripe>
        <ElTableColumn prop="id" label="ID" width="80" />
        <ElTableColumn prop="username" label="用户名" width="120" />
        <ElTableColumn prop="nickname" label="昵称" width="120" />
        <ElTableColumn prop="phone" label="手机" width="120" />
        <ElTableColumn prop="email" label="邮箱" width="180" />
        <ElTableColumn prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="roles" label="角色">
          <template #default="{ row }">{{ row.roles?.join(', ') || '-' }}</template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="openEdit(row)">编辑</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <ElPagination v-model:current-page="page" :page-size="size" :total="total" layout="total, prev, pager, next" style="margin-top: 16px" @current-change="loadList" />
    </ElCard>
    <ElDialog v-model="editVisible" title="编辑用户" width="480px" @close="editVisible = false">
      <ElForm label-width="80px">
        <ElFormItem label="昵称"><ElInput v-model="editForm.nickname" placeholder="昵称" /></ElFormItem>
        <ElFormItem label="手机"><ElInput v-model="editForm.phone" placeholder="手机号" /></ElFormItem>
        <ElFormItem label="邮箱"><ElInput v-model="editForm.email" placeholder="邮箱" /></ElFormItem>
        <ElFormItem label="状态">
          <ElSwitch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="禁用" />
        </ElFormItem>
        <ElFormItem label="角色">
          <ElSelect v-model="editForm.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <ElOption v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="editVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saving" @click="saveEdit">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
