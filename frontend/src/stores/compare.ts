import { defineStore } from 'pinia'
import { ref } from 'vue'

const MAX_COMPARE = 4

export const useCompareStore = defineStore('compare', () => {
  const ids = ref<number[]>([])

  function add(id: number) {
    if (ids.value.includes(id)) return
    if (ids.value.length >= MAX_COMPARE) {
      ids.value = [...ids.value.slice(1), id]
    } else {
      ids.value = [...ids.value, id]
    }
  }

  function remove(id: number) {
    ids.value = ids.value.filter((i) => i !== id)
  }

  function clear() {
    ids.value = []
  }

  return { ids, add, remove, clear }
})
