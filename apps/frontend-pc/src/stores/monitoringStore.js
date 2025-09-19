import { reactive } from 'vue'

const state = reactive({
  thresholds: [],
  alerts: [],
  totals: {
    thresholds: 0,
    alerts: 0
  },
  pollingStatus: null
})

export function useMonitoringStore() {
  return state
}

export function setThresholds(list, total = 0) {
  state.thresholds = Array.isArray(list) ? list : []
  state.totals.thresholds = total
}

export function setAlerts(list, total = 0) {
  state.alerts = Array.isArray(list) ? list : []
  state.totals.alerts = total
}

export function setPollingStatus(result) {
  state.pollingStatus = result
}

export function clearMonitoringStore() {
  state.thresholds = []
  state.alerts = []
  state.totals = { thresholds: 0, alerts: 0 }
  state.pollingStatus = null
}
