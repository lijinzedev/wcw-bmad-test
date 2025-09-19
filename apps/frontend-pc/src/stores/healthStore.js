import { reactive } from 'vue'

const state = reactive({
  factors: [],
  exposures: [],
  checks: [],
  cases: [],
  followUps: [],
  totals: {
    factors: 0,
    exposures: 0,
    checks: 0,
    cases: 0
  }
})

export function useHealthStore() {
  return state
}

export function setFactors(list, total = 0) {
  state.factors = Array.isArray(list) ? list : []
  state.totals.factors = total
}

export function setExposures(list, total = 0) {
  state.exposures = Array.isArray(list) ? list : []
  state.totals.exposures = total
}

export function setChecks(list, total = 0) {
  state.checks = Array.isArray(list) ? list : []
  state.totals.checks = total
}

export function setCases(list, total = 0) {
  state.cases = Array.isArray(list) ? list : []
  state.totals.cases = total
}

export function setFollowUps(list) {
  state.followUps = Array.isArray(list) ? list : []
}

export function clearHealthStore() {
  state.factors = []
  state.exposures = []
  state.checks = []
  state.cases = []
  state.followUps = []
  state.totals = { factors: 0, exposures: 0, checks: 0, cases: 0 }
}
