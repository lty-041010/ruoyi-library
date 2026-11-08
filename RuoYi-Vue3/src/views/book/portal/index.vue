<template>
  <div class="reader-portal">
    <!-- 顶部欢迎区 -->
    <section class="hero">
      <div class="hero-left">
        <p class="eyebrow">MY LIBRARY</p>
        <h1>你好，{{ displayName }}</h1>
        <p class="hero-sub">
          借书证 <b>{{ stats.myCardNo || '—' }}</b>
          <span class="dot">·</span>
          单次最多可借 {{ stats.myMaxBorrowNum || 5 }} 本
          <span class="dot">·</span>
          可自助续借 2 次
        </p>
      </div>
      <button class="notice-bell" type="button" title="消息提醒" @click="openNotices">
        <el-icon><Bell /></el-icon>
        <span v-if="unread > 0" class="notice-badge">{{ unread > 99 ? '99+' : unread }}</span>
      </button>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="num">{{ activeBorrows.length }}</span>
          <span class="label">在借中</span>
        </div>
        <div class="hero-stat">
          <span class="num">{{ Math.max((stats.myMaxBorrowNum || 5) - activeBorrows.length, 0) }}</span>
          <span class="label">还可借</span>
        </div>
        <div class="hero-stat">
          <span class="num">{{ reservations.length }}</span>
          <span class="label">预约中</span>
        </div>
      </div>
    </section>

    <el-tabs v-model="tab" class="portal-tabs">
      <!-- ================= 找书 ================= -->
      <el-tab-pane name="search">
        <template #label><span class="tab-label">找一本书</span></template>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜书名、作者或 ISBN"
            clearable
            size="large"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="categoryId" placeholder="全部分类" clearable size="large" class="category-select" @change="handleSearch">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
          </el-select>
          <el-button type="primary" size="large" class="search-btn" @click="handleSearch">搜索</el-button>
        </div>

        <div class="list-head">
          <div class="list-title">
            馆藏图书
            <span class="count">共 {{ total }} 种</span>
          </div>
          <div class="list-hint">点击封面查看详情，已借完的书可以预约</div>
        </div>

        <div v-loading="bookLoading" class="book-grid">
          <div v-for="book in books" :key="book.id" class="book-card" @click="openDetail(book)">
            <div class="cover" :style="coverStyle(book)">
              <img v-if="book.coverImage" :src="resolveCover(book.coverImage)" alt="" />
              <span v-else class="cover-text">{{ coverText(book) }}</span>
            </div>
            <div class="book-info">
              <h3 class="book-name" :title="book.bookName">{{ book.bookName }}</h3>
              <p class="book-meta">{{ book.author || '佚名' }}<span v-if="book.publisher"> · {{ book.publisher }}</span></p>
              <div class="book-foot">
                <span class="stock" :class="{ out: !book.availableStock }">
                  {{ book.availableStock > 0 ? `可借 ${book.availableStock} 本` : '已借完' }}
                </span>
                <span class="cat">{{ categoryName(book.categoryId) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-if="!bookLoading && !books.length" description="没有找到匹配的图书" :image-size="90" class="grid-empty" />
        </div>

        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="bookQuery.pageNum"
          v-model:limit="bookQuery.pageSize"
          layout="prev, pager, next, total"
          @pagination="loadBooks"
        />
      </el-tab-pane>

      <!-- ================= 我的借阅 ================= -->
      <el-tab-pane name="borrows">
        <template #label>
          <span class="tab-label">我的借阅<i v-if="activeBorrows.length" class="tab-badge">{{ activeBorrows.length }}</i></span>
        </template>
        <div class="section">
          <div class="section-head">
            <h3>正在借阅</h3>
            <span class="section-hint">到期前可自助续借，每本最多续借 2 次</span>
          </div>
          <div v-if="activeBorrows.length" class="borrow-list">
            <div v-for="item in activeBorrows" :key="item.id" class="borrow-card">
              <div class="mini-cover" :style="coverStyle(item)">{{ coverText(item) }}</div>
              <div class="borrow-main">
                <h4>{{ item.bookName || ('未知图书#' + item.bookId) }}</h4>
                <p>借出 {{ shortDate(item.borrowTime) }}<span class="dot">·</span>应还 {{ shortDate(item.dueTime) }}</p>
                <p v-if="item.renewCount" class="renewed">已续借 {{ item.renewCount }} 次</p>
                <p v-if="item.overdueDays > 0" class="fine-line">
                  已逾期 {{ item.overdueDays }} 天 · 应缴滞纳金 ¥{{ Number(item.fineAmount || 0).toFixed(2) }}
                </p>
              </div>
              <div class="borrow-side">
                <span class="due-chip" :class="dueClass(item)">{{ dueText(item) }}</span>
                <el-button size="small" plain :disabled="!canRenew(item)" @click="handleRenew(item)">续借</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="当前没有在借图书" :image-size="80" />

          <div class="section-head history-head">
            <h3>借阅历史</h3>
            <span class="section-hint">已归还 {{ history.length }} 本</span>
          </div>
          <div v-if="history.length" class="borrow-list history">
            <div v-for="item in history" :key="item.id" class="borrow-card">
              <div class="mini-cover gray" :style="coverStyle(item)">{{ coverText(item) }}</div>
              <div class="borrow-main">
                <h4>{{ item.bookName || ('未知图书#' + item.bookId) }}</h4>
                <p>借出 {{ shortDate(item.borrowTime) }}<span class="dot">·</span>归还 {{ shortDate(item.returnTime) }}</p>
              </div>
              <span class="status-chip">已归还</span>
            </div>
          </div>
          <el-empty v-else description="还没有借阅记录" :image-size="70" />
        </div>
      </el-tab-pane>

      <!-- ================= 我的预约 ================= -->
      <el-tab-pane name="reservations">
        <template #label>
          <span class="tab-label">我的预约<i v-if="reservations.length" class="tab-badge">{{ reservations.length }}</i></span>
        </template>
        <div class="section">
          <div class="section-head">
            <h3>预约中的图书</h3>
            <span class="section-hint">图书归还入库后，到馆即可借阅</span>
          </div>
          <div v-if="reservations.length" class="reserve-list">
            <div v-for="item in reservations" :key="item.id" class="reserve-row">
              <div class="mini-cover" :style="coverStyle({ bookName: item.bookName })">{{ coverText({ bookName: item.bookName }) }}</div>
              <div class="borrow-main">
                <h4>{{ item.bookName }}</h4>
                <p>{{ item.author || '佚名' }}<span class="dot">·</span>预约于 {{ shortDate(item.createTime) }}</p>
              </div>
              <span class="status-chip waiting">等待入库</span>
              <el-button size="small" text type="danger" @click="handleCancelReserve(item)">取消预约</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无预约，去「找一本书」看看借完的书吧" :image-size="80" />
        </div>
      </el-tab-pane>

      <!-- ================= 想读的书 ================= -->
      <el-tab-pane name="wishes">
        <template #label>
          <span class="tab-label">想读的书<i v-if="wishes.length" class="tab-badge">{{ wishes.length }}</i></span>
        </template>
        <div class="section">
          <div class="section-head">
            <h3>馆里没有？留言让图书馆采购</h3>
            <span class="section-hint">馆员审核采纳后会采购，上架后这里会提示你</span>
          </div>

          <el-form :model="wishForm" label-width="0" class="wish-form">
            <el-input v-model="wishForm.title" maxlength="200" placeholder="想读的书名（必填）" class="wish-title" />
            <el-input v-model="wishForm.author" maxlength="100" placeholder="作者（选填）" class="wish-input" />
            <el-input v-model="wishForm.isbn" maxlength="50" placeholder="ISBN（选填）" class="wish-input" />
            <el-input
              v-model="wishForm.reason"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              placeholder="为什么想读这本书？（选填，能帮馆员判断采购价值）"
              class="wish-reason"
            />
            <el-button type="primary" :loading="wishSubmitting" @click="handleSubmitWish">提交留言</el-button>
          </el-form>

          <div v-if="wishes.length" class="wish-list">
            <div v-for="item in wishes" :key="item.id" class="wish-row">
              <div class="wish-main">
                <h4>{{ item.title }}<span v-if="item.author" class="wish-author">{{ item.author }}</span></h4>
                <p>提交于 {{ shortDate(item.createTime) }}</p>
                <p v-if="item.reply" class="wish-reply">馆员回复：{{ item.reply }}</p>
              </div>
              <span class="status-chip" :class="wishClass(item.status)">{{ wishText(item.status) }}</span>
              <el-button v-if="item.status === '0'" size="small" text type="danger" @click="handleCancelWish(item)">取消留言</el-button>
            </div>
          </div>
          <el-empty v-else description="还没有留言，写下你想读的书吧" :image-size="80" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 图书详情 -->
    <el-dialog v-model="detailVisible" :width="detailWidth" class="book-dialog" :show-close="true">
      <div v-if="detail" class="detail">
        <div class="detail-top">
          <div class="detail-cover" :style="detail.coverImage ? {} : coverStyle(detail)">
            <img v-if="detail.coverImage" :src="resolveCover(detail.coverImage)" alt="" />
            <template v-else>{{ coverText(detail) }}</template>
          </div>
          <div class="detail-head">
            <h2>{{ detail.bookName }}</h2>
            <p>{{ detail.author || '佚名' }} · {{ detail.publisher || '未知出版社' }}</p>
            <div class="detail-tags">
              <span class="tag">{{ categoryName(detail.categoryId) }}</span>
              <span class="tag light">ISBN {{ detail.isbn }}</span>
            </div>
          </div>
        </div>
        <p class="detail-desc">{{ detail.description || '暂无简介。' }}</p>
        <div class="detail-stock">
          <span>馆藏 {{ detail.totalStock }} 本</span>
          <span>可借 {{ detail.availableStock }} 本</span>
          <span v-if="detail.deptName">{{ detail.deptName }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail && detail.availableStock <= 0"
          type="primary"
          :disabled="isReserved(detail.id)"
          @click="handleReserve(detail)"
        >
          {{ isReserved(detail.id) ? '已预约' : '预约这本书' }}
        </el-button>
        <span v-else class="detail-tip">该书当前可借，请到馆办理借阅</span>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeVisible" title="消息提醒" :width="noticeWidth">
      <div class="notice-toolbar">
        <span class="muted">共 {{ notices.length }} 条，{{ unread }} 条未读</span>
        <el-button link type="primary" :disabled="unread === 0" @click="handleReadAll">全部已读</el-button>
      </div>
      <div v-if="notices.length" class="notice-list">
        <div v-for="n in notices" :key="n.id" class="notice-row" :class="{ unread: n.readFlag === '0' }" @click="handleRead(n)">
          <span class="notice-dot" v-if="n.readFlag === '0'" />
          <div class="notice-body">
            <h4>{{ n.title }}</h4>
            <p>{{ n.content }}</p>
            <span class="notice-time">{{ shortDate(n.createTime) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂时没有新消息" :image-size="70" />
    </el-dialog>
  </div>
</template>

<script setup name="ReaderPortal">
import { ref, reactive, computed, onActivated } from 'vue'
import useUserStore from '@/store/modules/user'
import { getCurrentInstance } from 'vue'
import {
  listPortalBooks,
  listPortalCategories,
  listMyBorrows,
  renewBorrow,
  listMyReservations,
  addReservation,
  cancelReservation,
  getPortalStats
} from '@/api/book/portal'
import { listMyWishes, addWish, cancelWish } from '@/api/book/wish'
import { getMyNotices, getUnreadCount, readNotice, readAllNotices } from '@/api/book/notice'

const resolveCover = (path) => {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  return (import.meta.env.VITE_APP_BASE_API || '') + path
}

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const tab = ref('search')
const keyword = ref('')
const categoryId = ref(undefined)
const categories = ref([])
const books = ref([])
const total = ref(0)
const bookLoading = ref(false)
const stats = ref({})
const borrows = ref([])
const reservations = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const renewingId = ref(null)
const wishes = ref([])
const wishSubmitting = ref(false)
const wishForm = reactive({ title: '', author: '', isbn: '', reason: '' })
const unread = ref(0)
const notices = ref([])
const noticeVisible = ref(false)
const noticeWidth = window.innerWidth < 768 ? '92%' : '520px'
const detailWidth = window.innerWidth < 768 ? '92%' : '560px'

const bookQuery = reactive({ pageNum: 1, pageSize: 12 })
const displayName = computed(() => userStore.nickName || userStore.name || '读者')
const deptName = computed(() => userStore.deptName || '')

const activeBorrows = computed(() =>
  borrows.value.filter(i => i.status === '0' || i.status === '2' || (i.status === '0' && isPast(i.dueTime)))
)
const history = computed(() => borrows.value.filter(i => i.status === '1'))

function isPast(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr.replace(/-/g, '/')).getTime() < Date.now()
}

function shortDate(v) {
  return v ? String(v).slice(0, 10) : '—'
}

function dueDays(item) {
  if (!item.dueTime) return 0
  const due = new Date(String(item.dueTime).replace(/-/g, '/')).getTime()
  return Math.ceil((due - Date.now()) / 86400000)
}

function dueText(item) {
  const days = dueDays(item)
  if (days < 0) return `已逾期 ${Math.abs(days)} 天`
  if (days === 0) return '今天到期'
  return `剩余 ${days} 天`
}

function dueClass(item) {
  const days = dueDays(item)
  if (days < 0) return 'danger'
  if (days <= 7) return 'warn'
  return 'ok'
}

function canRenew(item) {
  const days = dueDays(item)
  return item.status === '0' && days >= 0 && (item.renewCount || 0) < 2
}

function categoryName(id) {
  const hit = categories.value.find(c => String(c.id) === String(id))
  return hit ? hit.categoryName : '未分类'
}

function coverText(book) {
  const name = (book && book.bookName) || '书'
  return name.slice(0, 2)
}

const gradients = [
  'linear-gradient(135deg,#2f6b5f,#5f9c8c)',
  'linear-gradient(135deg,#8a5a44,#c08a6e)',
  'linear-gradient(135deg,#3c5a78,#6a8fae)',
  'linear-gradient(135deg,#6b5b3e,#a68a5b)',
  'linear-gradient(135deg,#5a4a6b,#8c78a8)',
  'linear-gradient(135deg,#4a6b52,#7fa382)'
]
function coverStyle(book) {
  const seed = ((book && (book.id || String(book.bookName || '').length)) || 1) % gradients.length
  return { background: gradients[seed] }
}

function loadBooks() {
  bookLoading.value = true
  const kw = (keyword.value || '').trim()
  const params = {
    pageNum: bookQuery.pageNum,
    pageSize: bookQuery.pageSize,
    status: '0'
  }
  if (categoryId.value) params.categoryId = categoryId.value
  if (kw) {
    if (/^[0-9-]{6,}$/.test(kw)) params.isbn = kw
    else params.bookName = kw
  }
  listPortalBooks(params).then(res => {
    books.value = res.rows || []
    total.value = res.total || 0
    bookLoading.value = false
  }).catch(() => { bookLoading.value = false })
}

function handleSearch() {
  bookQuery.pageNum = 1
  loadBooks()
}

function openDetail(book) {
  detail.value = book
  detailVisible.value = true
}

function isReserved(bookId) {
  return reservations.value.some(r => String(r.bookId) === String(bookId) && r.status === '0')
}

function loadBorrows() {
  listMyBorrows({ pageNum: 1, pageSize: 100 }).then(res => {
    borrows.value = res.rows || []
  }).catch(() => {})
}

function loadReservations() {
  listMyReservations().then(res => {
    reservations.value = (res.data || []).filter(r => r.status === '0')
  }).catch(() => {})
}

function loadStats() {
  getPortalStats().then(res => {
    if (res.code === 200) stats.value = res.data
  }).catch(() => {})
}

function wishText(status) {
  return { '0': '待审核', '1': '已采纳 · 采购中', '2': '已驳回', '3': '已上架 · 可借阅' }[status] || '待审核'
}
function wishClass(status) {
  return { '0': 'waiting', '1': 'ok', '2': 'muted', '3': 'ok' }[status] || 'waiting'
}

function loadWishes() {
  listMyWishes().then(res => {
    wishes.value = res.data || []
  }).catch(() => {})
}

function loadUnread() {
  getUnreadCount().then(res => {
    unread.value = res.data || 0
  }).catch(() => {})
}

function openNotices() {
  getMyNotices().then(res => {
    notices.value = res.data || []
    noticeVisible.value = true
  }).catch(() => {})
}

function handleRead(n) {
  if (n.readFlag === '0') {
    readNotice(n.id).then(() => {
      n.readFlag = '1'
      unread.value = Math.max(unread.value - 1, 0)
    }).catch(() => {})
  }
}

function handleReadAll() {
  readAllNotices().then(() => {
    notices.value.forEach(n => { n.readFlag = '1' })
    unread.value = 0
  }).catch(() => {})
}

function handleSubmitWish() {
  if (!wishForm.title.trim()) {
    proxy.$modal.msgWarning('请先填写想读的书名')
    return
  }
  wishSubmitting.value = true
  addWish({
    title: wishForm.title.trim(),
    author: wishForm.author || undefined,
    isbn: wishForm.isbn || undefined,
    reason: wishForm.reason || undefined
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '留言已提交')
    wishForm.title = ''
    wishForm.author = ''
    wishForm.isbn = ''
    wishForm.reason = ''
    loadWishes()
  }).catch(() => {}).finally(() => { wishSubmitting.value = false })
}

function handleCancelWish(item) {
  proxy.$modal.confirm(`确认取消《${item.title}》的留言吗？`).then(() => {
    return cancelWish(item.id)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '已取消留言')
    loadWishes()
  }).catch(() => {})
}

function loadAll() {
  loadStats()
  loadBorrows()
  loadReservations()
  loadWishes()
  loadUnread()
  loadBooks()
}

function handleRenew(item) {
  proxy.$modal.confirm(`确认为《${item.bookName || '该书'}》续借 30 天吗？`).then(() => {
    renewingId.value = item.id
    return renewBorrow(item.id)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '续借成功')
    loadBorrows()
    loadStats()
  }).catch(() => {}).finally(() => { renewingId.value = null })
}

function handleReserve(book) {
  addReservation(book.id).then(res => {
    proxy.$modal.msgSuccess(res.msg || '预约成功')
    detailVisible.value = false
    loadReservations()
  }).catch(() => {})
}

function handleCancelReserve(item) {
  proxy.$modal.confirm(`确认取消《${item.bookName}》的预约吗？`).then(() => {
    return cancelReservation(item.id)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || '已取消预约')
    loadReservations()
  }).catch(() => {})
}

onActivated(() => {
  loadAll()
})

listPortalCategories().then(res => { categories.value = res.rows || res.data || [] }).catch(() => {})
loadAll()
</script>

<style scoped>
.reader-portal {
  --ink: #1f2a26;
  --muted: #7b857f;
  --brand: #2f6b5f;
  --brand-soft: #e7f0ec;
  --line: #e9e6df;
  --paper: #fbfaf7;
  min-height: calc(100vh - 84px);
  padding: 4px 2px 24px;
  color: var(--ink);
}

.hero {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 34px;
  border-radius: 18px;
  background: linear-gradient(120deg, #1f4d43 0%, #2f6b5f 55%, #4d8a78 100%);
  color: #fff;
  box-shadow: 0 16px 34px rgba(31, 77, 67, 0.18);
}
.hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -90px;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}
.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 3px;
  opacity: 0.75;
}
.hero h1 {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 600;
  letter-spacing: 1px;
}
.hero-sub {
  margin: 0;
  font-size: 13px;
  opacity: 0.85;
}
.hero-sub b { font-weight: 600; }
.dot { margin: 0 8px; opacity: 0.5; }
.hero-stats {
  display: flex;
  gap: 18px;
  position: relative;
  z-index: 1;
}
.hero-stat {
  min-width: 78px;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  text-align: center;
  backdrop-filter: blur(4px);
}
.hero-stat .num { display: block; font-size: 24px; font-weight: 600; }
.hero-stat .label { font-size: 12px; opacity: 0.8; }

.portal-tabs { margin-top: 18px; }
.tab-label { font-size: 15px; }
.tab-badge {
  display: inline-block;
  margin-left: 4px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--brand);
  color: #fff;
  font-size: 11px;
  font-style: normal;
  text-align: center;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin: 8px 0 20px;
}
.search-bar :deep(.el-input__wrapper) { border-radius: 12px; }
.category-select { width: 180px; }
.category-select :deep(.el-select__wrapper) { border-radius: 12px; }
.search-btn {
  border-radius: 12px;
  background: var(--brand);
  border-color: var(--brand);
  padding: 0 28px;
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 14px;
}
.list-title { font-size: 16px; font-weight: 600; }
.list-title .count { margin-left: 8px; font-size: 12px; font-weight: normal; color: var(--muted); }
.list-hint { font-size: 12px; color: var(--muted); }

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(188px, 1fr));
  gap: 18px;
  min-height: 220px;
}
.book-card {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 26px rgba(31, 42, 38, 0.1);
}
.cover {
  height: 150px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #fff;
  font-size: 30px;
  letter-spacing: 4px;
  font-family: 'Songti SC', 'STSong', 'SimSun', serif;
}
.cover img { width: 100%; height: 100%; object-fit: cover; }
.book-info { padding-top: 10px; }
.book-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-meta {
  margin: 5px 0 10px;
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-foot { display: flex; justify-content: space-between; align-items: center; font-size: 12px; }
.stock { color: var(--brand); }
.stock.out { color: #c06a4d; }
.cat { color: var(--muted); }
.grid-empty { grid-column: 1 / -1; }

.section { padding: 6px 0 10px; }
.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 6px 0 14px;
}
.section-head h3 { margin: 0; font-size: 16px; }
.section-hint { font-size: 12px; color: var(--muted); }
.history-head { margin-top: 28px; }

.borrow-list { display: flex; flex-direction: column; gap: 12px; }
.borrow-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
}
.mini-cover {
  width: 46px;
  height: 62px;
  flex: none;
  border-radius: 6px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Songti SC', 'STSong', 'SimSun', serif;
  font-size: 13px;
  letter-spacing: 1px;
}
.mini-cover.gray { filter: grayscale(0.55); opacity: 0.8; }
.borrow-main { flex: 1; min-width: 0; }
.borrow-main h4 { margin: 0 0 5px; font-size: 15px; font-weight: 600; }
.borrow-main p { margin: 0; font-size: 12px; color: var(--muted); }
.borrow-main .renewed { margin-top: 3px; }
.borrow-side { display: flex; align-items: center; gap: 10px; }
.due-chip {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}
.due-chip.ok { background: var(--brand-soft); color: var(--brand); }
.due-chip.warn { background: #fbf1e2; color: #a9762f; }
.due-chip.danger { background: #fbe9e6; color: #b4472f; }
.status-chip {
  padding: 4px 10px;
  border-radius: 999px;
  background: #f1f1ef;
  color: #8a8f8c;
  font-size: 12px;
}
.status-chip.waiting { background: var(--brand-soft); color: var(--brand); }

.reserve-list { display: flex; flex-direction: column; gap: 12px; }
.reserve-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  border: 1px dashed #d8d3c8;
  border-radius: 14px;
  background: var(--paper);
}

.detail-top { display: flex; gap: 20px; }
.detail-cover {
  width: 108px;
  height: 150px;
  flex: none;
  border-radius: 10px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  letter-spacing: 4px;
  font-family: 'Songti SC', 'STSong', 'SimSun', serif;
  overflow: hidden;
}
.detail-cover img,
.cover img { width: 100%; height: 100%; object-fit: cover; display: block; }
.fine-line { color: #c0392b; font-size: 12px; margin-top: 4px; }
.detail-head h2 { margin: 4px 0 8px; font-size: 22px; }
.detail-head p { margin: 0 0 12px; color: var(--muted); font-size: 13px; }
.detail-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.tag {
  padding: 3px 10px;
  border-radius: 6px;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 12px;
}
.tag.light { background: #f3f2ee; color: #6f7a74; }
.detail-desc {
  margin: 20px 0 16px;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--paper);
  color: #4a544f;
  font-size: 13px;
  line-height: 1.8;
  max-height: 150px;
  overflow: auto;
}
.detail-stock { display: flex; gap: 18px; font-size: 13px; color: var(--muted); }
.detail-tip { font-size: 13px; color: var(--muted); }

.status-chip.ok { background: var(--brand-soft); color: var(--brand); }
.status-chip.muted { background: #f1f1ef; color: #8a8f8c; }

.wish-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 18px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
}
.wish-title { width: 260px; }
.wish-input { width: 180px; }
.wish-reason { width: 100%; }
.wish-list { display: flex; flex-direction: column; gap: 12px; }
.wish-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 14px 18px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
}
.wish-main { flex: 1; }
.wish-main h4 { margin: 0 0 6px; font-size: 15px; }
.wish-author { margin-left: 8px; color: var(--muted); font-size: 12px; font-weight: normal; }
.wish-main p { margin: 2px 0; color: var(--muted); font-size: 12px; }
.wish-reply { color: #4a544f !important; background: var(--paper); padding: 6px 10px; border-radius: 8px; }

.notice-bell {
  position: absolute;
  top: 18px;
  right: 22px;
  width: 40px;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: background 0.2s;
}
.notice-bell:hover { background: rgba(255, 255, 255, 0.28); }
.notice-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  border-radius: 999px;
  background: #fff;
  color: #2f6b5f;
  font-size: 11px;
  line-height: 18px;
  font-weight: 600;
}
.notice-toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; font-size: 12px; }
.notice-list { max-height: 420px; overflow-y: auto; }
.notice-row {
  display: flex;
  gap: 10px;
  padding: 12px 8px;
  border-bottom: 1px solid #f1efe9;
  cursor: pointer;
}
.notice-row:hover { background: #fbfaf7; }
.notice-row.unread { background: #f4f9f6; }
.notice-dot { width: 7px; height: 7px; flex: none; margin-top: 6px; border-radius: 50%; background: #2f6b5f; }
.notice-body h4 { margin: 0 0 4px; font-size: 14px; color: #1f2a26; }
.notice-body p { margin: 0 0 4px; font-size: 12px; color: #6f7a74; line-height: 1.6; }
.notice-time { font-size: 11px; color: #a3aaa6; }

@media (max-width: 768px) {
  .hero { flex-direction: column; align-items: flex-start; gap: 18px; padding: 22px 18px; }
  .hero-stats { width: 100%; justify-content: space-between; gap: 10px; }
  .hero-stat .num { font-size: 22px; }
  .notice-bell { top: 12px; right: 12px; width: 34px; height: 34px; font-size: 15px; }
  .search-bar { flex-direction: column; align-items: stretch; }
  .search-bar .el-input, .category-select, .search-btn { width: 100% !important; }
  .book-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
  .book-card { padding: 14px; }
  .wish-form { flex-direction: column; }
  .wish-title, .wish-input, .wish-reason, .wish-form .el-button { width: 100% !important; }
  .wish-row { flex-wrap: wrap; }
  .reserve-row { flex-wrap: wrap; }
  .portal-tabs :deep(.el-tabs__nav-wrap) { overflow-x: auto; }
  .portal-tabs :deep(.el-tabs__nav) { flex-wrap: nowrap; }
}
</style>
