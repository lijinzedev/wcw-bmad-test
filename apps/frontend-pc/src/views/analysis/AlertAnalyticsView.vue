<template>
  <div class="alert-analytics">
    <header class="alert-analytics__header">
      <div>
        <h1>预警分析看板</h1>
        <p class="hint">查看预警趋势、严重级别分布与指标排名，可点击任意图表跳转到预警列表。</p>
      </div>
      <div class="filters">
        <label>
          时间范围
          <select v-model="range" @change="onRangeChange" :disabled="loading">
            <option value="thisWeek">本周</option>
            <option value="thisMonth">本月</option>
            <option value="custom">自定义</option>
          </select>
        </label>
        <label v-if="range === 'custom'">
          起始时间
          <input type="datetime-local" v-model="customFrom" :disabled="loading" />
        </label>
        <label v-if="range === 'custom'">
          结束时间
          <input type="datetime-local" v-model="customTo" :disabled="loading" />
        </label>
        <label>
          粒度
          <select v-model="bucket" @change="loadData" :disabled="loading">
            <option value="day">按日</option>
            <option value="week">按周</option>
            <option value="month">按月</option>
          </select>
        </label>
        <button @click="loadData" :disabled="loading">{{ loading ? '加载中…' : '刷新' }}</button>
        <button @click="exportCsv" :disabled="loading || exporting">{{ exporting ? '导出中…' : '导出 CSV' }}</button>
      </div>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <section class="charts" v-if="!error">
      <article class="chart-card">
        <header>
          <h2>预警趋势</h2>
          <span class="sub">按选择的时间粒度展示预警数量走势</span>
        </header>
        <div ref="trendChartEl" class="chart"></div>
        <p v-if="!trendBuckets.length && !loading" class="empty">暂无数据</p>
      </article>

      <article class="chart-card">
        <header>
          <h2>严重级别分布</h2>
          <span class="sub">堆叠柱状图展示各级别数量，占比一目了然</span>
        </header>
        <div ref="distributionChartEl" class="chart"></div>
        <p v-if="!trendBuckets.length && !loading" class="empty">暂无数据</p>
      </article>

      <article class="chart-card">
        <header>
          <h2>指标 TOP {{ limit }}</h2>
          <span class="sub">点击柱状图可跳转到对应指标的预警列表</span>
        </header>
        <div ref="topChartEl" class="chart"></div>
        <p v-if="!topMetrics.length && !loading" class="empty">暂无数据</p>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  exportAlertAnalyticsCsv,
  fetchAlertDistribution,
  fetchAlertTopMetrics,
  fetchAlertTrend
} from '../../services/analytics.js'

const router = useRouter()
const range = ref('thisWeek')
const customFrom = ref('')
const customTo = ref('')
const bucket = ref('day')
const limit = 5
const loading = ref(false)
const exporting = ref(false)
const error = ref('')
const trendBuckets = ref([])
const topMetrics = ref([])

const trendChartEl = ref(null)
const distributionChartEl = ref(null)
const topChartEl = ref(null)

const charts = reactive({ trend: null, distribution: null, top: null })

const severityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO', 'UNKNOWN']

const rangeParams = computed(() => {
  const { from, to } = resolveRange()
  return { from, to }
})

function resolveRange() {
  const now = new Date()
  if (range.value === 'thisWeek') {
    const start = new Date(now)
    const day = (now.getDay() + 6) % 7
    start.setDate(now.getDate() - day)
    start.setHours(0, 0, 0, 0)
    return { from: start.toISOString(), to: now.toISOString() }
  }
  if (range.value === 'thisMonth') {
    const start = new Date(now.getFullYear(), now.getMonth(), 1)
    return { from: start.toISOString(), to: now.toISOString() }
  }
  const fromValue = customFrom.value ? new Date(customFrom.value) : null
  const toValue = customTo.value ? new Date(customTo.value) : null
  return {
    from: fromValue ? fromValue.toISOString() : undefined,
    to: toValue ? toValue.toISOString() : undefined
  }
}

function onRangeChange() {
  if (range.value !== 'custom') {
    customFrom.value = ''
    customTo.value = ''
    loadData()
  }
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const params = rangeParams.value
    const baseParams = { ...params, bucket: bucket.value }
    const [trend, distribution, metrics] = await Promise.all([
      fetchAlertTrend(baseParams),
      fetchAlertDistribution(baseParams),
      fetchAlertTopMetrics({ ...baseParams, limit })
    ])
    trendBuckets.value = normalizeBuckets(trend || distribution)
    topMetrics.value = metrics || []
    await nextTick()
    initCharts()
    renderCharts()
  } catch (err) {
    error.value = err?.message || '预警分析数据加载失败'
  } finally {
    loading.value = false
  }
}

function normalizeBuckets(input) {
  if (!Array.isArray(input)) return []
  return input.map(item => ({
    bucketStart: item.bucketStart,
    bucketEnd: item.bucketEnd,
    total: item.total || 0,
    severityCounts: item.severityCounts || {}
  }))
}

function initCharts() {
  if (trendChartEl.value && !charts.trend) {
    charts.trend = echarts.init(trendChartEl.value)
    charts.trend.on('click', params => handleTrendPointClick(params.dataIndex))
  }
  if (distributionChartEl.value && !charts.distribution) {
    charts.distribution = echarts.init(distributionChartEl.value)
    charts.distribution.on('click', params => handleDistributionClick(params))
  }
  if (topChartEl.value && !charts.top) {
    charts.top = echarts.init(topChartEl.value)
    charts.top.on('click', params => handleMetricClick(params.dataIndex))
  }
}

function renderCharts() {
  renderTrendChart()
  renderDistributionChart()
  renderTopChart()
}

function renderTrendChart() {
  if (!charts.trend) return
  const labels = trendBuckets.value.map(b => formatBucketLabel(b.bucketStart, bucket.value))
  const totals = trendBuckets.value.map(b => b.total)
  charts.trend.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '预警数', type: 'line', smooth: true, data: totals }]
  }, true)
}

function renderDistributionChart() {
  if (!charts.distribution) return
  const labels = trendBuckets.value.map(b => formatBucketLabel(b.bucketStart, bucket.value))
  const severities = collectSeverities()
  const series = severities.map(severity => ({
    name: severity,
    type: 'bar',
    stack: 'severity',
    emphasis: { focus: 'series' },
    data: trendBuckets.value.map(b => b.severityCounts[severity] || 0)
  }))
  charts.distribution.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 40, right: 20, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', minInterval: 1 },
    series
  }, true)
}

function renderTopChart() {
  if (!charts.top) return
  const categories = topMetrics.value.map(item => item.metricCode || '未命名指标')
  const data = topMetrics.value.map(item => item.total || 0)
  charts.top.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ type: 'bar', data }]
  }, true)
}

function collectSeverities() {
  const dynamic = new Set()
  trendBuckets.value.forEach(bucket => {
    Object.keys(bucket.severityCounts || {}).forEach(key => dynamic.add(key))
  })
  const ordered = severityOrder.filter(severity => dynamic.has(severity))
  dynamic.forEach(item => {
    if (!ordered.includes(item)) ordered.push(item)
  })
  return ordered
}

function formatBucketLabel(bucketStart, currentBucket) {
  if (!bucketStart) return '-'
  const date = new Date(bucketStart)
  if (Number.isNaN(date.getTime())) return bucketStart
  if (currentBucket === 'day') {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
  if (currentBucket === 'week') {
    const weekEnd = new Date(date)
    weekEnd.setDate(weekEnd.getDate() + 6)
    return `${date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })}~${weekEnd.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })}`
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

function handleTrendPointClick(index) {
  const bucketData = trendBuckets.value[index]
  if (!bucketData) return
  openAlerts({ from: bucketData.bucketStart, to: bucketData.bucketEnd })
}

function handleDistributionClick(params) {
  const bucketData = trendBuckets.value[params.dataIndex]
  if (!bucketData) return
  const severity = params?.seriesName
  const query = { from: bucketData.bucketStart, to: bucketData.bucketEnd }
  if (severity) {
    query.severity = severity
  }
  openAlerts(query)
}

function handleMetricClick(index) {
  const metric = topMetrics.value[index]
  if (!metric) return
  const query = {}
  if (metric.metricCode) {
    query.metric = metric.metricCode
  }
  const { from, to } = rangeParams.value
  if (from) query.from = from
  if (to) query.to = to
  openAlerts(query)
}

function openAlerts(query) {
  router.push({ name: 'alerts-board', query })
}

async function exportCsv() {
  exporting.value = true
  error.value = ''
  try {
    const blob = await exportAlertAnalyticsCsv({ ...rangeParams.value, bucket: bucket.value })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'alert-analytics.csv'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (err) {
    error.value = err?.message || '导出失败'
  } finally {
    exporting.value = false
  }
}

function disposeCharts() {
  Object.keys(charts).forEach(key => {
    if (charts[key]) {
      charts[key].dispose()
      charts[key] = null
    }
  })
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', renderCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderCharts)
  disposeCharts()
})

defineExpose({
  handleTrendPointClick,
  handleDistributionClick,
  handleMetricClick,
  openAlerts,
  loadData
})
</script>

<style scoped>
.alert-analytics { max-width: 1200px; margin: 0 auto; padding: 16px; text-align: left; }
.alert-analytics__header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.filters { display: flex; gap: 12px; align-items: flex-end; flex-wrap: wrap; }
.filters label { display: flex; flex-direction: column; font-size: 14px; color: #475569; }
.filters input, .filters select { min-width: 160px; padding: 6px; border: 1px solid #cbd5f5; border-radius: 4px; }
.filters button { padding: 8px 16px; border: none; border-radius: 4px; background: #1d4ed8; color: #fff; cursor: pointer; }
.filters button[disabled] { opacity: 0.6; cursor: not-allowed; }
.hint { margin-top: 4px; color: #64748b; font-size: 14px; }
.charts { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 16px; margin-top: 16px; }
.chart-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; box-shadow: 0 2px 4px rgba(15,23,42,0.06); }
.chart-card header { display: flex; flex-direction: column; margin-bottom: 8px; }
.chart-card h2 { margin: 0; font-size: 18px; color: #1e293b; }
.chart-card .sub { font-size: 13px; color: #64748b; margin-top: 2px; }
.chart { width: 100%; height: 260px; }
.error { color: #dc2626; margin-top: 12px; }
.empty { color: #94a3b8; text-align: center; margin-top: 12px; }
</style>
