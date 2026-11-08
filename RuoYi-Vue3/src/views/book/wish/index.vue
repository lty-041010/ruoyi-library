<template>
  <div class="app-container wish-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="72px">
      <el-form-item label="书名" prop="title">
        <el-input v-model="queryParams.title" placeholder="搜索留言书名" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="书名" min-width="180">
        <template #default="scope">
          <div class="wish-title">{{ scope.row.title }}</div>
          <div class="wish-sub">{{ scope.row.author || '作者未填' }}<span v-if="scope.row.isbn"> · {{ scope.row.isbn }}</span></div>
        </template>
      </el-table-column>
      <el-table-column label="读者" width="120">
        <template #default="scope">
          <div>{{ scope.row.readerName || '—' }}</div>
          <div class="wish-sub">{{ scope.row.cardNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="所属分馆" prop="deptName" width="110" />
      <el-table-column label="想读理由" prop="reason" min-width="180" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.reason || '—' }}</template>
      </el-table-column>
      <el-table-column label="提交时间" width="160">
        <template #default="scope">{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)" effect="light">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="馆员回复" prop="reply" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === '0'" v-hasPermi="['book:wish:approve']" link type="primary" @click="openDialog(scope.row, 'approve')">采纳</el-button>
          <el-button v-if="scope.row.status === '0'" v-hasPermi="['book:wish:reject']" link type="danger" @click="openDialog(scope.row, 'reject')">驳回</el-button>
          <el-button v-if="scope.row.status === '1'" v-hasPermi="['book:wish:approve']" link type="success" @click="markOnShelf(scope.row)">标记已上架</el-button>
          <span v-if="scope.row.status === '2' || scope.row.status === '3'" class="done-text">已处理</span>
        </template>
      </el-table-column>
      <template #empty>
        <div class="empty-box">
          <div class="empty-title">暂无购书留言</div>
          <div class="empty-tip">读者在门户「想读的书」提交留言后，会出现在这里。</div>
        </div>
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="dialogVisible" :title="dialogType === 'approve' ? '采纳留言' : '驳回留言'" width="480px">
      <p class="dialog-book">《{{ current.title }}》 · {{ current.readerName || '读者' }}</p>
      <el-input
        v-model="replyInput"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        :placeholder="dialogType === 'approve' ? '给读者的回复（选填）' : '请说明驳回原因'"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDialog">{{ dialogType === 'approve' ? '确认采纳' : '确认驳回' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BookWish">
import { ref, reactive, getCurrentInstance, onActivated } from 'vue'
import { listWish, approveWish, rejectWish, onShelfWish } from '@/api/book/wish'
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogType = ref('approve')
const current = ref({})
const replyInput = ref('')
const submitting = ref(false)

const statusOptions = [
  { value: '0', label: '待审核' },
  { value: '1', label: '已采纳 · 采购中' },
  { value: '3', label: '已上架' },
  { value: '2', label: '已驳回 / 已取消' }
]

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  status: undefined
})

function statusText(s) {
  return { '0': '待审核', '1': '采购中', '2': '已驳回', '3': '已上架' }[s] || s
}
function statusTag(s) {
  return { '0': 'warning', '1': 'primary', '2': 'info', '3': 'success' }[s] || 'info'
}

function getList() {
  loading.value = true
  listWish(queryParams).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryParams.title = undefined; queryParams.status = undefined; handleQuery() }

function openDialog(row, type) {
  current.value = row
  dialogType.value = type
  replyInput.value = type === 'approve' ? '已采纳，图书馆将安排采购，上架后会在这里回复你。' : ''
  dialogVisible.value = true
}

function submitDialog() {
  if (dialogType.value === 'reject' && !replyInput.value.trim()) {
    proxy.$modal.msgWarning('驳回时请填写原因')
    return
  }
  submitting.value = true
  const call = dialogType.value === 'approve'
    ? approveWish(current.value.id, replyInput.value)
    : rejectWish(current.value.id, replyInput.value)
  call.then(res => {
    proxy.$modal.msgSuccess(res.msg || '处理成功')
    dialogVisible.value = false
    getList()
  }).catch(() => {}).finally(() => { submitting.value = false })
}

function markOnShelf(row) {
  proxy.$modal.confirm(`确认《${row.title}》已经采购并上架？标记后读者会看到「已上架 · 可借阅」。`).then(() => {
    return onShelfWish(row.id)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '已标记上架')
    getList()
  }).catch(() => {})
}

getList()
onActivated(() => getList())
</script>

<style scoped>
.wish-page { background: #fbfaf7; }
.wish-title { color: #1f2a26; font-size: 14px; }
.wish-sub { color: #9aa39e; font-size: 12px; margin-top: 2px; }
.done-text { color: #9aa39e; font-size: 12px; }
.dialog-book { margin: 0 0 12px; color: #7b857f; font-size: 13px; }
.empty-box { padding: 28px 0; color: #7b857f; }
.empty-title { font-size: 15px; color: #1f2a26; margin-bottom: 6px; }
.empty-tip { font-size: 12px; }
</style>
