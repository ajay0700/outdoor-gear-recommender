import request from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
}

export interface AuthResponse {
  token: string
  username: string
  nickname?: string
  roles: string[]
}

export const authApi = {
  login(data: LoginRequest) {
    return request.post<AuthResponse>('/auth/login', data)
  },
  register(data: RegisterRequest) {
    return request.post<AuthResponse>('/auth/register', data)
  },
}
