<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ElContainer,
  ElHeader,
  ElAside,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElButton,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElScrollbar,
} from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const isCollapse = ref(false)

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const hasRole = (code: string) =>
  authStore.user?.roles?.some((r) => r === code || r === `ROLE_${code}`) ?? false

const adminMenus = computed(() => {
  const menus: { path: string; name: string }[] = []
  if (hasRole('ADMIN') || hasRole('GEAR_ADMIN') || hasRole('EXPERT')) {
    menus.push({ path: '/admin/gears', name: '装备管理' })
  }
  if (hasRole('ADMIN')) {
    menus.push({ path: '/admin/users', name: '用户管理' })
    menus.push({ path: '/admin/role-apply', name: '角色申请审核' })
  }
  return menus
})

const defaultActive = computed(() => route.path)
</script>

<template>
  <ElContainer class="admin-layout">
    <ElHeader class="admin-header">
      <div class="admin-logo">
        <span class="logo-text">管理后台</span>
        <ElButton text type="primary" size="small" @click="router.push('/')">返回前台</ElButton>
      </div>
      <div class="admin-user">
        <ElDropdown trigger="click" @command="(cmd: string) => { if (cmd === 'logout') handleLogout(); else if (cmd === 'profile') router.push('/user') }">
          <span class="user-dropdown">
            {{ authStore.user?.nickname || authStore.user?.username }} ▼
          </span>
          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem command="profile">个人中心</ElDropdownItem>
              <ElDropdownItem command="logout" divided>退出登录</ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>
      </div>
    </ElHeader>
    <ElContainer class="admin-body">
      <ElAside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
        <ElScrollbar>
          <ElMenu
            :default-active="defaultActive"
            :collapse="isCollapse"
            router
            class="admin-menu"
          >
            <ElMenuItem index="/admin">
              <span>概览</span>
            </ElMenuItem>
            <ElMenuItem
              v-for="m in adminMenus"
              :key="m.path"
              :index="m.path"
            >
              <span>{{ m.name }}</span>
            </ElMenuItem>
          </ElMenu>
        </ElScrollbar>
      </ElAside>
      <ElMain class="admin-main">
        <RouterView />
      </ElMain>
    </ElContainer>
  </ElContainer>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  flex-direction: column;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #001529;
  color: #fff;
  padding: 0 24px;
  height: 56px;
}
.admin-logo {
  display: flex;
  align-items: center;
  gap: 16px;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
}
.admin-user .user-dropdown {
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
}
.admin-body {
  flex: 1;
  overflow: hidden;
}
.admin-aside {
  background: #001529;
  overflow: hidden;
}
.admin-menu {
  border: none;
  background: transparent;
}
.admin-menu :deep(.el-menu-item),
.admin-menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.65);
}
.admin-menu :deep(.el-menu-item:hover),
.admin-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08) !important;
}
.admin-main {
  padding: 24px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
