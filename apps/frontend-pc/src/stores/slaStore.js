import { reactive } from 'vue'

const state = reactive({
  rules: [],
  breaches: [],
  totals: { rules: 0, breaches: 0 },
  summary: { ackOnTimeRate: 0, resolveOnTimeRate: 0 }
})

export function useSlaStore() { return state }
export function setSlaRules(list, total = 0) { state.rules = Array.isArray(list) ? list : []; state.totals.rules = total }
export function setSlaBreaches(list, total = 0) { state.breaches = Array.isArray(list) ? list : []; state.totals.breaches = total }
export function setSlaSummary(s) { state.summary = s || {} }
export function clearSlaStore() { state.rules = []; state.breaches = []; state.totals = { rules: 0, breaches: 0 }; state.summary = {} }

