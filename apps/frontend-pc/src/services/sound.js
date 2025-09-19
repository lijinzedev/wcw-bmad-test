let audio = null

function ensureAudio() {
  if (audio) return audio
  // Simple short beep (440Hz ~100ms) base64 wav
  // Pre-generated minimal WAV data
  const beep = "data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEAESsAACJWAAACABAAZGF0YRAAAAABAQEBAP///wAAAP///wAAAP///wAAAP///wAAAP///wAAAP///wAAAP///wAAAP8="
  audio = new Audio(beep)
  audio.preload = 'auto'
  return audio
}

export function playBeep() {
  try {
    const a = ensureAudio()
    a.currentTime = 0
    a.volume = 0.6
    a.play().catch(() => {})
  } catch (e) {}
}

