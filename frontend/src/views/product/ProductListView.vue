<template>
  <div class="flex h-full flex-col gap-5 bg-slate-50 px-4 py-5 md:px-8">
    <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
      <div class="min-w-0">
        <h1 class="text-xl font-bold text-slate-900">产品管理</h1>
        <p class="mt-1 text-sm text-slate-500">共 {{ total }} 个产品</p>
      </div>

      <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索产品名称、编码、类型或描述"
            class="h-10 w-full rounded-lg border border-slate-200 bg-white pl-10 pr-4 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20 lg:w-80"
            @input="debouncedLoadProducts"
            @keydown.enter="loadProducts"
          />
        </div>
        <select
          v-model="status"
          class="h-10 rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-600 outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
          @change="applyFilters"
        >
          <option value="">全部状态</option>
          <option value="active">启用</option>
          <option value="inactive">停用</option>
        </select>
        <select
          v-model="categoryId"
          class="h-10 rounded-lg border border-slate-200 bg-white px-3 text-sm text-slate-600 outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
          @change="applyFilters"
        >
          <option value="">全部类目</option>
          <option v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
        </select>
        <button
          type="button"
          class="inline-flex h-10 items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-3 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          @click="createCategory"
        >
          <span class="material-symbols-outlined text-[18px] leading-none">create_new_folder</span>
          新增类目
        </button>
        <button
          type="button"
          class="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-primary px-4 text-sm font-bold text-white shadow-sm transition hover:bg-primary/90"
          @click="openCreateDialog"
        >
          <span class="material-symbols-outlined text-[18px] leading-none">add_box</span>
          新建产品
        </button>
      </div>
    </div>

    <div class="min-h-0 flex-1 overflow-hidden rounded-lg border border-slate-200 bg-white" v-loading="loading">
      <el-table
        v-if="!isMobile"
        :data="products"
        height="100%"
        row-key="productId"
        table-layout="fixed"
        empty-text="暂无产品"
        @row-click="openEditDialog"
      >
        <el-table-column label="产品" min-width="250">
          <template #default="{ row }">
            <div class="flex min-w-0 items-center gap-3">
              <div class="flex size-9 shrink-0 items-center justify-center overflow-hidden rounded-lg border border-slate-200 bg-white text-slate-400">
                <img v-if="row.mainImageUrl" :src="row.mainImageUrl" class="size-full object-cover" alt="product" />
                <span v-else class="material-symbols-outlined text-[20px]">inventory_2</span>
              </div>
              <div class="min-w-0">
                <p class="truncate text-sm font-semibold text-slate-900">{{ row.productName }}</p>
                <p class="truncate text-xs text-slate-400">{{ row.productCode || '无编码' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类目" min-width="170">
          <template #default="{ row }">
            <span class="block truncate text-sm text-slate-600">{{ row.categoryPath || row.categoryName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型/单位" width="130">
          <template #default="{ row }">
            <div class="text-sm text-slate-700">{{ productTypeLabel(row.productType) }}</div>
            <div class="text-xs text-slate-400">{{ row.unit || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="标准价" width="130" align="right">
          <template #default="{ row }">
            <span class="font-mono text-sm text-slate-700">{{ money(row.standardPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="负责人" width="130">
          <template #default="{ row }">
            <span class="block truncate text-sm text-slate-600">{{ row.ownerName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium" :class="statusClass(row.status)">
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">
            <span class="text-sm text-slate-500">{{ formatDateTime(row.updateTime || row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1" @click.stop>
              <button class="product-icon-button" title="编辑" type="button" @click="openEditDialog(row)">
                <span class="material-symbols-outlined text-[18px] leading-none">edit</span>
              </button>
              <button class="product-icon-button" :title="row.status === 'active' ? '停用' : '启用'" type="button" @click="toggleStatus(row)">
                <span class="material-symbols-outlined text-[18px] leading-none">{{ row.status === 'active' ? 'toggle_off' : 'toggle_on' }}</span>
              </button>
              <button class="product-icon-button text-rose-500 hover:bg-rose-50" title="删除" type="button" @click="handleDelete(row)">
                <span class="material-symbols-outlined text-[18px] leading-none">delete</span>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-else class="h-full overflow-y-auto px-3 py-3">
        <div v-if="products.length === 0" class="py-16 text-center text-slate-400">
          <span class="material-symbols-outlined text-5xl">inventory_2</span>
          <p class="mt-3 text-sm">{{ keyword.trim() ? '未找到匹配产品' : '暂无产品' }}</p>
        </div>
        <div v-else class="flex flex-col gap-3">
          <button
            v-for="product in products"
            :key="product.productId"
            type="button"
            class="w-full rounded-lg border border-slate-200 bg-white px-4 py-3 text-left transition active:bg-slate-100"
            @click="openEditDialog(product)"
          >
            <div class="flex items-start gap-3">
              <div class="flex size-10 shrink-0 items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-400">
                <span class="material-symbols-outlined text-[20px]">inventory_2</span>
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex items-start gap-2">
                  <div class="min-w-0 flex-1">
                    <div class="truncate text-sm font-bold text-slate-900">{{ product.productName }}</div>
                    <p class="mt-0.5 truncate text-xs text-slate-400">{{ product.categoryPath || product.categoryName || '未分类' }}</p>
                  </div>
                  <span class="inline-flex shrink-0 items-center rounded-full px-2 py-0.5 text-xs font-medium" :class="statusClass(product.status)">
                    {{ statusLabel(product.status) }}
                  </span>
                </div>
                <div class="mt-3 grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <div class="text-[11px] font-bold text-slate-400">编码</div>
                    <div class="truncate text-slate-600">{{ product.productCode || '-' }}</div>
                  </div>
                  <div>
                    <div class="text-[11px] font-bold text-slate-400">标准价</div>
                    <div class="truncate font-mono text-slate-600">{{ money(product.standardPrice) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="flex items-center justify-between text-sm text-slate-500">
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <div class="flex items-center gap-2">
        <button class="rounded-lg border border-slate-200 bg-white px-3 py-2 disabled:opacity-40" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <button class="rounded-lg border border-slate-200 bg-white px-3 py-2 disabled:opacity-40" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :fullscreen="isMobile"
      :width="isMobile ? '100%' : '600px'"
      :title="editingProduct ? '编辑产品' : '新建产品'"
      destroy-on-close
    >
      <el-form label-position="top" @submit.prevent>
        <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
          <el-form-item label="产品名称" required>
            <el-input v-model="form.productName" maxlength="255" placeholder="请输入产品名称" />
          </el-form-item>
          <el-form-item label="产品编码">
            <el-input v-model="form.productCode" maxlength="100" placeholder="选填，需唯一" />
          </el-form-item>
          <el-form-item label="产品类目">
            <el-select v-model="form.categoryId" class="w-full" clearable placeholder="默认未分类">
              <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="产品类型">
            <el-select v-model="form.productType" class="w-full" placeholder="请选择">
              <el-option label="实物" value="goods" />
              <el-option label="服务" value="service" />
              <el-option label="订阅" value="subscription" />
            </el-select>
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="form.unit" maxlength="50" placeholder="个、套、台、年..." />
          </el-form-item>
          <el-form-item label="标准价">
            <el-input v-model="form.standardPrice" placeholder="0.00" />
          </el-form-item>
          <el-form-item label="成本价">
            <el-input v-model="form.costPrice" placeholder="0.00" />
          </el-form-item>
        </div>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="补充产品卖点、适用场景或注意事项" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <button type="button" class="rounded-lg px-4 py-2 text-sm font-medium text-slate-500 hover:bg-slate-100" @click="dialogVisible = false">取消</button>
          <button
            type="button"
            class="rounded-lg bg-primary px-4 py-2 text-sm font-bold text-white disabled:opacity-50"
            :disabled="saving"
            @click="saveProduct"
          >
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addProduct,
  addProductCategory,
  deleteProduct,
  getProductCategoryTree,
  queryProductList,
  updateProduct,
  updateProductStatus
} from '@/api/product'
import { useResponsive } from '@/composables/useResponsive'
import { isRequestErrorHandled } from '@/utils/requestError'
import type { ProductAddBO, ProductCategoryVO, ProductStatus, ProductVO } from '@/types/product'

type Option = {
  label: string
  value: string
}

const { isMobile } = useResponsive()
const products = ref<ProductVO[]>([])
const categories = ref<ProductCategoryVO[]>([])
const loading = ref(false)
const keyword = ref('')
const status = ref<ProductStatus | ''>('')
const categoryId = ref('')
const page = ref(1)
const limit = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const saving = ref(false)
const editingProduct = ref<ProductVO | null>(null)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const form = reactive<ProductAddBO & { productId?: string }>({
  productName: '',
  productCode: '',
  categoryId: '',
  productType: 'goods',
  unit: '',
  standardPrice: '',
  costPrice: '',
  description: ''
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / limit.value)))
const categoryOptions = computed<Option[]>(() => flattenCategories(categories.value))

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})

async function loadProducts() {
  loading.value = true
  try {
    const result = await queryProductList({
      keyword: keyword.value.trim() || undefined,
      status: status.value,
      categoryId: categoryId.value || undefined,
      includeChildCategory: true,
      page: page.value,
      limit: limit.value
    })
    products.value = result.list || []
    total.value = result.totalRow || 0
  } catch (error) {
    if (!isRequestErrorHandled(error)) {
      ElMessage.error('加载产品列表失败')
    }
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = await getProductCategoryTree()
  } catch (error) {
    if (!isRequestErrorHandled(error)) {
      ElMessage.error('加载产品类目失败')
    }
  }
}

function debouncedLoadProducts() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadProducts()
  }, 300)
}

function applyFilters() {
  page.value = 1
  loadProducts()
}

function changePage(nextPage: number) {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  loadProducts()
}

function openCreateDialog() {
  editingProduct.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(product: ProductVO) {
  editingProduct.value = product
  form.productId = product.productId
  form.productName = product.productName || ''
  form.productCode = product.productCode || ''
  form.categoryId = product.categoryId ? String(product.categoryId) : ''
  form.productType = product.productType || 'goods'
  form.unit = product.unit || ''
  form.standardPrice = product.standardPrice ?? ''
  form.costPrice = product.costPrice ?? ''
  form.description = product.description || ''
  dialogVisible.value = true
}

function resetForm() {
  form.productId = undefined
  form.productName = ''
  form.productCode = ''
  form.categoryId = ''
  form.productType = 'goods'
  form.unit = ''
  form.standardPrice = ''
  form.costPrice = ''
  form.description = ''
}

async function saveProduct() {
  if (!form.productName.trim()) {
    ElMessage.warning('请输入产品名称')
    return
  }

  saving.value = true
  const payload: ProductAddBO = {
    productName: form.productName.trim(),
    productCode: form.productCode?.trim() || undefined,
    categoryId: form.categoryId || undefined,
    productType: form.productType || 'goods',
    unit: form.unit?.trim() || undefined,
    standardPrice: normalizeAmount(form.standardPrice),
    costPrice: normalizeAmount(form.costPrice),
    description: form.description?.trim() || undefined
  }

  try {
    if (editingProduct.value && form.productId) {
      await updateProduct({ ...payload, productId: form.productId })
      ElMessage.success('产品已更新')
    } else {
      await addProduct(payload)
      ElMessage.success('产品已创建')
    }
    dialogVisible.value = false
    await loadProducts()
  } catch (error) {
    if (!isRequestErrorHandled(error)) {
      ElMessage.error('保存产品失败')
    }
  } finally {
    saving.value = false
  }
}

async function toggleStatus(product: ProductVO) {
  const nextStatus: ProductStatus = product.status === 'active' ? 'inactive' : 'active'
  try {
    await updateProductStatus({ productId: product.productId, status: nextStatus })
    ElMessage.success(nextStatus === 'active' ? '产品已启用' : '产品已停用')
    await loadProducts()
  } catch (error) {
    if (!isRequestErrorHandled(error)) {
      ElMessage.error('更新产品状态失败')
    }
  }
}

async function handleDelete(product: ProductVO) {
  try {
    await ElMessageBox.confirm(`确定删除“${product.productName}”？`, '删除产品', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProduct(product.productId)
    ElMessage.success('产品已删除')
    await loadProducts()
  } catch (error) {
    if (!isRequestErrorHandled(error) && error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除产品失败')
    }
  }
}

async function createCategory() {
  try {
    const result = await ElMessageBox.prompt('请输入类目名称', '新增类目', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputValidator: value => !!value?.trim() || '类目名称不能为空'
    })
    const categoryName = result.value.trim()
    await addProductCategory({ categoryName })
    ElMessage.success('类目已创建')
    await loadCategories()
  } catch (error) {
    if (!isRequestErrorHandled(error) && error !== 'cancel' && error !== 'close') {
      ElMessage.error('创建类目失败')
    }
  }
}

function flattenCategories(items: ProductCategoryVO[], level = 0): Option[] {
  return items.flatMap(item => {
    const option = {
      label: `${'　'.repeat(level)}${item.categoryName}`,
      value: String(item.categoryId)
    }
    return [option, ...flattenCategories(item.children || [], level + 1)]
  })
}

function normalizeAmount(value?: number | string) {
  if (value === undefined || value === null || value === '') return undefined
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : undefined
}

function statusLabel(value?: string) {
  return value === 'inactive' ? '停用' : '启用'
}

function statusClass(value?: string) {
  return value === 'inactive'
    ? 'bg-slate-100 text-slate-500'
    : 'bg-emerald-50 text-emerald-600'
}

function productTypeLabel(value?: string) {
  if (value === 'service') return '服务'
  if (value === 'subscription') return '订阅'
  return '实物'
}

function money(value?: number | string) {
  if (value === undefined || value === null || value === '') return '-'
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) return String(value)
  return numeric.toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })
}

function formatDateTime(value?: string) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.product-icon-button {
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: rgb(100 116 139);
  transition: background-color 0.15s ease, color 0.15s ease;
}

.product-icon-button:hover {
  background: rgb(241 245 249);
  color: rgb(15 23 42);
}
</style>
