<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElCard,
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput,
  ElSelect,
  ElOption,
  ElPagination,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus'
import { adminApi } from '@/api/admin'
import { gearApi, type GearCategory, type GearTag, type GearListItem } from '@/api/gear'

const router = useRouter()
const list = ref<GearListItem[]>([])
const total = ref(0)
const loading = ref(false)
const categories = ref<GearCategory[]>([])
const tags = ref<GearTag[]>([])

const filters = ref({
  categoryId: undefined as number | undefined,
  name: '',
  brand: '',
  status: undefined as number | undefined,
})
const page = ref(0)
const size = 20

const formVisible = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const form = ref({
  id: undefined as number | undefined,
  name: '',
  categoryId: undefined as number | undefined,
  brand: '',
  price: 0,
  weight: undefined as number | undefined,
  season: '',
  scene: '',
  comfortTemperature: '',
  maxUsers: undefined as number | undefined,
  stock: 50,
  description: '',
  status: 1,
  tagIds: [] as number[],
})
const saving = ref(false)

async function loadCategories() {
  try {
    const { data } = await gearApi.getCategories()
    categories.value = data
  } catch {
    ElMessage.error('加载分类失败')
  }
}

async function loadTags() {
  try {
    const { data } = await gearApi.getTags()
    tags.value = data
  } catch {
    ElMessage.error('加载标签失败')
  }
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await adminApi.listGears({
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

function openCreate() {
  formMode.value = 'create'
  form.value = {
    name: '',
    categoryId: undefined,
    brand: '',
    price: 0,
    weight: undefined,
    season: '',
    scene: '',
    comfortTemperature: '',
    maxUsers: undefined,
    stock: 50,
    description: '',
    status: 1,
    tagIds: [],
  }
  formVisible.value = true
}

async function openEdit(row: GearListItem) {
  formMode.value = 'edit'
  try {
    const { data } = await adminApi.getGear(row.id)
    form.value = {
      id: data.id,
      name: data.name,
      categoryId: data.categoryId,
      brand: data.brand ?? '',
      price: data.price,
      weight: data.weight ?? undefined,
      season: data.season ?? '',
      scene: data.scene ?? '',
      comfortTemperature: data.comfortTemperature ?? '',
      maxUsers: data.maxUsers ?? undefined,
      stock: data.stock,
      description: data.description ?? '',
      status: data.status,
      tagIds: tags.value
        .filter((t) => data.tagNames?.includes(t.name))
        .map((t) => t.id),
    }
    formVisible.value = true
  } catch {
    ElMessage.error('加载装备详情失败')
  }
}

async function saveForm() {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请输入装备名称')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }
  saving.value = true
  try {
    if (formMode.value === 'create') {
      await adminApi.createGear({
        name: form.value.name,
        categoryId: form.value.categoryId!,
        brand: form.value.brand || undefined,
        price: form.value.price,
        weight: form.value.weight,
        season: form.value.season || undefined,
        scene: form.value.scene || undefined,
        comfortTemperature: form.value.comfortTemperature || undefined,
        maxUsers: form.value.maxUsers,
        stock: form.value.stock,
        description: form.value.description || undefined,
        status: form.value.status,
        tagIds: form.value.tagIds,
      })
      ElMessage.success('创建成功')
    } else if (form.value.id) {
      await adminApi.updateGear(form.value.id, {
        name: form.value.name,
        categoryId: form.value.categoryId,
        brand: form.value.brand,
        price: form.value.price,
        weight: form.value.weight,
        season: form.value.season,
        scene: form.value.scene,
        comfortTemperature: form.value.comfortTemperature,
        maxUsers: form.value.maxUsers,
        stock: form.value.stock,
        description: form.value.description,
        status: form.value.status,
        tagIds: form.value.tagIds,
      })
      ElMessage.success('保存成功')
    }
    formVisible.value = false
    loadList()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: GearListItem) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该装备吗？`, '提示')
    await adminApi.updateGearStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

async function handleDelete(row: GearListItem) {
  try {
    await ElMessageBox.confirm('确定要删除该装备吗？删除后不可恢复。', '提示', {
      type: 'warning',
    })
    await adminApi.deleteGear(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function statusLabel(s: number) {
  return s === 1 ? '上架' : '下架'
}

onMounted(() => {
  loadCategories()
  loadTags()
  loadList()
})
</script>

<template>
  <div class="admin-gear">
    <ElCard>
      <template #header>
        <span>装备管理</span>
        <ElButton type="primary" style="float: right" @click="openCreate">
          新增装备
        </ElButton>
      </template>
      <div class="toolbar">
        <ElSelect
          v-model="filters.categoryId"
          placeholder="分类"
          clearable
          style="width: 140px"
        >
          <ElOption
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </ElSelect>
        <ElInput
          v-model="filters.name"
          placeholder="装备名称"
          clearable
          style="width: 160px"
        />
        <ElInput
          v-model="filters.brand"
          placeholder="品牌"
          clearable
          style="width: 120px"
        />
        <ElSelect
          v-model="filters.status"
          placeholder="状态"
          clearable
          style="width: 100px"
        >
          <ElOption label="上架" :value="1" />
          <ElOption label="下架" :value="0" />
        </ElSelect>
        <ElButton type="primary" @click="handleSearch">搜索</ElButton>
      </div>
      <ElTable :data="list" v-loading="loading" stripe>
        <ElTableColumn prop="id" label="ID" width="70" />
        <ElTableColumn prop="name" label="名称" min-width="180" show-overflow-tooltip />
        <ElTableColumn prop="categoryName" label="分类" width="90" />
        <ElTableColumn prop="brand" label="品牌" width="100" />
        <ElTableColumn prop="price" label="价格" width="90">
          <template #default="{ row }">¥{{ row.price }}</template>
        </ElTableColumn>
        <ElTableColumn prop="stock" label="库存" width="70" />
        <ElTableColumn prop="status" label="状态" width="80">
          <template #default="{ row }">
            <ElTag :type="row.status === 1 ? 'success' : 'info'">
              {{ statusLabel(row.status) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <ElButton type="primary" link size="small" @click="router.push(`/gears/${row.id}`)">
              查看
            </ElButton>
            <ElButton type="primary" link size="small" @click="openEdit(row)">
              编辑
            </ElButton>
            <ElButton type="primary" link size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </ElButton>
            <ElButton type="danger" link size="small" @click="handleDelete(row)">
              删除
            </ElButton>
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

    <ElDialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增装备' : '编辑装备'"
      width="560px"
      @close="formVisible = false"
    >
      <ElForm label-width="100px">
        <ElFormItem label="名称" required>
          <ElInput v-model="form.name" placeholder="装备名称" />
        </ElFormItem>
        <ElFormItem label="分类" required>
          <ElSelect
            v-model="form.categoryId"
            placeholder="选择分类"
            style="width: 100%"
          >
            <ElOption
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="品牌">
          <ElInput v-model="form.brand" placeholder="品牌" />
        </ElFormItem>
        <ElFormItem label="价格" required>
          <ElInputNumber v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="重量(g)">
          <ElInputNumber v-model="form.weight" :min="0" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="季节">
          <ElInput v-model="form.season" placeholder="如：春,夏,秋" />
        </ElFormItem>
        <ElFormItem label="场景">
          <ElInput v-model="form.scene" placeholder="如：徒步,露营" />
        </ElFormItem>
        <ElFormItem label="舒适温标">
          <ElInput v-model="form.comfortTemperature" placeholder="如：-5℃" />
        </ElFormItem>
        <ElFormItem label="适用人数">
          <ElInputNumber v-model="form.maxUsers" :min="0" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="库存" required>
          <ElInputNumber v-model="form.stock" :min="0" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="标签">
          <ElSelect
            v-model="form.tagIds"
            multiple
            placeholder="选择标签"
            style="width: 100%"
          >
            <ElOption
              v-for="t in tags"
              :key="t.id"
              :label="`${t.name} (${t.type || '-'})`"
              :value="t.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="描述">
          <ElInput v-model="form.description" type="textarea" :rows="3" placeholder="商品描述" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="form.status" style="width: 100%">
            <ElOption label="上架" :value="1" />
            <ElOption label="下架" :value="0" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="formVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saving" @click="saveForm">
          保存
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
  flex-wrap: wrap;
}
</style>
