<script setup lang="ts">
import { ref } from 'vue'
import { NTabs, NTabPane, NAlert } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import UserManagementTable from './UserManagementTable.vue'
import StaffManagementTable from './StaffManagementTable.vue'

const authStore = useAuthStore()
const activeSubTab = ref('users')
</script>

<template>
  <div class="account-management py-4">
    <NTabs v-model:value="activeSubTab" type="card" size="medium" animated>
      <!-- 使用者管理（館長+館員可見） -->
      <NTabPane name="users" tab="使用者管理">
        <UserManagementTable />
      </NTabPane>

      <!-- 員工管理（僅館長可見）⭐ -->
      <NTabPane v-if="authStore.isAdmin" name="staff" tab="員工管理">
        <StaffManagementTable />
      </NTabPane>
    </NTabs>

    <!-- 權限提示（僅館員可見） -->
    <NAlert v-if="authStore.isStaff" type="info" class="mt-4" closable>
      💡 您當前為館員角色，只能管理使用者帳號。員工帳號管理需館長權限。
    </NAlert>
  </div>
</template>

<style scoped>
.account-management {
  min-height: 400px;
}
</style>
