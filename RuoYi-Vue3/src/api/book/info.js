import request from '@/utils/request'

// 查询图书信息列表
export function listInfo(query) {
  return request({
    url: '/book/info/list',
    method: 'get',
    params: query
  })
}

// 查询图书信息详细
export function getInfo(id) {
  return request({
    url: '/book/info/' + id,
    method: 'get'
  })
}

// 新增图书信息
export function addInfo(data) {
  return request({
    url: '/book/info',
    method: 'post',
    data: data
  })
}

// 修改图书信息
export function updateInfo(data) {
  return request({
    url: '/book/info',
    method: 'put',
    data: data
  })
}

// 删除图书信息
export function delInfo(id) {
  return request({
    url: '/book/info/' + id,
    method: 'delete'
  })
}

// 批量分配部门
export function assignDept(data) {
  return request({
    url: '/book/info/assignDept',
    method: 'put',
    data: data
  })
}