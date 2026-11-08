<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="借书卡号" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="请输入借书卡号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="读者姓名" prop="realName">
        <el-input v-model="queryParams.realName" placeholder="请输入读者姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['book:reader:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['book:reader:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['book:reader:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['book:reader:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="readerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="借书卡号" align="center" prop="cardNo" width="130" />
      <el-table-column label="读者姓名" align="center" prop="realName" width="120" />
      <el-table-column label="所属分馆" align="center" prop="deptName" width="110" show-overflow-tooltip />
      <el-table-column label="联系电话" align="center" prop="phone" width="130" />
      
      <el-table-column label="借阅情况(已借/上限)" align="center" width="150">
        <template #default="scope">
          <span style="color: #409eff; font-weight: bold;">{{ scope.row.currentBorrowNum }}</span>
          <span style="color: #909399;"> / {{ scope.row.maxBorrowNum }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="150" show-overflow-tooltip />
      
      <el-table-column label="操作" align="center" width="110" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['book:reader:edit']">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['book:reader:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改读者对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="readerRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="借书卡号" prop="cardNo">
              <el-input v-model="form.cardNo" placeholder="请输入借书卡号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
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
            <el-form-item label="最大可借数" prop="maxBorrowNum">
              <el-input-number v-model="form.maxBorrowNum" :min="0" style="width: 100%;" />
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

<script setup name="Reader">
import { onActivated } from 'vue'
import { listReader, getReader, delReader, addReader, updateReader } from "@/api/book/reader"
import { deptTreeSelect } from "@/api/system/user"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict('sys_normal_disable')
const userStore = useUserStore()
const isAdminUser = computed(() => userStore.roles.includes('admin'))
const deptOptions = ref([])

const readerList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    cardNo: undefined,
    realName: undefined,
    phone: undefined,
    status: undefined,
  },
  rules: {
    cardNo: [{ required: true, message: "借书卡号不能为空", trigger: "blur" }],
    realName: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
    maxBorrowNum: [{ required: true, message: "最大可借数不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "change" }],
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listReader(queryParams.value).then(response => {
    readerList.value = response.rows
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
    userId: null,
    cardNo: null, 
    realName: null, 
    phone: null,
    maxBorrowNum: 5,
    currentBorrowNum: 0, 
    status: "0", 
    remark: null,
    deptId: null
  }
  proxy.resetForm("readerRef")
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
  title.value = "添加读者"
}

function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getReader(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改读者"
  })
}

function submitForm() {
  proxy.$refs["readerRef"].validate(valid => {
    if (valid) {
      if (isAdminUser.value && form.value.id == null && !form.value.deptId) {
        proxy.$modal.msgWarning("请选择所属分馆")
        return
      }
      if (form.value.id != null) {
        updateReader(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addReader(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除读者编号为"' + _ids + '"的数据项？').then(function() {
    return delReader(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download('book/reader/export', {
    ...queryParams.value
  }, `reader_${new Date().getTime()}.xlsx`)
}

if (isAdminUser.value) {
  deptTreeSelect().then(res => {
    deptOptions.value = res.data
  })
}

getList()

onActivated(() => {
  getList()
})
</script>