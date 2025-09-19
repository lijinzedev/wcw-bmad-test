import { auth } from './auth.js'

let es = null
let enabled = false

export function startEventStream() {
  if (!enabled) return
  if (es || typeof window === 'undefined' || typeof EventSource === 'undefined') return
  try {
    es = new EventSource('/api/v1/monitoring/stream', { withCredentials: true })
    es.onmessage = onEvent
    es.addEventListener('NEW_ALERT', onEvent)
    es.addEventListener('ASSIGNED', onEvent)
    es.addEventListener('ACK_CHANGED', onEvent)
    es.addEventListener('ESCALATED', onEvent)
    es.onerror = () => { /* keep connection; browser will retry */ }
  } catch (e) {
    // Fallback: do nothing; polling remains in place via existing pages
  }
}

export function stopEventStream() {
  try { es && es.close() } catch (e) {}
  es = null
}

export function setEnabled(on) {
  enabled = !!on
  if (enabled) startEventStream()
  else stopEventStream()
}

function onEvent(ev) {
  try {
    const payload = typeof ev.data === 'string' ? JSON.parse(ev.data) : ev.data
    const type = payload?.type || ev.type
    if (type && console && console.info) console.info('[SSE]', type, payload?.data || payload)
    // Minimal UI cue; pages can also listen to custom event for finer actions
    if (type && typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('monitoring-event', { detail: payload }))
    }
  } catch (e) {
    // noop
  }
}
