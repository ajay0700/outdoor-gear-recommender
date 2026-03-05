<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElFormItem, ElInput, ElInputNumber, ElSelect, ElOption, ElButton, ElCard, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  season: '' as string,
  activityType: '',
  budgetMin: undefined as number | undefined,
  budgetMax: undefined as number | undefined,
})

const validateConfirm = (_rule: unknown, value: string, cb: (e?: Error) => void) => {
  if (value !== form.password) cb(new Error('两次密码不一致'))
  else cb()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 50, message: '长度 2-50', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
}

const formRef = ref()

const onSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const prefs =
      form.season || form.activityType || form.budgetMin != null || form.budgetMax != null
        ? {
            season: form.season || undefined,
            activityType: form.activityType || undefined,
            budgetMin: form.budgetMin,
            budgetMax: form.budgetMax,
          }
        : undefined
    await authStore.register(
      form.username.trim(),
      form.password,
      form.nickname.trim() || undefined,
      prefs
    )
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <ElCard class="register-card" shadow="hover">
      <template #header>
        <span>注册</span>
      </template>
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="0">
        <ElFormItem prop="username">
          <ElInput v-model="form.username" placeholder="用户名" size="large" />
        </ElFormItem>
        <ElFormItem prop="nickname">
          <ElInput v-model="form.nickname" placeholder="昵称（选填）" size="large" />
        </ElFormItem>
        <ElFormItem prop="password">
          <ElInput v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </ElFormItem>
        <ElFormItem prop="confirmPassword">
          <ElInput v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password @keyup.enter="onSubmit" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">
            注册
          </ElButton>
        </ElFormItem>
        <ElFormItem>
          <ElButton text type="primary" @click="router.push('/login')">已有账号？去登录</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.register-card {
  width: 420px;
}
.pref-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.pref-row .el-select,
.pref-row .el-input,
.pref-row .el-input-number {
  flex: 1;
}
</style>
