<template>
  <el-dialog
    v-model="visible"
    width="680px"
    :show-close="false"
    destroy-on-close
    top="6vh"
    class="schedule-dialog !rounded-2xl !p-0 overflow-hidden wk-crm-el-field-scope"
  >
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="size-10 rounded-xl bg-primary/10 flex items-center justify-center">
            <span class="material-symbols-outlined text-xl text-primary">calendar_month</span>
          </div>
          <div>
            <h2 class="text-lg font-bold text-slate-900">新增日程</h2>
            <p class="text-xs text-slate-500 mt-0.5">手动填写日程详细信息</p>
          </div>
        </div>
        <button
          class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors"
          @click="visible = false"
        >
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </template>

    <div class="space-y-6 bg-white px-5 pb-6 pt-5 md:px-6 md:pb-7 md:pt-6">
      <div class="space-y-5">
        <div>
          <label class="text-xs font-bold text-slate-500 mb-1.5 block">日程标题 <span class="text-red-500">*</span></label>
          <el-input
            v-model="scheduleForm.title"
            placeholder="请输入日程标题"
            size="large"
            class="w-full wk-crm-el-field-input"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="text-xs font-bold text-slate-500 mb-1.5 block">开始日期 <span class="text-red-500">*</span></label>
            <el-date-picker
              v-model="scheduleForm.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择开始日期"
              size="large"
              class="w-full wk-crm-el-field-date"
            />
          </div>
          <div>
            <label class="text-xs font-bold text-slate-500 mb-1.5 block">开始时间 <span class="text-red-500">*</span></label>
            <el-time-picker
              v-model="scheduleForm.startTime"
              value-format="HH:mm"
              format="HH:mm"
              placeholder="选择开始时间"
              size="large"
              class="w-full wk-crm-el-field-date"
            />
          </div>
          <div>
            <label class="text-xs font-bold text-slate-500 mb-1.5 block">结束日期</label>
            <el-date-picker
              v-model="scheduleForm.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择结束日期"
              clearable
              size="large"
              class="w-full wk-crm-el-field-date"
            />
          </div>
          <div>
            <label class="text-xs font-bold text-slate-500 mb-1.5 block">结束时间</label>
            <el-time-picker
              v-model="scheduleForm.endTime"
              value-format="HH:mm"
              format="HH:mm"
              placeholder="选择结束时间"
              clearable
              size="large"
              class="w-full wk-crm-el-field-date"
            />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="text-xs font-bold text-slate-500 mb-1.5 block">类型</label>
            <el-select v-model="scheduleForm.type" class="w-full wk-crm-el-field-select" size="large">
              <el-option label="会议" value="meeting" />
              <el-option label="电话" value="call" />
              <el-option label="拜访" value="visit" />
            </el-select>
          </div>
          <div>
            <label class="text-xs font-bold text-slate-500 mb-1.5 block">地点</label>
            <el-input
              v-model="scheduleForm.location"
              placeholder="请输入地点"
              size="large"
              class="w-full wk-crm-el-field-input"
            />
          </div>
        </div>

        <div>
          <label class="text-xs font-bold text-slate-500 mb-1.5 block">描述备注</label>
          <el-input
            v-model="scheduleForm.description"
            type="textarea"
            :rows="4"
            resize="none"
            placeholder="请输入日程备注信息..."
            class="w-full wk-crm-el-field-input"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3">
        <button
          class="flex-1 py-2.5 text-sm font-bold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-xl transition-colors"
          @click="visible = false"
        >
          取消
        </button>
        <button
          :disabled="!canSave"
          class="flex-1 py-2.5 text-sm font-bold text-white bg-primary hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed rounded-xl transition-colors shadow-sm"
          @click="handleSaveSchedule"
        >
          {{ saving ? '保存中...' : '确认保存' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addSchedule, type ScheduleAddBO } from '@/api/schedule'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'created'): void
}>()

type ScheduleFormState = {
  title: string
  startDate: string
  startTime: string
  endDate: string
  endTime: string
  type: string
  location: string
  description: string
}

function createDefaultFormState(): ScheduleFormState {
  return {
    title: '',
    startDate: '',
    startTime: '',
    endDate: '',
    endTime: '',
    type: 'meeting',
    location: '',
    description: ''
  }
}

const saving = ref(false)
const scheduleForm = reactive<ScheduleFormState>(createDefaultFormState())

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const canSave = computed(() =>
  !!scheduleForm.title && !!scheduleForm.startDate && !!scheduleForm.startTime && !saving.value
)

watch(
  () => props.modelValue,
  value => {
    if (!value) resetScheduleForm()
  }
)

function resetScheduleForm() {
  Object.assign(scheduleForm, createDefaultFormState())
}

async function handleSaveSchedule() {
  if (!canSave.value) return

  saving.value = true
  try {
    const data: ScheduleAddBO = {
      title: scheduleForm.title,
      startTime: `${scheduleForm.startDate}T${scheduleForm.startTime}:00`,
      type: scheduleForm.type,
      location: scheduleForm.location || undefined,
      description: scheduleForm.description || undefined
    }

    if (scheduleForm.endDate && scheduleForm.endTime) {
      data.endTime = `${scheduleForm.endDate}T${scheduleForm.endTime}:00`
    }

    await addSchedule(data)
    ElMessage.success('日程创建成功')
    emit('created')
    visible.value = false
  } catch (e: any) {
    ElMessage.error('创建日程失败: ' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}
</script>

<style>
.schedule-dialog .el-dialog__header {
  padding: 22px 24px 16px !important;
  margin-right: 0;
}

.schedule-dialog .el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
  padding: 0 !important;
}

.schedule-dialog .el-dialog__footer {
  padding: 14px 24px 22px !important;
}

.el-overlay:has(.schedule-dialog) {
  overflow: hidden;
}
</style>
