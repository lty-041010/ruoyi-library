import request from '@/utils/request'

// 读者门户：馆藏检索
export function listPortalBooks(query) {
  return request({
    url: '/book/info/list',
    method: 'get',
    params: query
  })
}

// 读者门户：分类
export function listPortalCategories() {
  return request({
    url: '/book/category/list',
    method: 'get',
    params: { status: '0' }
  })
}

// 我的借阅
export function listMyBorrows(query) {
  return request({
    url: '/book/borrow/list',
    method: 'get',
    params: query
  })
}

// 续借
export function renewBorrow(id) {
  return request({
    url: '/book/borrow/renew/' + id,
    method: 'post'
  })
}

// 我的预约
export function listMyReservations() {
  return request({
    url: '/book/reservation/my',
    method: 'get'
  })
}

// 预约图书
export function addReservation(bookId) {
  return request({
    url: '/book/reservation',
    method: 'post',
    data: { bookId }
  })
}

// 取消预约
export function cancelReservation(id) {
  return request({
    url: '/book/reservation/' + id,
    method: 'delete'
  })
}

// 门户统计（复用看板接口）
export function getPortalStats() {
  return request({
    url: '/book/dashboard/stats',
    method: 'get'
  })
}
