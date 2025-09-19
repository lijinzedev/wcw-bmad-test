import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { auth } from './services/auth'
import { startEventStream, setEnabled as setRealtimeEnabled } from './services/eventStream'
import { useSettingsStore } from './stores/settingsStore'

const app = createApp(App)

app.use(router)

app.mount('#app')

// Start SSE after mount if logged in; fallback to polling if not supported
if (auth.isAuthenticated()) {
  try {
    const settings = useSettingsStore()
    setRealtimeEnabled(!!settings.realtimeEnabled)
    if (settings.realtimeEnabled) startEventStream()
  } catch (e) {}
}
