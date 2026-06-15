<template>
  <div class="flex h-full min-h-0 overflow-y-auto bg-background-light md:overflow-hidden">
    <!-- Calendar Main -->
    <div class="flex min-h-0 flex-1 flex-col overflow-y-auto p-4 md:p-8" :class="{ 'border-r border-slate-100': selectedEvent || selectedTask }">
      <div class="flex min-h-0 w-full flex-1 flex-col gap-4 md:gap-6">
        <!-- Header -->
        <div class="shrink-0 flex flex-col gap-3 sm:gap-4 md:flex-row md:items-center md:justify-between">
          <div class="min-w-0">
            <h2 class="text-xl font-bold text-slate-900 sm:text-2xl">智能日程安排</h2>
            <p class="mt-1 line-clamp-2 text-xs leading-5 text-slate-500 sm:text-sm">{{ currentDateStr }} • 今天有 {{ todayScheduleCount }} 场会议和 {{ todayTaskCount }} 个待办任务</p>
          </div>
          <div class="grid w-full grid-cols-[auto_minmax(0,1fr)] items-center gap-2 sm:flex sm:w-auto sm:flex-wrap sm:gap-4">
            <div class="flex shrink-0 items-center justify-between gap-2 rounded-xl border border-slate-200 bg-white p-1 sm:w-auto sm:justify-start">
              <button
                type="button"
                class="size-8 flex items-center justify-center rounded-lg hover:bg-slate-50 text-slate-600 transition-colors"
                aria-label="上一段"
                @click="shiftCalendarAnchor(-1)"
              >
                <span class="material-symbols-outlined text-[20px]">chevron_left</span>
              </button>
              <button
                type="button"
                class="px-3 py-1 text-xs font-bold text-slate-600 hover:text-primary transition-colors"
                @click="goCalendarToday"
              >
                今天
              </button>
              <button
                type="button"
                class="size-8 flex items-center justify-center rounded-lg hover:bg-slate-50 text-slate-600 transition-colors"
                aria-label="下一段"
                @click="shiftCalendarAnchor(1)"
              >
                <span class="material-symbols-outlined text-[20px]">chevron_right</span>
              </button>
            </div>
            <div class="flex min-w-0 items-center rounded-lg border border-[var(--wk-input-border)] bg-[var(--wk-input-bg)] p-1">
              <button
                v-for="mode in viewModes"
                :key="mode.value"
                type="button"
                @click="viewMode = mode.value"
                class="min-w-0 flex-1 rounded-md px-2 py-1.5 text-sm font-medium transition-colors sm:flex-none sm:px-5"
                :class="viewMode === mode.value
                  ? 'bg-[var(--wk-bg-surface-hover)] text-primary'
                  : 'text-slate-600 hover:text-slate-900'"
              >{{ mode.label }}</button>
            </div>
            <button
              type="button"
              @click="showAddDialog = true"
              class="col-span-2 flex h-11 shrink-0 items-center justify-center gap-1.5 whitespace-nowrap rounded-xl bg-primary px-4 text-sm font-bold text-white shadow-sm transition-colors hover:bg-primary/90 sm:col-auto sm:gap-2 sm:px-6 sm:py-2.5"
            >
              <span class="material-symbols-outlined wk-plus-button-icon">add</span>
              新增日程
            </button>
          </div>
        </div>

        <!-- Calendar Views -->
        <div
          class="shrink-0 bg-white border rounded-2xl overflow-hidden shadow-sm relative"
          :class="[
            viewMode === 'month' ? 'border-[var(--wk-input-border)]' : 'border-[var(--wk-border-subtle)]',
            viewMode === 'grid'
              ? 'min-h-0 flex flex-col sm:flex-1'
              : viewMode === 'month'
                ? 'min-h-0 flex flex-col sm:flex-1'
                : 'shrink-0'
          ]"
        >
          <!-- Week View -->
          <div v-if="viewMode === 'grid'" class="grid h-[124px] grid-cols-7 divide-x divide-[var(--wk-border-subtle)] sm:h-auto sm:min-h-[400px] sm:flex-1">
            <div
              v-for="day in weekDays"
              :key="day.label"
              class="flex flex-col bg-white"
            >
              <div class="flex flex-col items-center justify-center gap-1 border-b border-[var(--wk-border-subtle)] p-1.5 sm:flex-row sm:justify-between sm:p-5">
                <div class="flex flex-col items-center sm:items-start">
                  <span class="text-[10px] font-medium sm:text-sm" :class="day.isToday ? 'text-primary' : 'text-slate-400'">
                    {{ day.label }}
                  </span>
                  <span class="text-[8px] leading-none text-slate-400 sm:text-[10px]">
                    {{ getLunarText(day.fullDate) }}
                  </span>
                </div>
                <span
                  class="flex size-6 shrink-0 items-center justify-center rounded-full text-xs font-bold sm:size-7 sm:text-sm"
                  :class="day.isToday ? 'bg-primary text-white' : 'text-slate-900'"
                >{{ day.date }}</span>
              </div>
              <div
                class="space-y-3 p-1.5 sm:flex-1 sm:p-3"
                :class="[
                  isMobile ? 'h-[60px]' : '',
                  isMobile && (getEventsForDate(day.fullDate).length || getTasksForDate(day.fullDate).length) ? 'cursor-pointer' : ''
                ]"
                @click="openMobileDayDialog(day.fullDate)"
              >
                <div v-if="isMobile" class="flex h-[40px] items-center justify-center">
                  <span
                    v-if="getEventsForDate(day.fullDate).length || getTasksForDate(day.fullDate).length"
                    class="size-2 rounded-full bg-primary/70"
                    aria-hidden="true"
                  />
                </div>
                <template v-else>
                  <div
                    v-for="event in getEventsForDate(day.fullDate)"
                    :key="event.scheduleId"
                    @click="selectedEvent = event; selectedTask = null"
                    class="p-3 rounded-xl border border-primary/20 bg-primary/5 shadow-sm hover:shadow-md hover:bg-primary/10 transition-all cursor-pointer"
                  >
                    <p class="text-xs font-bold text-primary mb-1 truncate">{{ event.title }}</p>
                    <p class="text-xs text-slate-500 truncate">{{ formatTime(event.startTime) }} • {{ event.customerName || event.participantNames || '' }}</p>
                  </div>
                  <!-- Tasks -->
                  <div
                    v-for="task in getTasksForDate(day.fullDate)"
                    :key="task.taskId"
                    class="p-3 rounded-xl border border-slate-200 bg-white shadow-sm transition-all flex items-start gap-2"
                  >
                    <button
                      @click.stop="handleToggleTask(task)"
                      class="mt-0.5 shrink-0 size-4 rounded-sm border flex items-center justify-center transition-colors"
                      :class="task.status === 'COMPLETED'
                        ? 'bg-emerald-500 border-emerald-500 text-white'
                        : 'border-slate-300 hover:border-primary text-transparent hover:text-primary/20'"
                    >
                      <span class="material-symbols-outlined text-[12px] font-bold">check</span>
                    </button>
                    <div class="min-w-0 flex-1" @click="selectTask(task)">
                      <p class="text-xs font-bold mb-1 truncate" :class="task.status === 'COMPLETED' ? 'text-slate-500 line-through' : 'text-slate-700'">
                        {{ task.title }}
                      </p>
                      <p class="text-xs text-slate-400 truncate">{{ task.customerName || '' }}</p>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- Month View -->
          <div v-else-if="viewMode === 'month'" class="flex flex-col sm:min-h-[560px] sm:flex-1">
            <div class="grid grid-cols-7 border-b border-[var(--wk-input-border)] bg-[var(--wk-input-bg)]">
              <div
                v-for="dayLabel in ['周一','周二','周三','周四','周五','周六','周日']"
                :key="dayLabel"
                class="py-2.5 text-center text-[11px] font-bold uppercase text-slate-500 sm:py-3 sm:text-xs"
              >
                {{ dayLabel }}
              </div>
            </div>
            <div class="grid grid-cols-7 grid-rows-5 divide-x divide-y divide-[var(--wk-input-border)] h-[400px] sm:flex-1 sm:h-auto">
              <div
                v-for="(cell, i) in monthCells"
                :key="i"
                class="h-full p-1.5 sm:h-auto sm:min-h-[120px] sm:p-2"
                :class="{ 'bg-slate-50/50': !cell.isCurrentMonth }"
              >
                <div class="flex justify-between items-start mb-1">
                  <span v-if="cell.fullDate" class="truncate text-[9px] leading-5 text-slate-400 sm:text-[10px] sm:leading-6">
                    {{ getLunarText(cell.fullDate) }}
                  </span>
                  <span
                    class="flex size-5 shrink-0 items-center justify-center rounded-full text-[11px] font-medium sm:size-6 sm:text-xs"
                    :class="cell.isToday
                      ? 'bg-primary text-white font-bold'
                      : !cell.isCurrentMonth ? 'text-slate-300' : 'text-slate-700'"
                  >
                    {{ cell.date }}
                  </span>
                </div>
                <div
                  v-if="cell.fullDate"
                  class="space-y-1 py-2"
                  :class="isMobile && (getEventsForDate(cell.fullDate).length || getTasksForDate(cell.fullDate).length) ? 'cursor-pointer' : ''"
                  @click="openMobileDayDialog(cell.fullDate)"
                >
                  <div v-if="isMobile" class="flex items-center justify-center">
                    <span
                      v-if="getEventsForDate(cell.fullDate).length || getTasksForDate(cell.fullDate).length"
                      class="size-1.5 rounded-full bg-primary/70"
                      aria-hidden="true"
                    />
                  </div>
                  <template v-else>
                    <div
                      v-for="event in getEventsForDate(cell.fullDate)"
                      :key="event.scheduleId"
                      @click="selectedEvent = event; selectedTask = null"
                      class="px-2 py-1 text-xs font-medium bg-primary/10 text-primary rounded truncate cursor-pointer hover:bg-primary/20"
                    >
                      {{ formatTime(event.startTime) }} {{ event.title }}
                    </div>
                    <div
                      v-for="task in getTasksForDate(cell.fullDate)"
                      :key="task.taskId"
                      class="px-2 py-1 text-xs font-medium rounded truncate cursor-pointer flex items-center gap-1 bg-slate-50 text-slate-700 hover:bg-slate-100"
                    >
                      <div
                        class="shrink-0 size-3 rounded-sm border flex items-center justify-center transition-colors"
                        :class="task.status === 'COMPLETED'
                          ? 'bg-emerald-500 border-emerald-500 text-white'
                          : 'border-slate-300 text-transparent'"
                        @click.stop="handleToggleTask(task)"
                      >
                        <span class="material-symbols-outlined text-xs font-bold">check</span>
                      </div>
                      <span
                        @click="selectTask(task)"
                        class="truncate"
                        :class="task.status === 'COMPLETED' ? 'line-through text-slate-500' : ''"
                      >{{ task.title }}</span>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- List View -->
          <div v-else class="p-6">
            <div class="max-w-3xl mx-auto space-y-8">
              <div v-if="schedules.length === 0 && tasks.length === 0" class="text-center py-20 text-slate-400">
                <span class="material-symbols-outlined text-4xl mb-2">calendar_today</span>
                <p class="text-sm">暂无日程安排和待办任务</p>
              </div>

              <div v-for="group in listGroups" :key="group.dateStr" class="space-y-4">
                <h3 class="text-sm font-bold text-slate-900 flex items-center gap-2">
                  <span class="size-2 rounded-full bg-primary"></span>
                  {{ group.header }}
                  <span class="text-xs font-normal text-slate-400 ml-2">农历 {{ group.lunar }}</span>
                </h3>

                <div class="space-y-4 ml-4 border-l-2 border-slate-100 pl-6">
                  <div
                    v-for="item in group.items"
                    :key="item.key"
                    class="relative bg-white border border-slate-200 rounded-xl p-4 hover:shadow-md transition-all group"
                    :class="item.kind === 'schedule' ? 'cursor-pointer' : ''"
                    @click="item.kind === 'schedule' ? (selectedEvent = item.payload, selectedTask = null) : selectTask(item.payload)"
                  >
                    <div
                      class="absolute -left-[32px] top-5 size-3 bg-white border-2 rounded-full"
                      :class="item.kind === 'schedule' ? 'border-primary' : 'border-slate-300'"
                    ></div>

                    <div v-if="item.kind === 'schedule'" class="flex items-start justify-between gap-4">
                      <div class="min-w-0">
                        <h4 class="text-sm font-bold text-slate-900 mb-1 group-hover:text-primary transition-colors truncate">
                          {{ item.payload.title }}
                        </h4>
                        <div class="flex items-center gap-2 flex-wrap">
                          <span v-if="item.payload.customerName" class="text-xs text-slate-500 font-medium">{{ item.payload.customerName }}</span>
                          <span v-if="item.payload.typeName" class="text-xs px-2 py-0.5 bg-primary/10 text-primary rounded-full font-bold">{{ item.payload.typeName }}</span>
                          <span v-if="item.payload.location" class="text-xs px-2 py-0.5 bg-slate-50 text-slate-600 rounded-full font-bold">{{ item.payload.location }}</span>
                        </div>
                      </div>
                      <div class="text-right shrink-0">
                        <span class="inline-block px-2 py-1 bg-slate-50 text-slate-600 text-xs font-bold rounded-lg">
                          {{ item.timeLabel }}
                        </span>
                      </div>
                    </div>

                    <div v-else class="flex items-start gap-3">
                      <button
                        @click.stop="handleToggleTask(item.payload)"
                        class="mt-0.5 shrink-0 size-5 rounded border flex items-center justify-center transition-colors"
                        :class="item.payload.status === 'COMPLETED'
                          ? 'bg-emerald-500 border-emerald-500 text-white'
                          : 'border-slate-300 hover:border-primary text-transparent hover:text-primary/20'"
                        aria-label="切换任务完成状态"
                        :title="item.payload.status === 'COMPLETED' ? '标记为未完成' : '标记为已完成'"
                      >
                        <span class="material-symbols-outlined text-[14px] font-bold">check</span>
                      </button>

                      <div class="min-w-0 flex-1" @click="selectTask(item.payload)">
                        <h4
                          class="text-sm font-bold mb-1 truncate"
                          :class="item.payload.status === 'COMPLETED' ? 'text-slate-400 line-through' : 'text-slate-900'"
                        >
                          {{ item.payload.title }}
                        </h4>
                        <div class="flex items-center gap-2 flex-wrap">
                          <span v-if="item.payload.customerName" class="text-xs text-slate-500">{{ item.payload.customerName }}</span>
                          <span v-if="item.payload.dueDate" class="text-xs px-2 py-0.5 bg-slate-50 text-slate-600 rounded-full font-bold">
                            截止 {{ formatDueDate(item.payload.dueDate) }}
                          </span>
                          <span
                            v-if="item.payload.priority"
                            class="text-xs px-2 py-0.5 rounded-full font-bold"
                            :class="{
                              'bg-red-50 text-red-500': item.payload.priority === 'HIGH',
                              'bg-amber-50 text-amber-500': item.payload.priority === 'MEDIUM',
                              'bg-slate-100 text-slate-500': item.payload.priority === 'LOW',
                            }"
                          >{{ item.payload.priority === 'HIGH' ? '高' : item.payload.priority === 'MEDIUM' ? '中' : '低' }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showMobileDayDialog"
      :width="isMobile ? '92%' : '520px'"
      top="12vh"
      :close-on-click-modal="true"
      :append-to-body="true"
      class="wk-dialog--flush"
    >
      <template #header>
        <div class="flex items-center justify-between gap-3 pr-2">
          <div class="min-w-0">
            <p class="truncate text-base font-bold text-slate-900">
              {{ mobileDayDialogDate ? `${mobileDayDialogDate} 日程` : '日程' }}
            </p>
            <p v-if="mobileDayDialogDate" class="mt-0.5 text-xs text-slate-400">
              农历 {{ getLunarText(mobileDayDialogDate) }}
            </p>
          </div>
        </div>
      </template>

      <div class="max-h-[60vh] overflow-auto pb-[env(safe-area-inset-bottom)]">
        <div
          v-if="mobileDayEvents.length === 0 && mobileDayTasks.length === 0"
          class="py-10 text-center text-sm text-slate-400"
        >
          当天暂无日程与任务
        </div>
        <div v-else class="space-y-5">
          <section v-if="mobileDayEvents.length" class="space-y-2">
            <div class="flex items-center justify-between px-1">
              <p class="text-xs font-bold uppercase tracking-wider text-slate-400">日程</p>
              <p class="text-xs font-bold text-slate-300">{{ mobileDayEvents.length }}</p>
            </div>
            <button
              v-for="event in mobileDayEvents"
              :key="event.scheduleId"
              type="button"
              class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-left transition-colors hover:bg-slate-50"
              @click="handleSelectEventFromMobileDialog(event)"
            >
              <p class="truncate text-sm font-bold text-slate-900">{{ event.title }}</p>
              <p class="mt-0.5 truncate text-xs text-slate-500">
                {{ formatTime(event.startTime) }}
                <template v-if="event.customerName || event.participantNames">
                  • {{ event.customerName || event.participantNames }}
                </template>
              </p>
            </button>
          </section>

          <section v-if="mobileDayTasks.length" class="space-y-2">
            <div class="flex items-center justify-between px-1">
              <p class="text-xs font-bold uppercase tracking-wider text-slate-400">任务</p>
              <p class="text-xs font-bold text-slate-300">{{ mobileDayTasks.length }}</p>
            </div>
            <button
              v-for="task in mobileDayTasks"
              :key="task.taskId"
              type="button"
              class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-left transition-colors hover:bg-slate-50"
              @click="handleSelectTaskFromMobileDialog(task)"
            >
              <p class="truncate text-sm font-bold text-slate-900">{{ task.title }}</p>
              <p class="mt-0.5 truncate text-xs text-slate-500">
                <template v-if="task.customerName">{{ task.customerName }}</template>
                <template v-else>-</template>
              </p>
            </button>
          </section>
        </div>
      </div>
    </el-dialog>

    <ScheduleDetailDrawer
      v-if="!isMobile"
      v-model="showScheduleDetailDrawer"
      :schedule="selectedEvent"
      @deleted="loadSchedules"
    />

    <TaskDetailDrawer
      v-model="showCalendarTaskDetail"
      :task="selectedTask"
      :is-mobile="isMobile"
      :ai-insight="selectedTask ? getAiInsight(selectedTask) : ''"
      @edit="handleEditFromDetail"
      @toggle-complete="handleToggleCompleteFromDetail"
      @delete="handleDeleteFromDetail"
    />

    <TaskEditDialog
      v-model="showTaskEditDialog"
      :is-mobile="isMobile"
      :editing-task="editingTask"
      :submitting="submitting"
      :ai-parsing="aiParsing"
      v-model:ai-parse-input="aiParseInput"
      :form-data="formData"
      v-model:selected-participants="selectedParticipants"
      :user-options="userOptions"
      :user-search-loading="userSearchLoading"
      :customer-options="customerOptions"
      :customer-search-loading="customerSearchLoading"
      :search-users="searchUsers"
      :search-customers="searchCustomers"
      @ai-parse="handleAiParse"
      @submit="handleSubmitTask"
    />

    <ScheduleFormDialog
      v-model="showAddDialog"
      @created="loadSchedules"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useResponsive } from '@/composables/useResponsive'
import { getMySchedules } from '@/api/schedule'
import { getMyTasks, updateTaskStatus, aiParseTask, deleteTask, updateTask } from '@/api/task'
import { queryCustomerList } from '@/api/customer'
import { queryUserList } from '@/api/auth'
import type { ScheduleVO } from '@/api/schedule'
import type { Task, TaskAddBO, TaskStatus, TaskUpdateBO } from '@/types/common'
import TaskDetailDrawer from '@/views/task/components/TaskDetailDrawer.vue'
import TaskEditDialog from '@/views/task/components/TaskEditDialog.vue'
import ScheduleDetailDrawer from './components/ScheduleDetailDrawer.vue'
import ScheduleFormDialog from './components/ScheduleFormDialog.vue'

const { isMobile } = useResponsive()
const route = useRoute()

const lunarFormatter = new Intl.DateTimeFormat('zh-Hans-u-ca-chinese', {
  month: 'short',
  day: 'numeric'
})

function getLunarText(dateStr: string): string {
  try {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    if (Number.isNaN(d.getTime())) return ''
    // Example output (depends on runtime): "二月18" / "闰二月18"
    return lunarFormatter.format(d)
  } catch {
    return ''
  }
}

const showScheduleDetailDrawer = computed({
  get: () => !!selectedEvent.value && !isMobile.value,
  set: (val: boolean) => {
    if (!val) selectedEvent.value = null
  }
})

const showCalendarTaskDetail = computed({
  get: () => !!selectedTask.value,
  set: (val: boolean) => {
    if (!val) selectedTask.value = null
  }
})

const viewMode = ref<'grid' | 'month' | 'list'>('grid')
const calendarAnchorDate = ref<Date>(new Date())
const selectedEvent = ref<ScheduleVO | null>(null)
const selectedTask = ref<Task | null>(null)
const schedules = ref<ScheduleVO[]>([])
const tasks = ref<Task[]>([])
const showTaskEditDialog = ref(false)
const editingTask = ref<Task | null>(null)
const showMobileDayDialog = ref(false)
const mobileDayDialogDate = ref<string | null>(null)
const submitting = ref(false)
const aiParseInput = ref('')
const aiParsing = ref(false)
const customerOptions = ref<{ value: string; label: string }[]>([])
const customerSearchLoading = ref(false)
const userOptions = ref<{ value: string; label: string }[]>([])
const userSearchLoading = ref(false)
const selectedParticipants = ref<string[]>([])
const formData = reactive<TaskAddBO & { status?: TaskStatus; assignedToName?: string }>({
  title: '',
  description: '',
  priority: 'MEDIUM',
  dueDate: undefined,
  status: undefined,
  taskType: '',
  customerId: '',
  assignedToName: ''
})

const viewModes = [
  { value: 'grid' as const, label: '周' },
  { value: 'month' as const, label: '月' },
  { value: 'list' as const, label: '列表' },
]

const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const mobileDayEvents = computed(() => {
  if (!mobileDayDialogDate.value) return []
  return getEventsForDate(mobileDayDialogDate.value)
})

const mobileDayTasks = computed(() => {
  if (!mobileDayDialogDate.value) return []
  return getTasksForDate(mobileDayDialogDate.value)
})

const currentDateStr = computed(() => {
  const anchor = calendarAnchorDate.value
  const y = anchor.getFullYear()
  const m = anchor.getMonth() + 1
  const d = anchor.getDate()
  const dayName = dayNames[anchor.getDay()]
  if (viewMode.value === 'month') {
    return `${y}年${m}月`
  }
  if (viewMode.value === 'grid') {
    const days = weekDays.value
    if (days.length >= 7) {
      const start = new Date(`${days[0].fullDate}T12:00:00`)
      const end = new Date(`${days[6].fullDate}T12:00:00`)
      return `${start.getFullYear()}年${start.getMonth() + 1}月${start.getDate()}日 - ${end.getFullYear()}年${end.getMonth() + 1}月${end.getDate()}日`
    }
  }
  return `${y}年${m}月${d}日，${dayName}`
})

const todayScheduleCount = computed(() => {
  const todayStr = toDateStr(new Date())
  return schedules.value.filter(e => toDateStr(new Date(e.startTime)) === todayStr).length
})

const todayTaskCount = computed(() => {
  const todayStr = toDateStr(new Date())
  return tasks.value.filter(t => t.status !== 'COMPLETED' && normalizeDueDate(t.dueDate ?? '') === todayStr).length
})

// --- Data Loading ---

async function loadSchedules() {
  try {
    schedules.value = await getMySchedules('all')
  } catch (e) {
    console.error('加载日程失败', e)
  }
}

async function loadTasks() {
  try {
    tasks.value = await getMyTasks('all')
    syncSelectedTask()
  } catch (e) {
    console.error('加载任务失败', e)
  }
}

onMounted(async () => {
  await Promise.all([loadSchedules(), loadTasks()])
  syncSelectedRecordFromRoute()
})

watch(
  () => [route.query.scheduleId, route.query.taskId],
  () => syncSelectedRecordFromRoute()
)

// --- Helpers ---

function toDateStr(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function getEventsForDate(dateStr: string): ScheduleVO[] {
  return schedules.value.filter(e => toDateStr(new Date(e.startTime)) === dateStr)
}

function normalizeDueDate(dueDate: string): string {
  if (!dueDate) return ''
  // dueDate could be "yyyy-MM-dd" or ISO datetime string
  if (dueDate.length === 10) return dueDate
  return toDateStr(new Date(dueDate))
}

function getTasksForDate(dateStr: string): Task[] {
  return tasks.value.filter(t => normalizeDueDate(t.dueDate ?? '') === dateStr)
}

function shiftCalendarAnchor(direction: number) {
  const next = new Date(calendarAnchorDate.value)
  if (viewMode.value === 'grid') {
    next.setDate(next.getDate() + 7 * direction)
  } else if (viewMode.value === 'month') {
    next.setMonth(next.getMonth() + direction)
  } else {
    next.setDate(next.getDate() + direction)
  }
  calendarAnchorDate.value = next
}

function goCalendarToday() {
  calendarAnchorDate.value = new Date()
  if (isMobile.value) {
    mobileDayDialogDate.value = toDateStr(new Date())
  }
}

function openMobileDayDialog(dateStr: string) {
  if (!isMobile.value) return
  const events = getEventsForDate(dateStr)
  const dayTasks = getTasksForDate(dateStr)
  if (!events.length && !dayTasks.length) return
  mobileDayDialogDate.value = dateStr
  showMobileDayDialog.value = true
}

function handleSelectEventFromMobileDialog(event: ScheduleVO) {
  selectedEvent.value = event
  selectedTask.value = null
  showMobileDayDialog.value = false
}

function handleSelectTaskFromMobileDialog(task: Task) {
  selectedTask.value = task
  selectedEvent.value = null
  showMobileDayDialog.value = false
}

async function handleToggleTask(task: Task) {
  const newStatus = task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
  try {
    await updateTaskStatus(task.taskId, newStatus.toLowerCase())
    await loadTasks()
  } catch (error) {
    console.error('Update task status failed:', error)
  }
}

function selectTask(task: Task) {
  selectedEvent.value = null
  selectedTask.value = task
}

function syncSelectedRecordFromRoute() {
  const scheduleId = firstQueryValue(route.query.scheduleId)
  const taskId = firstQueryValue(route.query.taskId)
  if (scheduleId) {
    selectedEvent.value = schedules.value.find(item => String(item.scheduleId) === scheduleId) || null
    selectedTask.value = null
    return
  }
  if (taskId) {
    selectedTask.value = tasks.value.find(item => String(item.taskId) === taskId) || null
    selectedEvent.value = null
  }
}

function firstQueryValue(value: unknown): string {
  if (Array.isArray(value)) return value[0] ? String(value[0]) : ''
  return value == null ? '' : String(value)
}

function syncSelectedTask() {
  if (!selectedTask.value) return
  selectedTask.value = tasks.value.find(task => task.taskId === selectedTask.value?.taskId) || null
}

function formatDueDate(dueDate: string): string {
  if (!dueDate) return ''
  const d = new Date(dueDate)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const showAddDialog = ref(false)

async function searchCustomers(query: string) {
  if (!query) {
    customerOptions.value = []
    return
  }
  customerSearchLoading.value = true
  try {
    const res = await queryCustomerList({ keyword: query, page: 1, limit: 20 })
    customerOptions.value = (res.list || []).map((customer: any) => ({
      value: String(customer.customerId),
      label: customer.companyName
    }))
  } catch (e) {
    console.warn('客户搜索失败:', e)
    customerOptions.value = []
  } finally {
    customerSearchLoading.value = false
  }
}

async function searchUsers(query: string) {
  if (!query) {
    userOptions.value = []
    return
  }
  userSearchLoading.value = true
  try {
    const res = await queryUserList({ search: query })
    userOptions.value = (res.list || []).map((user: any) => ({
      value: user.realname || user.username,
      label: user.realname || user.username
    }))
  } catch (e) {
    console.warn('用户搜索失败:', e)
    userOptions.value = []
  } finally {
    userSearchLoading.value = false
  }
}

function handleEdit(task: Task) {
  editingTask.value = task
  Object.assign(formData, {
    title: task.title,
    description: task.description || '',
    priority: task.priority,
    dueDate: task.dueDate ? formatDateTimeLocal(task.dueDate) : undefined,
    status: task.status,
    taskType: task.taskType || '',
    customerId: task.customerId || '',
    assignedToName: task.assignedToName || ''
  })
  if (task.customerId && task.customerName) {
    customerOptions.value = [{ value: String(task.customerId), label: task.customerName }]
  }
  selectedParticipants.value = task.participantNames
    ? task.participantNames.split(/[,，]\s*/).filter(Boolean)
    : []
  userOptions.value = selectedParticipants.value.map(name => ({ value: name, label: name }))
  showTaskEditDialog.value = true
}

function handleEditFromDetail(task: Task) {
  handleEdit(task)
  if (isMobile.value) selectedTask.value = null
}

async function handleToggleCompleteFromDetail(task: Task) {
  await handleToggleTask(task)
  if (isMobile.value) selectedTask.value = null
}

async function handleDeleteFromDetail(task: Task) {
  await handleDeleteTask(task)
  if (isMobile.value) selectedTask.value = null
}

async function handleDeleteTask(task: Task) {
  try {
    await ElMessageBox.confirm(`确定要删除任务「${task.title}」吗？`, '提示', { type: 'warning' })
    await deleteTask(task.taskId)
    if (selectedTask.value?.taskId === task.taskId) {
      selectedTask.value = null
    }
    ElMessage.success('删除成功')
    await loadTasks()
  } catch {
    // Cancelled
  }
}

async function handleSubmitTask() {
  if (!formData.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!formData.dueDate) {
    ElMessage.warning('请选择截止时间')
    return
  }
  if (!editingTask.value) return

  submitting.value = true
  try {
    const submitData: TaskUpdateBO = {
      taskId: editingTask.value.taskId,
      title: formData.title,
      description: formData.description,
      priority: formData.priority,
      dueDate: formData.dueDate,
      taskType: formData.taskType,
      participantNames: selectedParticipants.value.join(', '),
      customerId: formData.customerId || undefined,
      status: formData.status
    }
    await updateTask(submitData)
    ElMessage.success('更新成功')
    showTaskEditDialog.value = false
    resetTaskForm()
    await loadTasks()
  } finally {
    submitting.value = false
  }
}

function resetTaskForm() {
  editingTask.value = null
  aiParseInput.value = ''
  selectedParticipants.value = []
  customerOptions.value = []
  userOptions.value = []
  Object.assign(formData, {
    title: '',
    description: '',
    priority: 'MEDIUM',
    dueDate: undefined,
    status: undefined,
    taskType: '',
    customerId: '',
    assignedToName: ''
  })
}

async function handleAiParse() {
  if (!aiParseInput.value.trim()) return
  aiParsing.value = true
  try {
    const result = await aiParseTask(aiParseInput.value)
    if (result.title) formData.title = result.title
    if (result.dueDate) formData.dueDate = result.dueDate
    if (result.priority) formData.priority = result.priority.toUpperCase() as any
    if (result.taskType) formData.taskType = result.taskType
    if (result.customerName) {
      const res = await queryCustomerList({ keyword: result.customerName, page: 1, limit: 5 })
      const list = res.list || []
      if (list.length > 0) {
        customerOptions.value = list.map((customer: any) => ({
          value: String(customer.customerId),
          label: customer.companyName
        }))
        formData.customerId = String(list[0].customerId)
      }
    }
    if (result.participantNames) {
      selectedParticipants.value = result.participantNames.split(/[,，]\s*/).filter(Boolean)
      userOptions.value = selectedParticipants.value.map(name => ({ value: name, label: name }))
    }
    if (result.description) formData.description = result.description
    if (result.assignedToName) formData.assignedToName = result.assignedToName
    ElMessage.success('AI 解析完成，请确认并补充信息')
  } catch (error) {
    console.error('AI parse task failed:', error)
  } finally {
    aiParsing.value = false
  }
}

function formatDateTimeLocal(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function getAiInsight(task: Task): string {
  if (task.description) return task.description
  if (task.priority === 'HIGH') return '此任务优先级较高，建议尽快处理以推进业务进展。'
  if (task.priority === 'MEDIUM') return '常规跟进任务，按计划执行即可。'
  return '低优先级任务，可在空闲时间处理。'
}

// --- Week View ---

const weekDays = computed(() => {
  const anchor = calendarAnchorDate.value
  const today = anchor.getDay()
  const mondayOffset = today === 0 ? -6 : 1 - today
  const todayStr = toDateStr(new Date())
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(anchor)
    d.setDate(anchor.getDate() + mondayOffset + i)
    return {
      label: ['周一','周二','周三','周四','周五','周六','周日'][i],
      date: d.getDate(),
      fullDate: toDateStr(d),
      isToday: toDateStr(d) === todayStr
    }
  })
})

// --- Month View ---

const monthCells = computed(() => {
  const anchor = calendarAnchorDate.value
  const year = anchor.getFullYear()
  const month = anchor.getMonth()
  const todayStr = toDateStr(new Date())
  const firstDay = new Date(year, month, 1)
  const startDow = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1
  const cells: { date: number; isCurrentMonth: boolean; isToday: boolean; fullDate: string }[] = []
  for (let i = 0; i < 35; i++) {
    const date = i - startDow + 1
    const cellDate = new Date(year, month, date)
    const isCurrentMonth = cellDate.getMonth() === month
    cells.push({
      date: cellDate.getDate(),
      isCurrentMonth,
      isToday: toDateStr(cellDate) === todayStr,
      fullDate: toDateStr(cellDate)
    })
  }
  return cells
})

type ListItem =
  | { key: string; kind: 'schedule'; time: number; timeLabel: string; payload: ScheduleVO }
  | { key: string; kind: 'task'; time: number; timeLabel: string; payload: Task }

function getDayHeader(dateStr: string): string {
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = d.getMonth() + 1
  const day = d.getDate()
  const dayName = dayNames[d.getDay()] ?? ''
  const isToday = toDateStr(d) === toDateStr(new Date())
  return `${y}年${m}月${day}日，${dayName}${isToday ? ' (今天)' : ''}`
}

const listGroups = computed(() => {
  const groups = new Map<string, ListItem[]>()

  for (const e of schedules.value) {
    const dateStr = toDateStr(new Date(e.startTime))
    const items = groups.get(dateStr) ?? []
    items.push({
      key: `schedule-${e.scheduleId}`,
      kind: 'schedule',
      time: new Date(e.startTime).getTime(),
      timeLabel: formatTime(e.startTime),
      payload: e
    })
    groups.set(dateStr, items)
  }

  for (const t of tasks.value) {
    const dateStr = normalizeDueDate(t.dueDate ?? '')
    if (!dateStr) continue
    const items = groups.get(dateStr) ?? []
    const dueTs = t.dueDate ? new Date(t.dueDate).getTime() : new Date(`${dateStr}T23:59:59`).getTime()
    items.push({
      key: `task-${t.taskId}`,
      kind: 'task',
      time: Number.isFinite(dueTs) ? dueTs : new Date(`${dateStr}T23:59:59`).getTime(),
      timeLabel: '待办',
      payload: t
    })
    groups.set(dateStr, items)
  }

  return Array.from(groups.entries())
    .map(([dateStr, items]) => ({
      dateStr,
      header: getDayHeader(dateStr),
      lunar: getLunarText(dateStr),
      items: items.slice().sort((a, b) => a.time - b.time)
    }))
    .sort((a, b) => new Date(a.dateStr).getTime() - new Date(b.dateStr).getTime())
})
</script>
