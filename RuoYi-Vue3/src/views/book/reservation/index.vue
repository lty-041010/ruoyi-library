<template>
  <div class="app-container reservation-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="72px">
      <el-form-item label="书名" prop="bookName">
        <el-input v-model="queryParams.bookName" placeholder="按书名搜索" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="借书证" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="读者借书证号" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="读者" prop="readerName" min-width="110" />
      <el-table-column label="借书证" prop="cardNo" width="110" />
      <el-table-column label="图书" min-width="180">
        <template #default="scope">
          <span class="book-name">{{ scope.row.bookName || '未知图书' }}</span>
          <span class="isbn">{{ scope.row.isbn }}</span>
        </template>
      </el-table-column>
      <el-table-column label="所属分馆" prop="deptName" min-width="120" />
      <el-table-column label="预约时间" width="170">
        <template #default="scope">{{ parseTime(scope.row.reserveTime || scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)" effect="light">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" align="center" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            v-hasPermi="['book:reservation:handle']"
            link
            type="primary"
            @click="handleItem(scope.row)"
          >到书核销</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            v-hasPermi="['book:reservation:cancel']"
            link
            type="danger"
            @click="cancelItem(scope.row)"
          >取消</el-button>
          <span v-if="scope.row.status !== '0'" class="done-text">已处理</span>
        </template>
      </el-table-column>
      <template #empty>
        <div class="empty-box">
          <div class="empty-title">暂无预约记录</div>
          <div class="empty-tip">读者在门户预约无库存图书后，会在这里出现，等待到书核销。</div>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="BookReservation">
import { ref, reactive, getCurrentInstance, onActivated } from 'vue'
import { listReservation, handleReservation, cancelReservationByAdmin } from '@/api/book/reservation'
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)

const statusOptions = [
  { value: '0', label: '等待入库' },
  { value: '3', label: '已完成' },
  { value: '2', label: '已取消' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  bookName: undefined,
  cardNo: undefined,
  status: undefined
})

function statusText(s) {
  return { '0': '等待入库', '2': '已取消', '3': '已完成' }[s] || s || '—'
}
function statusTag(s) {
  return { '0': 'warning', '2': 'info', '3': 'success' }[s] || 'info'
}

function getList() {
  loading.value = true
  listReservation(queryParams).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}
function resetQuery() {
  queryParams.bookName = undefined
  queryParams.cardNo = undefined
  queryParams.status = undefined
  handleQuery()
}

function handleItem(row) {
  proxy.$modal.confirm(`确认「${row.bookName || '该图书'}」已到馆并完成核销？`).then(() => {
    return handleReservation(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess('核销成功，读者可到馆取书')
    getList()
  }).catch(() => {})
}

function cancelItem(row) {
  proxy.$modal.confirm(`确认取消 ${row.readerName || '该读者'} 对「${row.bookName || '该图书'}」的预约？`).then(() => {
    return cancelReservationByAdmin(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess('已取消该预约')
    getList()
  }).catch(() => {})
}

getList()
onActivated(() => getList())
</script>

<style scoped>
.reservation-page { background: #fbfaf7; }
.book-name { color: #1f2a26; }
.isbn { margin-left: 8px; color: #9aa39e; font-size: 12px; }
.done-text { color: #9aa39e; font-size: 12px; }
.empty-box { padding: 28px 0; color: #7b857f; }
.empty-title { font-size: 15px; color: #1f2a26; margin-bottom: 6px; }
.empty-tip { font-size: 12px; }
</style>
