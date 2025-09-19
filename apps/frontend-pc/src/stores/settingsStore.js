import { reactive } from 'vue'

const KEY = 'pc_settings'
function load() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return {}
    return JSON.parse(raw)
  } catch (_) { return {} }
}
function save(obj) {
  try { localStorage.setItem(KEY, JSON.stringify(obj || {})) } catch (_) {}
}

const initial = load()
const state = reactive({
  realtimeEnabled: Boolean(initial.realtimeEnabled || false),
  soundEnabled: Boolean(initial.soundEnabled || false)
})

export function useSettingsStore() { return state }
export function setRealtimeEnabled(on) { state.realtimeEnabled = !!on; persist() }
export function setSoundEnabled(on) { state.soundEnabled = !!on; persist() }
function persist() { save({ realtimeEnabled: state.realtimeEnabled, soundEnabled: state.soundEnabled }) }
