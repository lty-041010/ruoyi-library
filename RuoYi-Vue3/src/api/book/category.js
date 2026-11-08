import request from '@/utils/request'

// 查询图书分类列表
export function listCategory(query) {
  return request({
    url: '/book/category/list',
    method: 'get',
    params: query
  })
}

// 查询图书分类详细
export function getCategory(id) {
  return request({
    url: '/book/category/' + id,
    method: 'get'
  })
}

// 新增图书分类
export function addCategory(data) {
  return request({
    url: '/book/category',
    method: 'post',
    data: data
  })
}

// 修改图书分类
export function updateCategory(data) {
  return request({
    url: '/book/category',
    method: 'put',
    data: data
  })
}

// 删除图书分类
export function delCategory(id) {
  return request({
    url: '/book/category/' + id,
    method: 'delete'
  })
}