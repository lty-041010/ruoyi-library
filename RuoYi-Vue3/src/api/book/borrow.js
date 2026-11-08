import request from '@/utils/request' // ★ 必须加上这一行
import { createCrudApi } from '@/utils/crud'

const baseApi = createCrudApi('/book/borrow')

export const listBorrow = baseApi.list
export const getBorrow = baseApi.get
export const addBorrow = baseApi.add
export const updateBorrow = baseApi.update
export const delBorrow = baseApi.del

// 查询跨馆代还列表
export function listCrossReturn(query) {
  return request({
    url: '/book/borrow/crossReturn/list',
    method: 'get',
    params: query,
    
  })
}