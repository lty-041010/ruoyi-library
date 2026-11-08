import request from '@/utils/request'

// 获取首页看板数据
export function getDashboardStats() {
  return request({
    url: '/book/dashboard/stats',
    method: 'get'
  })
}