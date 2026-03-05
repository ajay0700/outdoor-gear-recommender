import request from './request'

export interface GearCategory {
  id: number
  name: string
  parentId: number
  sortOrder: number
}

export interface GearListItem {
  id: number
  name: string
  categoryId: number
  categoryName: string
  brand: string
  price: number
  weight: number | null
  season: string | null
  scene: string | null
  stock: number
  coverImage: string | null
  status: number
  createdAt: string
  tagNames: string[]
}

export interface GearRating {
  id: number
  userId: number
  userNickname: string
  score: number
  comment: string | null
  createdAt: string
}

export interface GearDetail {
  id: number
  name: string
  categoryId: number
  categoryName: string
  brand: string
  price: number
  weight: number | null
  season: string | null
  scene: string | null
  stock: number
  coverImage: string | null
  status: number
  createdAt: string
  tagNames: string[]
  /** 按类型分组的标签，如 { "产品特性": ["轻量化","防水"], "适用场景": ["徒步","露营"] } */
  tagGroups?: Record<string, string[]>
  comfortTemperature: string | null
  maxUsers: number | null
  imageList: string | null
  description: string | null
  avgScore: number | null
  ratingCount: number
  ratings: GearRating[]
  isFavorite: boolean
  cartQuantity: number
  /** 同分类推荐 */
  relatedGears?: GearListItem[]
}

export interface GearListParams {
  categoryId?: number
  name?: string
  brand?: string
  priceMin?: number
  priceMax?: number
  season?: string
  scene?: string
  tagId?: number
  page?: number
  size?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface GearTag {
  id: number
  name: string
  type: string | null
  createdAt: string
  updatedAt: string
}

export const gearApi = {
  getCategories() {
    return request.get<GearCategory[]>('/gears/categories')
  },

  getTags() {
    return request.get<GearTag[]>('/gears/tags')
  },

  list(params?: GearListParams) {
    return request.get<PageResponse<GearListItem>>('/gears', { params })
  },

  getDetail(id: number) {
    return request.get<GearDetail>(`/gears/${id}`)
  },

  rate(id: number, data: { score: number; comment?: string }) {
    return request.post<{ message: string }>(`/gears/${id}/rating`, data)
  },

  compare(ids: number[]) {
    return request.get<GearDetail[]>('/gears/compare', {
      params: { ids: ids.join(',') },
    })
  },
}
