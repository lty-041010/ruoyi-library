// src/utils/crud.js
import request from '@/utils/request'

export function createCrudApi(baseUrl) {
  return {
    list: (query) => request({ url: `${baseUrl}/list`, method: 'get', params: query }),
    get: (id) => request({ url: `${baseUrl}/${id}`, method: 'get' }),
    add: (data) => request({ url: baseUrl, method: 'post', data }),
    update: (data) => request({ url: baseUrl, method: 'put', data }),
    del: (id) => request({ url: `${baseUrl}/${id}`, method: 'delete' })
  }
}