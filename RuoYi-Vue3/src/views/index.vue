<template>
  <div class="app-container">
    <!-- 读者登录：展示读者门户；管理员/馆员：展示统计看板 -->
    <reader-portal v-if="isReader" />
    <div v-else>
    <!-- 顶部四个数据卡片 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <!-- 修改点：点击卡片依然跳转到图书信息页 -->
        <el-card shadow="hover" class="stat-card" @click="goToPage('/book/info')">
          <div class="stat-content">
            <div class="icon-box bg-blue"><el-icon><Reading /></el-icon></div>
            <div class="data-box">
              <div class="title">馆藏总册数</div>
              <div class="value">
                {{ stats.totalCopies || 0 }} <span class="unit">册</span>
              </div>
              <div class="sub-title">共 {{ stats.totalBooks || 0 }} 种书目</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToPage('/book/reader')">
          <div class="stat-content">
            <div class="icon-box bg-green"><el-icon><User /></el-icon></div>
            <div class="data-box">
              <div class="title">读者总数</div>
              <div class="value">{{ stats.totalReaders || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToPage('/book/borrow', { status: '0' })">
          <div class="stat-content">
            <div class="icon-box bg-orange"><el-icon><Document /></el-icon></div>
            <div class="data-box">
              <div class="title">当前在借</div>
              <div class="value">{{ stats.activeBorrows || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="goToPage('/book/borrow', { status: '2' })">
          <div class="stat-content">
            <div class="icon-box bg-red"><el-icon><Warning /></el-icon></div>
            <div class="data-box">
              <div class="title">逾期未还</div>
              <div class="value">{{ stats.overdueBorrows || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" header="各分馆藏书分布">
          <div ref="deptChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" header="图书分类占比">
          <div ref="categoryChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup name="Index">
import { ref, computed, onMounted, onActivated, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/book/dashboard'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import ReaderPortal from '@/views/book/portal/index.vue'

const router = useRouter()
const userStore = useUserStore()
const isReader = computed(() => (userStore.roles || []).includes('book_reader'))
const stats = ref({})
const deptChartRef = ref(null)
const categoryChartRef = ref(null)
let deptChart = null
let categoryChart = null

function goToPage(path, query = {}) {
  router.push({ path, query })
}

/** 获取数据并渲染 */
function initDashboard() {
  if (isReader.value) return
  getDashboardStats().then(res => {
    if (res.code === 200) {
      stats.value = res.data
      nextTick(() => {
        renderDeptChart(res.data.deptStats)
        renderCategoryChart(res.data.categoryStats)
      })
    }
  })
}

/** 渲染各分馆柱状图 */
function renderDeptChart(data) {
  if (!deptChartRef.value) return
  if (!deptChart) deptChart = echarts.init(deptChartRef.value)
  
  // 过滤掉没有书的部门，并按数量降序排序
  const validData = data.filter(item => item.value > 0).sort((a, b) => b.value - a.value)
  const xData = validData.map(item => item.name)
  const yData = validData.map(item => item.value)
  
  deptChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: xData, 
      axisLabel: { interval: 0, rotate: 30, color: '#666' },
      axisLine: { lineStyle: { color: '#ccc' } }
    },
    yAxis: { 
      type: 'value',
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: [{
      data: yData,
      type: 'bar',
      itemStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [4, 4, 0, 0] 
      },
      barMaxWidth: 50
    }]
  }, true)
}

/** 渲染分类饼图 */
function renderCategoryChart(data) {
  if (!categoryChartRef.value) return
  if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value)
  
  // 1. 过滤掉 0 本的分类，并按数量降序排序
  const sortedData = data.filter(item => item.value > 0).sort((a, b) => b.value - a.value);
  
  // 2. 取前 8 名，剩下的合并为“其他分类”
  const topData = sortedData.slice(0, 8);
  const otherValue = sortedData.slice(8).reduce((sum, item) => sum + item.value, 0);
  
  const pieData = topData.map(item => ({ name: item.name, value: item.value }));
  if (otherValue > 0) {
    pieData.push({ name: '其他分类', value: otherValue });
  }
  
  // 3. 渲染图表
  categoryChart.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} 本 ({d}%)'
    },
    legend: { 
      type: 'scroll',       // ★ 开启图例滚动
      orient: 'vertical',   // ★ 垂直排列
      right: '10%',         // ★ 放在右侧
      top: 'center',
      itemWidth: 14,
      itemHeight: 14
    },
    series: [{
      name: '图书分类',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'], // ★ 饼图靠左侧，给右侧图例留出空间
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { 
        label: { show: true, fontSize: 18, fontWeight: 'bold', formatter: '{b}\n{c}本' } 
      },
      labelLine: { show: false },
      data: pieData
    }]
  }, true)
}

// 监听窗口大小变化，自适应图表
function handleResize() {
  if (deptChart) deptChart.resize()
  if (categoryChart) categoryChart.resize()
}
window.addEventListener('resize', handleResize)

onMounted(() => {
  initDashboard()
})

onActivated(() => {
  initDashboard()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
  transition: transform 0.3s;
  cursor: pointer;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}
.stat-content {
  display: flex;
  align-items: center;
}
.icon-box {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
  color: #fff;
  margin-right: 15px;
}
.bg-blue { background: linear-gradient(135deg, #66b1ff, #409EFF); }
.bg-green { background: linear-gradient(135deg, #85ce61, #67C23A); }
.bg-orange { background: linear-gradient(135deg, #e6a23c, #ebb563); }
.bg-red { background: linear-gradient(135deg, #f56c6c, #f89898); }
.data-box .title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}
.data-box .value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
/* 新增单位样式 */
.data-box .value .unit {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}
/* 新增副标题样式 */
.data-box .sub-title {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>