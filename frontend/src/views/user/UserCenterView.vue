<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElInputNumber, ElButton, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { userApi, type UserPreference } from '@/api/user'

const authStore = useAuthStore()
const pref = ref<UserPreference>({})
const saving = ref(false)

async function loadPref() {
  try {
    const { data } = await userApi.getPreferences()
    pref.value = data
  } catch {
    pref.value = {}
  }
}

async function savePref() {
  saving.value = true
  try {
    await userApi.savePreferences(pref.value)
    ElMessage.success('偏好已保存')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadPref)
</script>

<template>
  <ElCard>
    <template #header>个人中心</template>
    <p>用户名：{{ authStore.user?.username }}</p>

    <ElCard header="推荐偏好" shadow="never" style="margin-top: 16px">
      <p class="hint">填写后可在无出行计划时获得个性化推荐（缓解冷启动），画像越细致推荐越精准</p>
      <ElForm label-width="100px" style="max-width: 500px">
        <ElFormItem label="偏好季节">
          <ElSelect v-model="pref.season" placeholder="选填" clearable style="width: 100%">
            <ElOption label="春" value="春" />
            <ElOption label="夏" value="夏" />
            <ElOption label="秋" value="秋" />
            <ElOption label="冬" value="冬" />
            <ElOption label="四季" value="四季" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="活动类型">
          <ElInput v-model="pref.activityType" placeholder="如：徒步、露营、登山" clearable />
        </ElFormItem>
        <ElFormItem label="偏好目的地">
          <ElInput v-model="pref.preferredDestinations" placeholder="如：稻城亚丁,西藏,新疆" clearable />
        </ElFormItem>
        <ElFormItem label="偏好装备分类">
          <ElInput v-model="pref.preferredCategories" placeholder="如：帐篷,睡袋,登山杖" clearable />
        </ElFormItem>
        <ElFormItem label="偏好标签">
          <ElInput v-model="pref.preferredTags" placeholder="如：轻量化,防水,保暖" clearable />
        </ElFormItem>
        <ElFormItem label="难度偏好">
          <ElSelect v-model="pref.difficultyPreference" placeholder="选填" clearable style="width: 100%">
            <ElOption label="入门" value="入门" />
            <ElOption label="进阶" value="进阶" />
            <ElOption label="专业" value="专业" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="预算下限">
          <ElInputNumber v-model="pref.budgetMin" :min="0" controls-position="right" style="width: 100%" />
        </ElFormItem>
        <ElFormItem label="预算上限">
          <ElInputNumber v-model="pref.budgetMax" :min="0" controls-position="right" style="width: 100%" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="saving" @click="savePref">保存偏好</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </ElCard>
</template>

<style scoped>
.hint {
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}
</style>
