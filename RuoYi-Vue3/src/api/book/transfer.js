import request from '@/utils/request'

// 查询图书调拨记录列表
export function listTransfer(query) {
  return request({
    url: '/book/transfer/list',
    method: 'get',
    params: query
  })
}
// 删除调拨记录
export function delTransfer(ids) {
  return request({
    url: '/book/transfer/' + ids,
    method: 'delete'
  })
}