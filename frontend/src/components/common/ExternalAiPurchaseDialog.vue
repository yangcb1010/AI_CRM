<template>
  <el-dialog
    :model-value="modelValue"
    width="820px"
    class="external-ai-purchase-dialog"
    align-center
    :close-on-click-modal="!creatingOrder && !refreshingOrder"
    @update:model-value="handleVisibleChange"
  >
    <template #header>
      <div class="flex items-center gap-3">
        <span class="flex size-10 items-center justify-center overflow-hidden rounded-lg bg-white shadow-sm ring-1 ring-slate-100">
          <img src="/logo.png" alt="" class="size-full object-cover" />
        </span>
        <div class="min-w-0">
          <p class="truncate text-base font-semibold text-slate-950">悟空云 AI 额度</p>
          <p class="mt-1 text-xs text-slate-500">购买成功后立即进入远端额度账本</p>
        </div>
      </div>
    </template>

    <div class="space-y-4">
      <section class="grid gap-3 sm:grid-cols-3">
        <div class="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
          <p class="text-xs font-medium text-slate-500">可用额度</p>
          <p class="mt-2 text-xl font-semibold tabular-nums text-slate-950">{{ formatCredit(usage?.creditRemaining ?? options?.creditRemaining ?? 0) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-4 py-3">
          <p class="text-xs font-medium text-slate-500">购买剩余</p>
          <p class="mt-2 text-xl font-semibold tabular-nums text-slate-950">{{ formatCredit(usage?.purchasedCreditRemaining ?? options?.purchasedCreditRemaining ?? 0) }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white px-4 py-3">
          <p class="text-xs font-medium text-slate-500">预留中</p>
          <p class="mt-2 text-xl font-semibold tabular-nums text-slate-950">{{ formatCredit(usage?.reservedCreditUsed ?? options?.reservedCreditUsed ?? 0) }}</p>
        </div>
      </section>

      <section class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_292px]">
        <div class="space-y-4">
          <div class="rounded-lg border border-slate-200 bg-white p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="text-sm font-semibold text-slate-900">额度套餐</p>
              <button
                type="button"
                class="inline-flex h-8 items-center rounded-md border border-slate-200 bg-white px-3 text-xs font-medium text-slate-600 transition hover:border-blue-300 hover:text-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="loading"
                @click="loadBillingData"
              >
                {{ loading ? '刷新中' : '刷新' }}
              </button>
            </div>

            <div v-if="plans.length" class="grid gap-3 sm:grid-cols-2">
              <button
                v-for="plan in plans"
                :key="plan.id"
                type="button"
                class="min-h-[116px] rounded-lg border px-4 py-3 text-left transition-all"
                :class="selectedPlanId === plan.id
                  ? 'border-blue-500 bg-blue-50 text-blue-700 shadow-sm ring-2 ring-blue-100'
                  : 'border-slate-200 bg-white text-slate-700 hover:border-blue-300 hover:bg-slate-50'"
                @click="selectedPlanId = plan.id"
              >
                <span class="block text-lg font-semibold tabular-nums text-slate-950">{{ formatCredit(plan.creditAmount) }} 积分</span>
                <span class="mt-1 block text-xs text-slate-500">{{ planSubtitle(plan) }}</span>
                <span class="mt-4 block text-xl font-semibold tabular-nums text-slate-950">{{ priceText(plan.priceFen) }}</span>
              </button>
            </div>
            <div v-else class="rounded-lg border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
              暂无可购买套餐
            </div>
          </div>

          <div class="rounded-lg border border-slate-200 bg-white p-4">
            <p class="mb-3 text-sm font-semibold text-slate-900">支付方式</p>
            <div v-if="channelOptions.length" class="grid gap-2 sm:grid-cols-2">
              <button
                v-for="channel in channelOptions"
                :key="`${channel.code}-${channel.normalizedCode}`"
                type="button"
                class="flex h-11 items-center justify-between rounded-lg border px-3 text-sm font-medium transition-all disabled:cursor-not-allowed disabled:opacity-50"
                :class="selectedChannel === channel.normalizedCode
                  ? 'border-blue-500 bg-blue-50 text-blue-700 ring-2 ring-blue-100'
                  : 'border-slate-200 bg-white text-slate-700 hover:border-blue-300 hover:bg-slate-50'"
                :disabled="!channel.enabled"
                :title="channel.unavailableReason || channel.displayLabel"
                @click="selectedChannel = channel.normalizedCode"
              >
                <span class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-[18px]">{{ channelIcon(channel.normalizedCode) }}</span>
                  {{ channel.displayLabel }}
                </span>
                <span v-if="!channel.enabled" class="text-xs font-normal text-slate-400">不可用</span>
              </button>
            </div>
            <div v-else class="rounded-lg border border-dashed border-slate-200 bg-slate-50 px-4 py-6 text-center text-sm text-slate-500">
              当前没有可用支付方式
            </div>
          </div>
        </div>

        <aside class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <div class="space-y-4">
            <div>
              <p class="text-sm font-semibold text-slate-900">本次购买</p>
              <div class="mt-3 rounded-lg border border-slate-200 bg-white p-3">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="truncate text-sm font-semibold text-slate-950">
                      {{ selectedPlan ? `${formatCredit(selectedPlan.creditAmount)} 积分` : '未选择套餐' }}
                    </p>
                    <p class="mt-1 truncate text-xs text-slate-500">
                      {{ selectedChannelOption ? selectedChannelOption.displayLabel : '未选择支付方式' }}
                    </p>
                  </div>
                  <p class="shrink-0 text-base font-semibold tabular-nums text-slate-950">
                    {{ selectedPlan ? priceText(selectedPlan.priceFen) : '-' }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="currentOrder" class="space-y-3">
              <div class="flex items-center justify-between gap-3">
                <div class="min-w-0">
                  <p class="truncate text-xs text-slate-500">{{ currentOrder.orderNo }}</p>
                  <p class="mt-1 text-sm font-semibold text-slate-900">{{ currentOrder.amountDisplay }}</p>
                </div>
                <el-tag :type="currentOrder.status === 'PAID' ? 'success' : currentOrder.status === 'PENDING' ? 'warning' : 'info'">
                  {{ statusText(currentOrder.status) }}
                </el-tag>
              </div>

              <div v-if="currentOrder.paymentMode === 'qrcode' && currentOrder.qrCodeImage" class="rounded-lg border border-slate-200 bg-white p-4 text-center">
                <img :src="currentOrder.qrCodeImage" alt="支付二维码" class="mx-auto size-48" />
                <p class="mt-2 text-xs text-slate-500">请使用对应支付 App 扫码</p>
              </div>
              <iframe
                v-else-if="currentOrder.paymentMode === 'page' && currentOrder.paymentFormHtml"
                class="h-64 w-full rounded-lg border border-slate-200 bg-white"
                sandbox="allow-forms allow-scripts allow-same-origin"
                :srcdoc="currentOrder.paymentFormHtml"
              />
              <div v-else class="flex h-44 items-center justify-center rounded-lg border border-dashed border-slate-300 bg-white text-sm text-slate-400">
                支付载荷生成中
              </div>

              <button
                type="button"
                class="inline-flex h-9 w-full items-center justify-center rounded-md border border-slate-200 bg-white px-3 text-sm font-medium text-slate-700 transition hover:border-blue-300 hover:text-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                :disabled="refreshingOrder"
                @click="refreshCurrentOrder"
              >
                {{ refreshingOrder ? '刷新中...' : '刷新支付状态' }}
              </button>
            </div>

            <div class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs leading-5 text-slate-500">
              {{ purchaseHint }}
            </div>

            <button
              type="button"
              class="inline-flex h-11 w-full items-center justify-center rounded-md px-4 text-sm font-semibold transition disabled:cursor-not-allowed disabled:bg-slate-200 disabled:text-slate-500"
              :class="creatingOrder || loading ? 'bg-slate-200 text-slate-500' : 'bg-blue-600 text-white hover:bg-blue-700'"
              :disabled="creatingOrder || loading"
              @click="createOrder"
            >
              {{ creatingOrder ? '生成中...' : currentOrder ? '重新生成支付单' : '生成支付单' }}
            </button>
          </div>
        </aside>
      </section>

      <section v-if="recentOrders.length" class="rounded-lg border border-slate-200 bg-white">
        <div class="border-b border-slate-100 px-4 py-3 text-sm font-semibold text-slate-900">最近订单</div>
        <div class="divide-y divide-slate-100">
          <div v-for="order in recentOrders.slice(0, 4)" :key="order.orderNo" class="flex items-center justify-between gap-3 px-4 py-3 text-sm">
            <div class="min-w-0">
              <p class="truncate font-medium text-slate-800">{{ order.planName }}</p>
              <p class="mt-1 truncate text-xs text-slate-500">{{ order.orderNo }}</p>
            </div>
            <div class="shrink-0 text-right">
              <p class="font-semibold tabular-nums text-slate-900">{{ order.amountDisplay }}</p>
              <p class="mt-1 text-xs text-slate-500">{{ statusText(order.status) }}</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createExternalAiPurchaseOrder,
  getExternalAiPurchaseOrder,
  getExternalAiPurchaseOptions,
  getExternalAiUsage,
  listExternalAiPurchaseOrders
} from '@/api/systemConfig'
import type {
  ExternalAiPaymentChannel,
  ExternalAiPurchaseChannel,
  ExternalAiPurchaseOptions,
  ExternalAiPurchaseOrder,
  ExternalAiPurchaseOrderStatus,
  ExternalAiPurchasePlan,
  ExternalAiUsage
} from '@/types/systemConfig'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'paid'): void
}>()

type ChannelOption = ExternalAiPurchaseChannel & {
  normalizedCode: ExternalAiPaymentChannel
  displayLabel: string
}

const numberFormatter = new Intl.NumberFormat('zh-CN')
const loading = ref(false)
const creatingOrder = ref(false)
const refreshingOrder = ref(false)
const options = ref<ExternalAiPurchaseOptions | null>(null)
const usage = ref<ExternalAiUsage | null>(null)
const currentOrder = ref<ExternalAiPurchaseOrder | null>(null)
const recentOrders = ref<ExternalAiPurchaseOrder[]>([])
const selectedPlanId = ref('')
const selectedChannel = ref<ExternalAiPaymentChannel>('')
let pollingTimer: number | null = null

const plans = computed(() => options.value?.plans || [])
const channels = computed(() => options.value?.channels || [])
const channelOptions = computed<ChannelOption[]>(() => channels.value.map((channel) => ({
  ...channel,
  normalizedCode: normalizePaymentChannel(channel.code),
  displayLabel: channelDisplayLabel(channel)
})))
const selectedPlan = computed(() => plans.value.find((plan) => plan.id === selectedPlanId.value) || null)
const selectedChannelOption = computed(() => channelOptions.value.find((channel) => (
  channel.enabled && channel.normalizedCode === selectedChannel.value
)) || null)
const purchaseDisabledReason = computed(() => {
  if (loading.value && !options.value) return '正在加载购买配置'
  if (!options.value) return '购买配置尚未加载'
  if (!options.value.enabled) return '额度购买暂未开放'
  if (!plans.value.length) return '当前没有可购买套餐'
  if (!selectedPlan.value) return '请选择额度套餐'
  if (!channelOptions.value.length) return '当前没有可用支付方式'
  if (!selectedChannelOption.value) return '请选择可用的支付方式'
  return ''
})
const purchaseHint = computed(() => {
  if (purchaseDisabledReason.value) return purchaseDisabledReason.value
  if (currentOrder.value?.status === 'PENDING') return '支付完成后会自动刷新到账状态，也可以手动刷新。'
  return '创建支付单后按支付平台返回结果入账。'
})

watch(() => props.modelValue, (visible) => {
  if (visible) {
    loadBillingData()
  } else {
    stopPolling()
  }
})

onBeforeUnmount(stopPolling)

function handleVisibleChange(value: boolean) {
  emit('update:modelValue', value)
}

async function loadBillingData() {
  loading.value = true
  try {
    const [nextOptions, nextUsage, nextOrders] = await Promise.all([
      getExternalAiPurchaseOptions(),
      getExternalAiUsage(20),
      listExternalAiPurchaseOrders(10)
    ])
    options.value = nextOptions
    usage.value = nextUsage
    recentOrders.value = nextOrders

    if (!nextOptions.plans.some((plan) => plan.id === selectedPlanId.value)) {
      selectedPlanId.value = nextOptions.plans[0]?.id || ''
    }

    const selectedStillEnabled = nextOptions.channels.some((channel) => (
      channel.enabled && normalizePaymentChannel(channel.code) === selectedChannel.value
    ))
    if (!selectedStillEnabled) {
      const enabledChannel = nextOptions.channels.find((channel) => channel.enabled)
      selectedChannel.value = enabledChannel ? normalizePaymentChannel(enabledChannel.code) : ''
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function createOrder() {
  if (creatingOrder.value || loading.value) return
  const reason = purchaseDisabledReason.value
  if (reason) {
    ElMessage.warning(reason)
    return
  }
  const plan = selectedPlan.value
  const channel = selectedChannelOption.value
  if (!plan || !channel) {
    ElMessage.warning('请选择套餐和支付方式')
    return
  }

  creatingOrder.value = true
  try {
    currentOrder.value = await createExternalAiPurchaseOrder({
      planId: plan.id,
      paymentChannel: channel.normalizedCode
    })
    startPolling()
  } catch {
    // handled by interceptor
  } finally {
    creatingOrder.value = false
  }
}

async function refreshCurrentOrder() {
  if (!currentOrder.value?.orderNo) return
  refreshingOrder.value = true
  try {
    const nextOrder = await getExternalAiPurchaseOrder(currentOrder.value.orderNo)
    currentOrder.value = nextOrder
    if (nextOrder.status === 'PAID') {
      stopPolling()
      ElMessage.success('支付成功，额度已到账')
      await loadBillingData()
      emit('paid')
    }
  } catch {
    // handled by interceptor
  } finally {
    refreshingOrder.value = false
  }
}

function startPolling() {
  stopPolling()
  pollingTimer = window.setInterval(() => {
    if (currentOrder.value?.status === 'PENDING') {
      refreshCurrentOrder()
    }
  }, 3000)
}

function stopPolling() {
  if (pollingTimer !== null) {
    window.clearInterval(pollingTimer)
    pollingTimer = null
  }
}

function normalizePaymentChannel(code: string | null | undefined): ExternalAiPaymentChannel {
  return String(code || '').trim().toLowerCase()
}

function channelDisplayLabel(channel: ExternalAiPurchaseChannel) {
  const code = normalizePaymentChannel(channel.code)
  const label = String(channel.label || '').trim()
  if (code === 'wechat' || label.toLowerCase() === 'wechat pay') return '微信支付'
  if (code === 'alipay' || label.toLowerCase() === 'alipay') return '支付宝'
  return label || channel.code
}

function channelIcon(code: string) {
  if (code === 'wechat') return 'qr_code_2'
  if (code === 'alipay') return 'account_balance_wallet'
  return 'payments'
}

function planSubtitle(plan: ExternalAiPurchasePlan) {
  const description = String(plan.description || '').trim()
  if (description) return description
  const name = String(plan.name || '').trim()
  if (!name || /credits?/i.test(name)) return '标准额度包'
  return name
}

function formatCredit(value: number) {
  return numberFormatter.format(Number(value || 0))
}

function priceText(priceFen: number) {
  return `¥${(Number(priceFen || 0) / 100).toFixed(2)}`
}

function statusText(status: ExternalAiPurchaseOrderStatus) {
  const map: Record<ExternalAiPurchaseOrderStatus, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    FAILED: '失败',
    CLOSED: '已关闭',
    EXPIRED: '已过期'
  }
  return map[status] || status
}
</script>

<style scoped>
.external-ai-purchase-dialog :deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

.external-ai-purchase-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px 12px;
  border-bottom: 1px solid #e2e8f0;
}

.external-ai-purchase-dialog :deep(.el-dialog__body) {
  padding: 16px 24px 24px;
}

@media (max-width: 768px) {
  .external-ai-purchase-dialog :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
  }

  .external-ai-purchase-dialog :deep(.el-dialog__header),
  .external-ai-purchase-dialog :deep(.el-dialog__body) {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
