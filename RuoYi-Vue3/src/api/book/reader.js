import request from '@/utils/request'

// 查询读者扩展列表
export function listReader(query) {
  return request({
    url: '/book/reader/list',
    method: 'get',
    params: query
  })
}

// 查询读者扩展详细
export function getReader(id) {
  return request({
    url: '/book/reader/' + id,
    method: 'get'
  })
}

// 新增读者扩展
export function addReader(data) {
  return request({
    url: '/book/reader',
    method: 'post',
    data: data
  })
}

// 修改读者扩展
export function updateReader(data) {
  return request({
    url: '/book/reader',
    method: 'put',
    data: data
  })
}

// 删除读者扩展
export function delReader(id) {
  return request({
    url: '/book/reader/' + id,
    method: 'delete'
  })
}