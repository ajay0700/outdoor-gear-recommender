import request from './request'

export interface TripPlanGear {
  id: number
  planId: number
  gearId: number
  gearName: string
  source: string
  quantity: number
}

export interface TripPlan {
  id: number
  userId: number
  name: string
  destination: string | null
  startDate: string | null
  endDate: string | null
  days: number | null
  peopleCount: number | null
  budget: number | null
  season: string | null
  activityType: string | null
  difficultyLevel: number | null
  note: string | null
  requirementText: string | null
  extractedKeywords: string | null
  status: number
  createdAt: string
  updatedAt: string
  gears: TripPlanGear[]
}

export interface TripPlanCreateRequest {
  name: string
  destination?: string
  startDate?: string
  endDate?: string
  days?: number
  peopleCount?: number
  budget?: number
  season?: string
  activityType?: string
  difficultyLevel?: number
  note?: string
  /** 需求描述（自由文本），系统自动提取关键词用于推荐 */
  requirementText?: string
  status?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const planApi = {
  list(page = 0, size = 20) {
    return request.get<PageResponse<TripPlan>>('/user/plans', {
      params: { page, size },
    })
  },

  get(id: number) {
    return request.get<TripPlan>(`/user/plans/${id}`)
  },

  create(data: TripPlanCreateRequest) {
    return request.post<TripPlan>('/user/plans', data)
  },

  update(id: number, data: TripPlanCreateRequest) {
    return request.put<TripPlan>(`/user/plans/${id}`, data)
  },

  delete(id: number) {
    return request.delete<{ message: string }>(`/user/plans/${id}`)
  },

  addGear(planId: number, gearId: number, quantity = 1, source = 'MANUAL') {
    return request.post<{ message: string }>(`/user/plans/${planId}/gears`, {
      gearId,
      quantity,
      source,
    })
  },

  removeGear(planId: number, gearId: number) {
    return request.delete<{ message: string }>(
      `/user/plans/${planId}/gears/${gearId}`
    )
  },
}
