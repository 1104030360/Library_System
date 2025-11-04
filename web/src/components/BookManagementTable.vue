<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBooksStore } from '@/stores/books'
import { NInput, NButton, NDataTable, NTag } from 'naive-ui'
import type { Book } from '@/types'
import type { DataTableColumns } from 'naive-ui'
import { h } from 'vue'

const router = useRouter()
const booksStore = useBooksStore()
const searchQuery = ref('')

const emit = defineEmits<{
  edit: [book: Book]
  delete: [book: Book]
}>()

const columns: DataTableColumns<Book> = [
  {
    title: '編號',
    key: 'id',
    width: 100,
    ellipsis: { tooltip: true }
  },
  {
    title: '書名',
    key: 'title',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '作者',
    key: 'author',
    width: 130,
    ellipsis: { tooltip: true }
  },
  {
    title: '出版社',
    key: 'publisher',
    width: 130,
    ellipsis: { tooltip: true }
  },
  {
    title: '借閱',
    key: 'borrowCount',
    width: 80,
    align: 'center',
    render: (row) => {
      return h('span', { class: 'font-semibold text-blue-600' },
        row.borrowCount || 0)
    }
  },
  {
    title: '評分',
    key: 'averageRating',
    width: 90,
    align: 'center',
    render: (row) => {
      const rating = row.averageRating || 0
      return h('span', {
        class: rating >= 4 ? 'font-semibold text-green-600' :
               rating >= 3 ? 'font-semibold text-yellow-600' :
               'font-semibold text-gray-500'
      }, rating > 0 ? `⭐${rating.toFixed(1)}` : '-')
    }
  },
  {
    title: '評論',
    key: 'reviewCount',
    width: 80,
    align: 'center',
    render: (row) => {
      return h('span', { class: 'text-slate-600' },
        row.reviewCount || 0)
    }
  },
  {
    title: '狀態',
    key: 'isAvailable',
    width: 90,
    render: (row) => {
      return h(
        NTag,
        {
          type: row.isAvailable ? 'success' : 'warning',
          size: 'small'
        },
        { default: () => (row.isAvailable ? '可借' : '借出') }
      )
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    render: (row) => {
      return h('div', { class: 'flex gap-2 flex-nowrap' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => router.push(`/book/${row.id}`)
          },
          { default: () => '詳情' }
        ),
        h(
          NButton,
          {
            type: 'primary',
            size: 'small',
            onClick: () => emit('edit', row)
          },
          { default: () => '編輯' }
        ),
        h(
          NButton,
          {
            type: 'error',
            size: 'small',
            onClick: () => emit('delete', row)
          },
          { default: () => '刪除' }
        )
      ])
    }
  }
]

const filteredBooks = computed(() => {
  if (!searchQuery.value) return booksStore.books

  const query = searchQuery.value.toLowerCase()
  return booksStore.books.filter(
    (book: Book) =>
      book.title.toLowerCase().includes(query) ||
      book.author.toLowerCase().includes(query) ||
      book.publisher.toLowerCase().includes(query) ||
      book.id.toLowerCase().includes(query)
  )
})
</script>

<template>
  <div class="book-management-table">
    <div class="mb-4 flex justify-between items-center">
      <NInput
        v-model:value="searchQuery"
        placeholder="搜尋書籍編號、書名、作者、出版社..."
        clearable
        class="w-96"
      />

      <NButton type="primary" @click="router.push('/add-book')"> + 新增書籍 </NButton>
    </div>

    <NDataTable
      :columns="columns"
      :data="filteredBooks"
      :pagination="{ pageSize: 10 }"
      :scroll-x="1200"
      striped
    >
      <template #empty>
        <div class="text-center py-8 text-slate-400">查無書籍資料</div>
      </template>
    </NDataTable>
  </div>
</template>

<style scoped>
.book-management-table {
  width: 100%;
  overflow-x: auto;
}

/* 確保表格在小螢幕上可以滾動 */
:deep(.n-data-table) {
  min-width: 100%;
}
</style>
