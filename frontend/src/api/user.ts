import request from './request'

export interface UserPreference {
  season?: string
  activityType?: string
  budgetMin?: number
  budgetMax?: number
  /** 偏好目的地，逗号分隔 */
  preferredDestinations?: string
  /** 偏好装备分类，逗号分隔 */
  preferredCategories?: string
  /** 偏好标签，逗号分隔 */
  preferredTags?: string
  /** 难度偏好 */
  difficultyPreference?: string
}

export interface CartItem {
  id: number
  gearId: number
  gearName: string
  quantity: number
  price: number
  selected: boolean
}

export const userApi = {
  getPreferences() {
    return request.get<UserPreference>('/user/preferences')
  },

  savePreferences(data: UserPreference) {
    return request.put<UserPreference>('/user/preferences', data)
  },

  addFavorite(gearId: number) {
    return request.post<{ message: string }>(`/user/favorites/${gearId}`)
  },

  removeFavorite(gearId: number) {
    return request.delete<{ message: string }>(`/user/favorites/${gearId}`)
  },

  getCart() {
    return request.get<CartItem[]>('/user/cart')
  },

  addToCart(gearId: number, quantity = 1) {
    return request.post<{ message: string }>('/user/cart', {
      gearId,
      quantity,
    })
  },

  updateCartItem(id: number, quantity: number) {
    return request.patch<{ message: string }>(`/user/cart/${id}`, { quantity })
  },

  removeCartItem(id: number) {
    return request.delete<{ message: string }>(`/user/cart/${id}`)
  },
}
