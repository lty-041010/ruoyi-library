<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="书名" prop="bookName">
        <el-input v-model="queryParams.bookName" placeholder="请输入书名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="作者" prop="author">
        <el-input v-model="queryParams.author" placeholder="请输入作者" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="ISBN" prop="isbn">
        <el-input v-model="queryParams.isbn" placeholder="请输入ISBN" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="dict in book_status" :key="dict.value" :label="dict.label" :value="dict.value" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['book:info:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['book:info:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['book:info:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['book:info:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Share"
          :disabled="multiple"
          @click="handleAssignDept"
          v-hasRole="['admin']"
        >批量划拨分馆</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="infoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      
      <el-table-column label="封面" align="center" prop="coverImage" width="60">
        <template #default="scope">
          <image-preview :src="scope.row.coverImage" :width="35" :height="35"/>
        </template>
      </el-table-column>
      
      <el-table-column label="书名" align="center" prop="bookName" min-width="130" show-overflow-tooltip />
      <el-table-column label="作者" align="center" prop="author" min-width="90" show-overflow-tooltip />
      
      <el-table-column label="所属分类" align="center" prop="categoryId" width="90">
        <template #default="scope">
          <span>{{ getCategoryName(scope.row.categoryId) }}</span>
        </template>
      </el-table-column>

      <!-- ★ 新增：所属分馆列 -->
      <el-table-column label="所属分馆" align="center" prop="deptName" width="120" show-overflow-tooltip />

      <el-table-column label="ISBN" align="center" prop="isbn" width="115" show-overflow-tooltip />
      <el-table-column label="出版社" align="center" prop="publisher" min-width="100" show-overflow-tooltip />
      
      <el-table-column label="出版日期" align="center" prop="publishDate" width="100">
        <template #default="scope">
          <span>{{ parseTime(scope.row.publishDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="价格" align="center" prop="price" width="70" />

      <el-table-column label="库存(可用/总)" align="center" width="110">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold;" v-if="scope.row.availableStock === 0">0</span>
          <span style="color: #67c23a; font-weight: bold;" v-else>{{ scope.row.availableStock }}</span>
          <span style="color: #909399;"> / {{ scope.row.totalStock }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="book_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['book:info:edit']">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['book:info:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 批量划拨分馆弹窗 -->
    <el-dialog title="批量划拨分馆" v-model="deptOpen" width="400px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="目标分馆">
          <el-tree-select
            v-model="targetDeptId"
            :data="deptOptions"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            value-key="id"
            placeholder="请选择要划拨的分馆"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAssignDept">确 定</el-button>
          <el-button @click="deptOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加或修改图书信息对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="infoRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="书名" prop="bookName">
              <el-input v-model="form.bookName" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN 编号" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入ISBN 编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版日期" prop="publishDate">
              <el-date-picker clearable v-model="form.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择出版日期" style="width: 100%;">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格(元)" prop="price">
              <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="总库存" prop="totalStock">
              <el-input-number v-model="form.totalStock" :min="0" @change="handleStockChange" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可用库存" prop="availableStock">
              <el-input-number v-model="form.availableStock" :min="0" disabled style="width: 100%;" />
            </el-form-item>
          </el-col>

          <el-col :span="12" v-if="isAdminUser">
            <el-form-item label="所属分馆" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                value-key="id"
                placeholder="默认当前分馆"
                check-strictly
                clearable
                :disabled="form.id != null"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择所属分类" style="width: 100%;">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.id"
                  :label="item.categoryName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in book_status" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          
          <el-col :span="24">
            <el-form-item label="封面图片" prop="coverImage">
              <image-upload v-model="form.coverImage"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="图书简介" prop="description">
              <el-input v-model="form.description" type="textarea" placeholder="请输入内容" :rows="3" />
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

<script setup name="Info">
import { onActivated } from 'vue'
import { listInfo, getInfo, delInfo, addInfo, updateInfo, assignDept } from "@/api/book/info"
import { listCategory } from "@/api/book/category" 
import { deptTreeSelect } from "@/api/system/user"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const { book_status } = useDict('book_status')
const userStore = useUserStore()
const isAdminUser = computed(() => userStore.roles.includes('admin'))

const infoList = ref([])
const categoryOptions = ref([]) 
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const deptOpen = ref(false)
const targetDeptId = ref(null)
const deptOptions = ref([])
const borrowedNum = ref(0)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    isbn: undefined,
    bookName: undefined,
    author: undefined,
    status: undefined,
  },
  rules: {
    isbn: [{ required: true, message: "ISBN 编号不能为空", trigger: "blur" }],
    bookName: [{ required: true, message: "书名不能为空", trigger: "blur" }],
    price: [{ required: true, message: "价格不能为空", trigger: "blur" }],
    totalStock: [{ required: true, message: "总库存不能为空", trigger: "blur" }],
    categoryId: [{ required: true, message: "请选择所属分类", trigger: "change" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }],
  }
})

const { queryParams, form, rules } = toRefs(data)

function getCategoryOptions() {
  listCategory({ pageSize: 1000 }).then(res => {
    categoryOptions.value = res.rows
  })
}

function getCategoryName(id) {
  const category = categoryOptions.value.find(item => item.id === id)
  return category ? category.categoryName : id
}

function handleStockChange(value) {
  if (!form.value.id) {
    form.value.availableStock = value
  } else {
    form.value.availableStock = value - borrowedNum.value;
    if (form.value.availableStock < 0) {
      proxy.$modal.msgWarning(`注意：该书目前已被借出 ${borrowedNum.value} 本，总库存不能低于此数量！`);
      form.value.availableStock = 0; 
    }
  }
}

function getList() {
  loading.value = true
  listInfo(queryParams.value).then(response => {
    infoList.value = response.rows
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
    id: null, isbn: null, bookName: null, author: null, publisher: null,
    publishDate: null, price: null, totalStock: 0, availableStock: 0,
    coverImage: null, description: null, categoryId: null, status: "0", remark: null,
    deptId: null
  }
  borrowedNum.value = 0
  proxy.resetForm("infoRef")
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
  title.value = "添加图书信息"
}

function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getInfo(_id).then(response => {
    form.value = response.data
    borrowedNum.value = (form.value.totalStock || 0) - (form.value.availableStock || 0)
    
    open.value = true
    title.value = "修改图书信息"
  })
}

function submitForm() {
  proxy.$refs["infoRef"].validate(valid => {
    if (valid) {
      if (isAdminUser.value && form.value.id == null && !form.value.deptId) {
        proxy.$modal.msgWarning("请选择所属分馆")
        return
      }
      if (form.value.id != null) {
        updateInfo(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addInfo(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除图书信息编号为"' + _ids + '"的数据项？').then(function() {
    return delInfo(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('book/info/export', {
    ...queryParams.value
  }, `info_${new Date().getTime()}.xlsx`)
}

// ============ 划拨逻辑 ============

function handleAssignDept() {
  targetDeptId.value = null
  deptOpen.value = true
  deptTreeSelect().then(response => {
    deptOptions.value = response.data
  })
}

function submitAssignDept() {
  if (!targetDeptId.value) {
    proxy.$modal.msgWarning("请选择目标分馆")
    return
  }
  
  proxy.$modal.confirm('确认将选中的 ' + ids.value.length + ' 条图书划拨到该分馆吗？').then(function() {
    return assignDept({ ids: ids.value, deptId: targetDeptId.value })
  }).then(() => {
    proxy.$modal.msgSuccess("划拨成功")
    deptOpen.value = false
    getList()
  }).catch(() => {})
}

function loadDeptOptions() {
  deptTreeSelect().then(response => {
    deptOptions.value = response.data
  })
}

getCategoryOptions()
getList()
if (isAdminUser.value) {
  loadDeptOptions()
}

onActivated(() => {
  getList()
})
</script>