import { defineAsyncComponent } from 'vue'

export default {
  install(app) {
    // 扫描 components 下所有的 index.vue
    const components = import.meta.glob('./*/index.vue')
    
    for (const [key, value] of Object.entries(components)) {
      // 提取目录名作为组件名，例如 './Pagination/index.vue' -> 'Pagination'
      const componentName = key.split('/')[1]
      // 异步注册全局组件
      app.component(componentName, defineAsyncComponent(value))
    }
  }
}