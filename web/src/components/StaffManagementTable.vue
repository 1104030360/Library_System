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

const staff = ref<AccountInfo[]>([])
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

// 載入館員列表
const loadStaff = async () => {
  loading.value = true
  try {
    staff.value = await bookApi.getStaff()
  } catch (error) {
    message.error('載入館員列表失敗')
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

// 新增館員
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
    const result = await bookApi.createStaff(formData.value)
    if (result.success) {
      message.success('館員創建成功')
      showModal.value = false
      loadStaff()
    } else {
      message.error(result.message || '創建失敗')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || '創建館員失敗')
    console.error(error)
  }
}

// 刪除館員
const handleDelete = (userId: string) => {
  dialog.warning({
    title: '確認刪除',
    content: `確定要刪除館員 ${userId} 嗎？此操作無法復原。`,
    positiveText: '確認刪除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const result = await bookApi.deleteStaff(userId)
        if (result.success) {
          message.success('館員已刪除')
          loadStaff()
        } else {
          message.error(result.message || '刪除失敗')
        }
      } catch (error: any) {
        message.error(error.response?.data?.message || '刪除館員失敗')
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  loadStaff()
})
</script>

<template>
  <div class="staff-management-table">
    <div class="mb-4 flex justify-between items-center">
      <h3 class="text-xl font-bold text-slate-800">館員列表</h3>
      <NButton type="primary" @click="handleAdd">
        新增館員
      </NButton>
    </div>

    <NDataTable
      :columns="columns"
      :data="staff"
      :loading="loading"
      :bordered="false"
      :single-line="false"
      striped
    />

    <!-- 新增/編輯對話框 -->
    <NModal
      v-model:show="showModal"
      preset="card"
      title="新增館員"
      style="width: 500px"
      :bordered="false"
      size="huge"
      :segmented="{
        content: 'soft',
        footer: 'soft'
      }"
    >
      <NForm>
        <NFormItem label="館員 ID" required>
          <NInput
            v-model:value="formData.id"
            placeholder="請輸入館員 ID（4位數字，如：0007）"
          />
        </NFormItem>

        <NFormItem label="姓名" required>
          <NInput
            v-model:value="formData.name"
            placeholder="請輸入館員姓名"
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
.staff-management-table {
  padding: 1rem;
}
</style>
