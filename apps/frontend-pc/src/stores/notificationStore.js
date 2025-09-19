import { reactive } from 'vue'

const state = reactive({
  rules: [],
  logs: [],
  totals: { rules: 0, logs: 0 }
})

export function useNotificationStore() { return state }
export function setRules(list, total = 0) { state.rules = Array.isArray(list) ? list : []; state.totals.rules = total }
export function setLogs(list, total = 0) { state.logs = Array.isArray(list) ? list : []; state.totals.logs = total }
export function clearNotificationStore() { state.rules = []; state.logs = []; state.totals = { rules: 0, logs: 0 } }

