import request from '@/utils/request'

// 后台预约列表
export function listReservation(query) {
  return request({
    url: '/book/reservation/list',
    method: 'get',
    params: query
  })
}

// 到书核销
export function handleReservation(id) {
  return request({
    url: '/book/reservation/handle/' + id,
    method: 'put'
  })
}

// 后台取消预约
export function cancelReservationByAdmin(id) {
  return request({
    url: '/book/reservation/cancel/' + id,
    method: 'put'
  })
}

// 读者：我的预约
export function listMyReservation() {
  return request({
    url: '/book/reservation/my',
    method: 'get'
  })
}

// 读者：预约图书
export function addReservation(bookId) {
  return request({
    url: '/book/reservation',
    method: 'post',
    data: { bookId }
  })
}

// 读者：取消预约
export function cancelMyReservation(id) {
  return request({
    url: '/book/reservation/' + id,
    method: 'delete'
  })
}
