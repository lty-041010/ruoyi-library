import request from '@/utils/request'

// 读者：我的购书留言
export function listMyWishes() {
  return request({
    url: '/book/wish/my',
    method: 'get'
  })
}

// 读者：提交购书留言
export function addWish(data) {
  return request({
    url: '/book/wish',
    method: 'post',
    data
  })
}

// 读者：取消留言
export function cancelWish(id) {
  return request({
    url: '/book/wish/' + id,
    method: 'delete'
  })
}

// 后台：留言列表
export function listWish(query) {
  return request({
    url: '/book/wish/list',
    method: 'get',
    params: query
  })
}

export function approveWish(id, reply) {
  return request({
    url: '/book/wish/approve/' + id,
    method: 'put',
    data: { reply }
  })
}

export function rejectWish(id, reply) {
  return request({
    url: '/book/wish/reject/' + id,
    method: 'put',
    data: { reply }
  })
}

export function onShelfWish(id) {
  return request({
    url: '/book/wish/onshelf/' + id,
    method: 'put',
    data: {}
  })
}
