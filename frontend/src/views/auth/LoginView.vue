<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElFormItem, ElInput, ElButton, ElCard, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const formRef = ref()

const onSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authStore.login(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <ElCard class="login-card" shadow="hover">
      <template #header>
        <span>登录</span>
      </template>
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="0">
        <ElFormItem prop="username">
          <ElInput v-model="form.username" placeholder="用户名" size="large" />
        </ElFormItem>
        <ElFormItem prop="password">
          <ElInput v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="onSubmit" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" size="large" :loading="loading" style="width: 100%" @click="onSubmit">
            登录
          </ElButton>
        </ElFormItem>
        <ElFormItem>
          <ElButton text type="primary" @click="router.push('/register')">没有账号？去注册</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.login-card {
  width: 400px;
}
</style>
