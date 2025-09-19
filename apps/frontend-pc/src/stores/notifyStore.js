import { reactive } from 'vue'

const state = reactive({
  alertsBadge: 0,
  toasts: [] // { id, title, message, ts }
})

export function useNotifyStore() { return state }

export function setAlertsBadge(n) { state.alertsBadge = Math.max(0, Number(n || 0)) }
export function incAlertsBadge(delta = 1) { state.alertsBadge = Math.max(0, state.alertsBadge + (Number(delta) || 0)) }
export function clearAlertsBadge() { state.alertsBadge = 0 }

let nextId = 1
export function pushToast(title, message) {
  const id = nextId++
  state.toasts.push({ id, title, message, ts: Date.now() })
  setTimeout(() => { removeToast(id) }, 5000)
}

export function removeToast(id) {
  const idx = state.toasts.findIndex(t => t.id === id)
  if (idx >= 0) state.toasts.splice(idx, 1)
}

