<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElContainer, ElHeader, ElMain, ElAside, ElMenu, ElMenuItem, ElSubMenu, ElDropdown, ElDropdownItem, ElDropdownMenu } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const hasAdminRole = () =>
  authStore.user?.roles?.some((r: string) =>
    ['ROLE_ADMIN', 'ROLE_GEAR_ADMIN', 'ROLE_EXPERT', 'ADMIN', 'GEAR_ADMIN', 'EXPERT'].includes(r)
  ) ?? false

const navItems = [
  { path: '/', name: '首页' },
  { path: '/gears', name: '装备' },
  { path: '/gears/compare', name: '装备对比' },
  { path: '/plans', name: '出行计划' },
  { path: '/recommend', name: '推荐' },
  ...(hasAdminRole() ? [{ path: '/admin', name: '管理后台' }] : []),
]
</script>

<template>
  <ElContainer class="main-layout">
    <ElHeader class="header">
      <div class="logo">户外装备推荐</div>
      <ElMenu
        :default-active="$route.path"
        mode="horizontal"
        :collapse="isCollapse"
        router
        class="nav-menu"
      >
        <ElMenuItem v-for="item in navItems" :key="item.path" :index="item.path">
          {{ item.name }}
        </ElMenuItem>
      </ElMenu>
      <div class="user-area">
        <ElDropdown v-if="authStore.isLoggedIn" trigger="click" @command="(cmd: string) => { if (cmd === 'logout') handleLogout(); else if (cmd === 'profile') router.push('/user') }">
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
        <template v-else>
          <el-button type="primary" text @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </ElHeader>
    <ElMain class="main-content">
      <RouterView />
    </ElMain>
  </ElContainer>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
  flex-direction: column;
}
.header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 20px;
}
.logo {
  font-size: 18px;
  font-weight: 600;
  margin-right: 40px;
  color: #409eff;
}
.nav-menu {
  flex: 1;
  border: none;
}
.user-area {
  margin-left: auto;
}
.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.main-content {
  flex: 1;
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}
@media (max-width: 768px) {
  .header {
    flex-wrap: wrap;
    padding: 0 12px;
  }
  .logo {
    font-size: 16px;
    margin-right: 16px;
  }
  .main-content {
    padding: 12px;
  }
}
</style>
