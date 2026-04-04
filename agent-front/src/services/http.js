import axios from 'axios'

export const apiClient = axios.create({
  baseURL: '/api',
  timeout: 180000
})

export function buildApiUrl(url, params = {}) {
  return apiClient.getUri({
    url,
    params
  })
}
