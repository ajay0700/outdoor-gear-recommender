import request from './request'
import type { PageResponse, GearListItem, GearDetail } from './gear'

export interface SysRole {
  id: number
  code: string
  name: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface UserProfile {
  id: number
  username: string
  nickname: string | null
  phone: string | null
  email: string | null
  avatar: string | null
  status: number
  points: number
  level: number
  lastLoginAt: string | null
  createdAt: string
  roles: string[]
}

export interface AdminUserUpdateRequest {
  status?: number
  nickname?: string
  phone?: string
  email?: string
  roleIds?: number[]
}

export interface RoleApply {
  id: number
  userId: number
  username: string
  nickname: string | null
  roleCode: string
  reason: string | null
  status: number
  adminNote: string | null
  reviewedBy: number | null
  reviewedAt: string | null
  createdAt: string
}

export interface RoleApplyReviewRequest {
  status: number
  adminNote?: string
}

export const adminApi = {
  // 角色
  getRoles() {
    return request.get<SysRole[]>('/admin/roles')
  },

  // 用户管理
  listUsers(params?: { page?: number; size?: number; username?: string }) {
    return request.get<PageResponse<UserProfile>>('/admin/users', { params })
  },
  getUser(id: number) {
    return request.get<UserProfile>(`/admin/users/${id}`)
  },
  updateUser(id: number, data: AdminUserUpdateRequest) {
    return request.put<UserProfile>(`/admin/users/${id}`, data)
  },

  // 角色申请审核
  listRoleApplies(params?: { page?: number; size?: number; status?: number }) {
    return request.get<PageResponse<RoleApply>>('/admin/role-apply', { params })
  },
  reviewRoleApply(id: number, data: RoleApplyReviewRequest) {
    return request.put<RoleApply>(`/admin/role-apply/${id}/review`, data)
  },

  // 装备管理
  listGears(params?: {
    categoryId?: number
    name?: string
    brand?: string
    priceMin?: number
    priceMax?: number
    season?: string
    scene?: string
    tagId?: number
    status?: number
    page?: number
    size?: number
  }) {
    return request.get<PageResponse<GearListItem>>('/admin/gears', { params })
  },
  getGear(id: number) {
    return request.get<GearDetail>(`/admin/gears/${id}`)
  },
  createGear(data: {
    name: string
    categoryId: number
    brand?: string
    price: number
    weight?: number
    season?: string
    scene?: string
    comfortTemperature?: string
    maxUsers?: number
    stock: number
    coverImage?: string
    imageList?: string
    description?: string
    status?: number
    tagIds?: number[]
  }) {
    return request.post<GearDetail>('/admin/gears', data)
  },
  updateGear(id: number, data: Partial<{
    name: string
    categoryId: number
    brand: string
    price: number
    weight: number
    season: string
    scene: string
    comfortTemperature: string
    maxUsers: number
    stock: number
    coverImage: string
    imageList: string
    description: string
    status: number
    tagIds: number[]
  }>) {
    return request.put<GearDetail>(`/admin/gears/${id}`, data)
  },
  updateGearStatus(id: number, status: number) {
    return request.patch<{ message: string }>(`/admin/gears/${id}/status`, { status })
  },
  deleteGear(id: number) {
    return request.delete<{ message: string }>(`/admin/gears/${id}`)
  },
}
