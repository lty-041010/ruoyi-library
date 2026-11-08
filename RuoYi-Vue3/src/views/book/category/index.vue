<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="queryParams.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['book:category:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['book:category:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['book:category:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['book:category:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="categoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      
      <el-table-column label="分类名称" align="center" prop="categoryName" min-width="200" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ getFullCategoryName(scope.row) }}</span>
        </template>
      </el-table-column>

      <el-table-column v-if="checkRole(['admin', 'book_manager'])" label="显示顺序" align="center" prop="orderNum" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="200" show-overflow-tooltip />
      
      <el-table-column label="操作" align="center" width="120" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['book:category:edit']">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['book:category:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="父分类" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="categoryTreeOptions"
                :props="{ value: 'id', label: 'categoryName', children: 'children' }"
                value-key="id"
                placeholder="请选择父分类（不选则为顶级）"
                check-strictly
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="分类名称" prop="categoryName">
              <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Category">
import { listCategory, getCategory, delCategory, addCategory, updateCategory } from "@/api/book/category"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict('sys_normal_disable')

const categoryList = ref([])
const categoryOptions = ref([]) 
const categoryTreeOptions = ref([]) 
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const userStore = useUserStore()

function checkRole(roles) {
  if (userStore.roles.includes('admin')) return true
  return roles.some(role => userStore.roles.includes(role));
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    categoryName: undefined,
    status: undefined,
  },
  rules: {
    // ★ 修改：去掉了 parentId 的必填，因为默认是 0（顶级）
    categoryName: [{ required: true, message: "分类名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "显示顺序不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function buildTree(data) {
  const res = []
  const map = {}
  
  res.push({ id: 0, categoryName: '顶级分类', children: [] })
  map[0] = res[0]

  data.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })

  data.forEach(item => {
    if (map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      res[0].children.push(map[item.id])
    }
  })
  return res
}

function getCategoryOptions() {
  listCategory({ pageSize: 1000 }).then(res => {
    categoryOptions.value = res.rows
    categoryTreeOptions.value = buildTree(res.rows)
  })
}

function getFullCategoryName(row) {
  if (row.parentId === 0) return row.categoryName
  
  const parent = categoryOptions.value.find(item => item.id === row.parentId)
  if (parent) {
    const parentName = getFullCategoryName(parent)
    return `${parentName} / ${row.categoryName}`
  }
  return row.categoryName
}

function getList() {
  loading.value = true
  listCategory(queryParams.value).then(response => {
    categoryList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    id: null,
    parentId: 0, 
    categoryName: null,
    orderNum: 0,
    status: "0",
    remark: null
  }
  proxy.resetForm("categoryRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加图书分类"
}

function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getCategory(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改图书分类"
  })
}

function submitForm() {
  proxy.$refs["categoryRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateCategory(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
          getCategoryOptions()
        })
      } else {
        addCategory(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
          getCategoryOptions()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除图书分类编号为"' + _ids + '"的数据项？').then(function() {
    return delCategory(_ids)
  }).then(() => {
    getList()
    getCategoryOptions()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('book/category/export', {
    ...queryParams.value
  }, `category_${new Date().getTime()}.xlsx`)
}

getCategoryOptions()
getList()
</script>