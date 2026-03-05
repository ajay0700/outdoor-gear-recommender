import request from './request'

export interface RecommendItem {
  gearId: number
  gearName: string
  categoryId: number
  categoryName: string
  brand: string
  price: number
  coverImage: string | null
  score: number
  algorithmHint: string | null
}

export interface RecommendConstraint {
  season?: string
  budgetMin?: number
  budgetMax?: number
  scene?: string
  peopleCount?: number
  categoryId?: number
  excludeGearIds?: number[]
}

export interface RecommendRequest {
  planId?: number
  userId?: number
  constraint?: RecommendConstraint
  topN?: number
}

export const recommendApi = {
  byPlan(planId: number, topN = 20) {
    return request.get<RecommendItem[]>('/recommend/by-plan', {
      params: { planId, topN },
    })
  },

  recommend(data: RecommendRequest) {
    return request.post<RecommendItem[]>('/recommend', data)
  },
}
