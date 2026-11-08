<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="借阅状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="dict in book_borrow_status" :key="dict.value" :label="dict.label" :value="dict.value" />
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
        <el-button type="success" plain icon="FullScreen" @click="openScan" v-hasPermi="['book:borrow:add']">扫码借还</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Position" @click="openCrossReturn" v-hasPermi="['book:borrow:edit']">跨馆代还</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['book:borrow:add']">登记借阅</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格（已移除合计行） -->
    <el-table v-loading="loading" :data="borrowList">
      <el-table-column label="记录ID" align="center" prop="id" width="80" />
      <el-table-column label="读者" align="center" prop="readerId" width="120">
        <template #default="scope">
          <span>{{ scope.row.readerName || ('读者#' + scope.row.readerId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="图书名称" align="center" prop="bookId" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.bookName || ('未知图书#' + scope.row.bookId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="借出时间" align="center" prop="borrowTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.borrowTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应还时间" align="center" prop="dueTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.dueTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="book_borrow_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="逾期天数" align="center" prop="overdueDays" width="90">
        <template #default="scope">
          <span :class="{ 'text-danger': scope.row.overdueDays > 0 }">{{ scope.row.overdueDays || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="滞纳金" align="center" prop="fineAmount" width="100">
        <template #default="scope">
          <span v-if="Number(scope.row.fineAmount || 0) > 0" class="text-danger">¥{{ Number(scope.row.fineAmount).toFixed(2) }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '2'"
            link type="success" @click="handleReturn(scope.row)" v-hasPermi="['book:borrow:edit']">归还</el-button>

          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['book:borrow:edit']">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['book:borrow:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="borrowRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="选择读者" prop="readerId">
              <el-select v-model="form.readerId" placeholder="请选择读者" filterable :loading="readerLoading" :disabled="form.id != null" style="width: 100%;">
                <el-option
                  v-for="item in readerOptions"
                  :key="item.id"
                  :label="item.realName + ' (' + item.cardNo + ')'"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="选择图书" prop="bookId">
              <el-input v-model="form.bookName" placeholder="请点击右侧按钮选择图书" readonly @click="form.id == null && openBookDialog()">
                <template #append>
                  <el-button icon="Search" :disabled="form.id != null" @click="form.id == null && openBookDialog()" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <!-- 新增：借出时间 -->
          <el-col :span="24">
            <el-form-item label="借出时间" prop="borrowTime">
              <el-date-picker
                v-model="form.borrowTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择借出时间"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <!-- 应还时间：增加disabledDate，禁止早于借出时间 -->
          <el-col :span="24">
            <el-form-item label="应还时间" prop="dueTime">
              <el-date-picker
                v-model="form.dueTime"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择应还时间"
                style="width: 100%;"
                :disabled-date="(time) => {
                  if (!form.borrowTime) return false
                  // 只禁用借出日期之前的日期，同一天仍可选（具体时分由校验规则保证）
                  return time.getTime() < new Date(form.borrowTime).setHours(0, 0, 0, 0)
                }"
              />
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

    <!-- 图书选择弹窗 -->
    <el-dialog title="选择图书" v-model="bookDialogOpen" width="900px" append-to-body>
      <el-form :model="bookQuery" :inline="true">
        <el-form-item label="书名">
          <el-input v-model="bookQuery.bookName" placeholder="请输入书名" @keyup.enter="searchBooks" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBooks">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="bookList" highlight-current-row @row-click="handleBookSelect" height="400px">
        <el-table-column label="书名" prop="bookName" show-overflow-tooltip />
        <el-table-column label="作者" prop="author" width="120" show-overflow-tooltip />
        <el-table-column label="ISBN" prop="isbn" width="150" />
        <el-table-column label="库存" prop="availableStock" width="80" align="center">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;" v-if="scope.row.availableStock <= 0">0</span>
            <span style="color: #67c23a; font-weight: bold;" v-else>{{ scope.row.availableStock }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="bookTotal>0" :total="bookTotal" v-model:page="bookQuery.pageNum" v-model:limit="bookQuery.pageSize" @pagination="searchBooks" />
    </el-dialog>

    <!-- 跨馆代还弹窗（工作台模式） -->
    <el-dialog title="跨馆代还工作台" v-model="crossOpen" width="900px" append-to-body>
      <el-alert title="这里显示全系统所有未归还的借阅记录。如果是外馆的书，点击“代还”将自动把书转移到本馆。" type="info" show-icon style="margin-bottom: 15px;" />

      <el-form :model="crossQuery" :inline="true" size="small">
        <el-form-item label="读者卡号">
          <el-input v-model="crossQuery.cardNo" placeholder="请输入卡号" clearable @keyup.enter="getCrossList" />
        </el-form-item>
        <el-form-item label="书名">
          <el-input v-model="crossQuery.bookName" placeholder="请输入书名" clearable @keyup.enter="getCrossList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getCrossList">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="crossLoading" :data="crossList" height="400px">
        <el-table-column label="读者" prop="readerName" width="120" />
        <el-table-column label="卡号" prop="cardNo" width="120" />
        <el-table-column label="书名" prop="bookName" show-overflow-tooltip />
        <el-table-column label="所属分馆" width="150">
          <template #default="scope">
            <el-tag v-if="scope.row.isLocal === 1" type="success">本馆</el-tag>
            <el-tag v-else type="warning">外馆代还</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应还时间" prop="dueTime" width="160">
          <template #default="scope">
            <span :style="scope.row.status === '2' ? 'color: red;' : ''">
              {{ parseTime(scope.row.dueTime, '{y}-{m}-{d}') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="submitCrossReturn(scope.row)">立即代还</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="crossTotal>0" :total="crossTotal" v-model:page="crossQuery.pageNum" v-model:limit="crossQuery.pageSize" @pagination="getCrossList" />
    </el-dialog>

    <!-- 扫码借还 -->
    <el-dialog v-model="scanOpen" title="扫码借还" width="460px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="借书证号">
          <el-input v-model="scanForm.cardNo" placeholder="扫描或输入读者借书证号" clearable @keyup.enter="submitScan('borrow')" />
        </el-form-item>
        <el-form-item label="图书 ISBN">
          <el-input v-model="scanForm.isbn" placeholder="扫描或输入图书 ISBN" clearable @keyup.enter="submitScan('borrow')" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon
          title="扫码枪输入后回车即可；借出与归还都按「证号 + ISBN」定位记录。" />
      </el-form>
      <template #footer>
        <el-button @click="scanOpen = false">取消</el-button>
        <el-button type="warning" :loading="scanLoading" @click="submitScan('return')">扫码归还</el-button>
        <el-button type="primary" :loading="scanLoading" @click="submitScan('borrow')">扫码借出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Borrow">
import { onActivated } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listBorrow, getBorrow, delBorrow, addBorrow, updateBorrow, listCrossReturn } from "@/api/book/borrow"
import { listReader } from "@/api/book/reader"
import { listInfo } from "@/api/book/info"
import request from '@/utils/request'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const { book_borrow_status } = useDict('book_borrow_status')

const borrowList = ref([])
const readerOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref("")
const readerLoading = ref(false)

const scanOpen = ref(false)
const scanLoading = ref(false)
const scanForm = ref({ cardNo: '', isbn: '' })

function openScan() {
  scanForm.value = { cardNo: '', isbn: '' }
  scanOpen.value = true
}

function submitScan(mode) {
  if (!scanForm.value.cardNo || !scanForm.value.isbn) {
    proxy.$modal.msgWarning('请先填写借书证号和图书 ISBN')
    return
  }
  scanLoading.value = true
  const url = mode === 'borrow' ? '/book/borrow/scan' : '/book/borrow/crossReturn'
  request({ url, method: 'post', data: { ...scanForm.value } }).then(res => {
    proxy.$modal.msgSuccess(res.msg || (mode === 'borrow' ? '借阅成功' : '归还成功'))
    scanOpen.value = false
    getList()
  }).finally(() => {
    scanLoading.value = false
  })
}

const bookDialogOpen = ref(false)
const bookList = ref([])
const bookTotal = ref(0)
const bookQuery = ref({
  pageNum: 1,
  pageSize: 10,
  bookName: undefined,
  status: '0'
})

// ================= 跨馆还书工作台相关 =================
const crossOpen = ref(false)
const crossLoading = ref(false)
const crossList = ref([])
const crossTotal = ref(0)
const crossQuery = ref({ pageNum: 1, pageSize: 10, cardNo: '', bookName: '' })

function openCrossReturn() {
  crossOpen.value = true
  getCrossList()
}

function getCrossList() {
  crossLoading.value = true
  listCrossReturn(crossQuery.value).then(res => {
    crossList.value = res.rows || []
    crossTotal.value = res.total || 0
  }).catch(err => {
    console.error("查询跨馆列表失败", err)
  }).finally(() => {
    crossLoading.value = false
  })
}

function submitCrossReturn(row) {
  const isCrossDept = row.isLocal === 0
  const msg = isCrossDept
    ? `《${row.bookName}》属于外馆，确认代还并自动调拨至本馆吗？`
    : `确认办理《${row.bookName}》的还书吗？`

  proxy.$modal.confirm(msg).then(() => {
    return request({
      url: '/book/borrow/crossReturn',
      method: 'post',
      data: { cardNo: row.cardNo, isbn: row.isbn }
    })
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || "代还成功")
    getCrossList()
    getList()
  }).catch(() => {})
}
// ======================================================

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined,
  },
  rules: {
    readerId: [{ required: true, message: "请选择读者", trigger: "change" }],
    bookId: [{ required: true, message: "请选择图书", trigger: "change" }],
    borrowTime: [{ required: true, message: "请选择借出时间", trigger: "change" }],
    dueTime: [
      { required: true, message: "应还时间不能为空", trigger: "blur" },
      {
        validator: (rule, value, callback) => {
          const borrowVal = data.form.borrowTime
          if (!borrowVal || !value) return callback()
          const bTime = new Date(borrowVal).getTime()
          const dTime = new Date(value).getTime()
          if (dTime <= bTime) {
            callback(new Error('应还时间必须晚于借出时间'))
          } else {
            callback()
          }
        },
        trigger: ['change', 'blur']
      }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getReaderOptions() {
  readerLoading.value = true
  listReader({ pageNum: 1, pageSize: 1000 }).then(res => {
    readerOptions.value = res.rows
  }).finally(() => {
    readerLoading.value = false
  })
}

function openBookDialog() {
  bookDialogOpen.value = true
  searchBooks()
}

function searchBooks() {
  listInfo(bookQuery.value).then(res => {
    bookList.value = res.rows
    bookTotal.value = res.total
  })
}

function handleBookSelect(row) {
  if (row.availableStock <= 0 || row.status !== '0') {
    proxy.$modal.msgWarning("该图书已下架或库存不足，无法借阅")
    return
  }
  form.value.bookId = row.id
  form.value.bookName = row.bookName
  bookDialogOpen.value = false
}

function getList() {
  loading.value = true
  listBorrow(queryParams.value).then(response => {
    borrowList.value = response.rows
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
    id: null, readerId: null, bookId: null, bookName: null,
    borrowTime: null, dueTime: null, status: "0", remark: null
  }
  proxy.resetForm("borrowRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "登记借阅"

  if (readerOptions.value.length === 0) {
    getReaderOptions()
  }
  // 默认借出时间=当前时间
  const nowDate = new Date()
  form.value.borrowTime = proxy.parseTime(nowDate, '{y}-{m}-{d} {h}:{i}:{s}')
  // 默认应还时间+30天
  const due = new Date()
  due.setDate(due.getDate() + 30);
  form.value.dueTime = proxy.parseTime(due, '{y}-{m}-{d} {h}:{i}:{s}')
}

function handleUpdate(row) {
  reset()
  getBorrow(row.id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改借阅记录"
  })
}

function submitForm() {
  proxy.$refs["borrowRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateBorrow(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBorrow(form.value).then(() => {
          proxy.$modal.msgSuccess("借阅成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleReturn(row) {
  proxy.$modal.confirm('确认读者已经归还《' + (row.bookName || '该书') + '》吗？').then(function() {
    const data = { id: row.id, status: '1' };
    return updateBorrow(data);
  }).then(() => {
    proxy.$modal.msgSuccess("归还成功");
    getList();
  }).catch(() => {})
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除记录编号为"' + _ids + '"的数据项？').then(function() {
    return delBorrow(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 拦截从首页传来的参数（借阅中/逾期卡片跳转），读取一次后立刻清除
function applyRouteQuery() {
  const status = route.query.status
  if (status !== undefined && status !== null && status !== '') {
    queryParams.value.status = String(status)
    queryParams.value.pageNum = 1
    router.replace({ path: route.path })
  }
}

applyRouteQuery()
getList() // 初始化加载

onActivated(() => {
  // 页面被 keep-alive 缓存时 setup 不会重跑，必须在 activated 里重新读取跳转参数
  applyRouteQuery()
  getList() // 切换页签回来时刷新
})
</script>
