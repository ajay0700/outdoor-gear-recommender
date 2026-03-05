import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type AuthResponse } from '@/api/auth'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  function getInitialUser() {
    try {
      const raw = localStorage.getItem(USER_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }
  const user = ref<{ username: string; nickname?: string; roles: string[] } | null>(getInitialUser())

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(res: AuthResponse) {
    token.value = res.token
    user.value = { username: res.username, nickname: res.nickname, roles: res.roles || [] }
    localStorage.setItem(TOKEN_KEY, res.token)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(username: string, password: string) {
    const { data } = await authApi.login({ username, password })
    setAuth(data)
    return data
  }

  async function register(
    username: string,
    password: string,
    nickname?: string,
    preferences?: { season?: string; activityType?: string; budgetMin?: number; budgetMax?: number }
  ) {
    const { data } = await authApi.register({ username, password, nickname })
    setAuth(data)
    if (preferences && (preferences.season || preferences.activityType || preferences.budgetMin != null || preferences.budgetMax != null)) {
      try {
        await import('@/api/user').then(({ userApi }) => userApi.savePreferences(preferences))
      } catch {
        // 偏好保存失败不影响注册
      }
    }
    return data
  }

  function initFromStorage() {
    token.value = localStorage.getItem(TOKEN_KEY)
    const raw = localStorage.getItem(USER_KEY)
    user.value = raw ? JSON.parse(raw) : null
  }

  return { token, user, isLoggedIn, login, register, logout, setAuth, initFromStorage }
})
