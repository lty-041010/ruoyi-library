import request from '@/utils/request'

export function getMyNotices() {
  return request({
    url: '/book/notice/my',
    method: 'get'
  })
}

export function getUnreadCount() {
  return request({
    url: '/book/notice/unread',
    method: 'get'
  })
}

export function readNotice(id) {
  return request({
    url: '/book/notice/read/' + id,
    method: 'put'
  })
}

export function readAllNotices() {
  return request({
    url: '/book/notice/readAll',
    method: 'put'
  })
}
