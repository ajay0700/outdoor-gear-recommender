<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElStatistic } from 'element-plus'
import { adminApi } from '@/api/admin'
import { gearApi } from '@/api/gear'

const userCount = ref(0)
const gearCount = ref(0)
const pendingApplyCount = ref(0)

async function loadStats() {
  try {
    const [users, gears, applies] = await Promise.all([
      adminApi.listUsers({ page: 0, size: 1 }).catch(() => ({ data: { totalElements: 0 } })),
      adminApi.listGears({ page: 0, size: 1 }).catch(() => ({ data: { totalElements: 0 } })),
      adminApi.listRoleApplies({ page: 0, size: 1, status: 0 }).catch(() => ({ data: { totalElements: 0 } })),
    ])
    userCount.value = users?.data?.totalElements ?? 0
    gearCount.value = gears?.data?.totalElements ?? 0
    pendingApplyCount.value = applies?.data?.totalElements ?? 0
  } catch {
    // 无权限时忽略
  }
}

onMounted(loadStats)
</script>

<template>
  <div class="admin-dashboard">
    <h2 style="margin-bottom: 24px">管理概览</h2>
    <ElRow :gutter="24">
      <ElCol :span="8">
        <ElCard shadow="hover">
          <ElStatistic title="用户总数" :value="userCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard shadow="hover">
          <ElStatistic title="装备总数" :value="gearCount" />
        </ElCard>
      </ElCol>
      <ElCol :span="8">
        <ElCard shadow="hover">
          <ElStatistic title="待审核申请" :value="pendingApplyCount" />
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
}
</style>
