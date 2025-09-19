import { reactive } from 'vue'

const state = reactive({
  articles: [], mappings: [], totals: { articles: 0, mappings: 0 }
})

export function useKbStore() { return state }
export function setArticles(list, total=0) { state.articles = Array.isArray(list)?list:[]; state.totals.articles = total }
export function setMappings(list, total=0) { state.mappings = Array.isArray(list)?list:[]; state.totals.mappings = total }

