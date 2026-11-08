<template>
  <div class="app-container">
    <!-- 搜索栏 (无权限时隐藏) -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch && hasPermission" label-width="80px">
      <el-form-item label="书名" prop="bookName">
        <el-input v-model="queryParams.bookName" placeholder="请输入书名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作人" prop="operator">
        <el-input v-model="queryParams.operator" placeholder="请输入操作人" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 (无权限时隐藏) -->
    <el-row :gutter="10" class="mb8" v-if="hasPermission">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['book:transfer:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['book:transfer:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- ================= 核心优化：无权限时的友好提示 ================= -->
    <el-empty v-if="!hasPermission" description="抱歉，只有超级管理员才能查看全局调拨记录哦~" :image-size="200" style="margin-top: 80px;" />
    
    <!-- 有权限时的数据表格 -->
    <div v-else>
      <el-table v-loading="loading" :data="transferList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="记录ID" align="center" prop="id" width="80" />
        <el-table-column label="图书名称" align="center" prop="bookName" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="原分馆" align="center" prop="sourceDeptName" width="150">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.sourceDeptName || '未知分馆' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="目标分馆" align="center" prop="targetDeptName" width="150">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.targetDeptName || '未知分馆' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作人" align="center" prop="operator" width="120" />
        <el-table-column label="调拨时间" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="Transfer">
import { ref, computed } from 'vue'
import { listTransfer, delTransfer } from "@/api/book/transfer"
import useUserStore from '@/store/modules/user' // ★ 引入用户状态

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// ★ 核心逻辑：判断当前用户是否是超级管理员
const hasPermission = computed(() => {
  return userStore.roles.includes('admin')
})

const transferList = ref([])
const loading = ref(false) // 默认设为 false
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    bookName: undefined,
    operator: undefined
  }
})

const { queryParams } = toRefs(data)

/** 查询列表 */
function getList() {
  // 如果没有权限，直接返回，不发起请求，避免报错弹窗
  if (!hasPermission.value) return;

  loading.value = true
  listTransfer(queryParams.value).then(response => {
    transferList.value = response.rows
    total.value = response.total
  }).catch(() => {
    // 就算后台报错，也要把转圈关掉
    transferList.value = []
  }).finally(() => {
    loading.value = false // ★ 无论成功失败，都关闭加载状态
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 删除按钮操作 */
function handleDelete() {
  proxy.$modal.confirm('是否确认删除选中的 ' + ids.value.length + ' 条调拨记录？删除后不可恢复！').then(function() {
    return delTransfer(ids.value)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('book/transfer/export', {
    ...queryParams.value
  }, `transfer_${new Date().getTime()}.xlsx`)
}

// 初始化：只有有权限才去查
if (hasPermission.value) {
  getList()
}
</script>