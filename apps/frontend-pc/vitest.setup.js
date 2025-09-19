// Minimal localStorage polyfill for tests
if (typeof global.localStorage === 'undefined') {
  const store = new Map()
  global.localStorage = {
    getItem: (k) => (store.has(k) ? store.get(k) : null),
    setItem: (k, v) => store.set(k, String(v)),
    removeItem: (k) => store.delete(k),
    clear: () => store.clear()
  }
}

// Provide base URL used by router
process.env.BASE_URL = '/'

