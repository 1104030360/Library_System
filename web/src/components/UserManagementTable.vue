<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NDataTable,
  NButton,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NSpace,
  useMessage,
  useDialog,
  type DataTableColumns
} from 'naive-ui'
import { bookApi } from '@/api'
import type { AccountInfo, CreateAccountRequest } from '@/types'

const message = useMessage()
const dialog = useDialog()

const users = ref<AccountInfo[]>([])
const loading = ref(false)
const showModal = ref(false)

const formData = ref<CreateAccountRequest>({
  id: '',
  name: '',
  password: '',
  email: ''
})

// 表格列定義
const columns: DataTableColumns<AccountInfo> = [
  { title: 'ID', key: 'id', width: 100 },
  { title: '姓名', key: 'name', width: 150 },
  { title: 'Email', key: 'email', ellipsis: { tooltip: true } },
  { title: '建立日期', key: 'createdAt', width: 180 },
  { title: '最後登入', key: 'lastLogin', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id)
          }, { default: () => '刪除' })
        ]
      })
    }
  }
]

// 載入使用者列表
const loadUsers = async () => {
  loading.value = true
  try {
    users.value = await bookApi.getUsers()
  } catch (error) {
    message.error('載入使用者列表失敗')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 開啟新增對話框
const handleAdd = () => {
  formData.value = {
    id: '',
    name: '',
    password: '',
    email: ''
  }
  showModal.value = true
}

// 新增使用者
const handleCreate = async () => {
  if (!formData.value.id || !formData.value.name || !formData.value.password) {
    message.warning('請填寫所有必填欄位')
    return
  }

  if (formData.value.password.length < 6) {
    message.warning('密碼至少需要 6 個字元')
    return
  }

  try {
    const result = await bookApi.createUser(formData.value)
    if (result.success) {
      message.success('使用者創建成功')
      showModal.value = false
      loadUsers()
    } else {
      message.error(result.message || '創建失敗')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '創建使用者失敗')
    console.error(error)
  }
}

// 刪除使用者
const handleDelete = (userId: string) => {
  dialog.warning({
    title: '確認刪除',
    content: `確定要刪除使用者 ${userId} 嗎？此操作無法復原。`,
    positiveText: '確認刪除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const result = await bookApi.deleteUser(userId)
        if (result.success) {
          message.success('使用者已刪除')
          loadUsers()
        } else {
          message.error(result.message || '刪除失敗')
        }
      } catch (error: any) {
        message.error(error.response?.data?.message || '刪除使用者失敗')
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="user-management-table">
    <div class="mb-4 flex justify-between items-center">
      <h3 class="text-xl font-bold text-slate-800">使用者列表</h3>
      <NButton type="primary" @click="handleAdd">
        新增使用者
      </NButton>
    </div>

    <NDataTable
      :columns="columns"
      :data="users"
      :loading="loading"
      :bordered="false"
      :single-line="false"
      striped
    />

    <!-- 新增/編輯對話框 -->
    <NModal
      v-model:show="showModal"
      preset="card"
      title="新增使用者"
      style="width: 500px"
      :bordered="false"
      size="huge"
      :segmented="{
        content: 'soft',
        footer: 'soft'
      }"
    >
      <NForm>
        <NFormItem label="使用者 ID" required>
          <NInput
            v-model:value="formData.id"
            placeholder="請輸入使用者 ID（4位數字，如：1001）"
          />
        </NFormItem>

        <NFormItem label="姓名" required>
          <NInput
            v-model:value="formData.name"
            placeholder="請輸入使用者姓名"
          />
        </NFormItem>

        <NFormItem label="密碼" required>
          <NInput
            v-model:value="formData.password"
            type="password"
            placeholder="請輸入密碼（至少 6 個字元）"
            show-password-on="click"
          />
        </NFormItem>

        <NFormItem label="Email">
          <NInput
            v-model:value="formData.email"
            placeholder="請輸入 Email（選填）"
          />
        </NFormItem>
      </NForm>

      <template #footer>
        <div class="flex justify-end gap-3">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleCreate">確認新增</NButton>
        </div>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.user-management-table {
  padding: 1rem;
}
</style>
