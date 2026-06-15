<template>
  <div class="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
    <div class="w-full max-w-md">
      <div class="mb-8 text-center">
        <h1 class="text-3xl font-bold text-gray-900">AI CRM</h1>
        <p class="mt-2 text-gray-600">{{ copy.subtitle }}</p>
      </div>

      <el-card class="shadow-lg">
        <template #header>
          <span class="text-lg font-semibold">{{ copy.login }}</span>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleLogin"
        >
          <el-form-item :label="copy.username" prop="username">
            <el-input
              v-model="formData.username"
              :placeholder="copy.usernamePlaceholder"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item :label="copy.password" prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              :placeholder="copy.passwordPlaceholder"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="w-full"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? copy.loggingIn : copy.login }}
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="enabledExternalProviders.length" class="mt-5">
          <div class="mb-3 flex items-center gap-3 text-xs font-semibold uppercase text-gray-400">
            <span class="h-px flex-1 bg-gray-200"></span>
            <span>{{ copy.externalLogin }}</span>
            <span class="h-px flex-1 bg-gray-200"></span>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <el-button
              v-for="provider in enabledExternalProviders"
              :key="provider.provider"
              class="!ml-0"
              :loading="externalLoadingProvider === provider.provider"
              @click="startExternalLogin(provider.provider)"
            >
              <span class="mr-2 inline-flex h-5 w-5 items-center justify-center rounded-full bg-slate-100 text-xs font-bold text-primary">
                {{ providerMark(provider.provider) }}
              </span>
              {{ provider.name }}
            </el-button>
          </div>
        </div>

        <div class="mt-4 text-center text-sm text-gray-500">
          <p>{{ copy.testAccount }}</p>
        </div>
      </el-card>

      <SliderCaptchaDialog
        v-model="showCaptchaDialog"
        @verified="handleCaptchaVerified"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import {
  exchangeExternalLoginTicket,
  getExternalAuthAuthorizeUrl,
  getExternalAuthProviders,
  getOidcSessionToken
} from '@/api/auth'
import { useUserStore } from '@/stores/user'
import SliderCaptchaDialog from '@/components/auth/SliderCaptchaDialog.vue'
import type { ExternalAuthProvider, ExternalAuthProviderCode } from '@/types/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const showCaptchaDialog = ref(false)
const externalProviders = ref<ExternalAuthProvider[]>([])
const externalLoadingProvider = ref<ExternalAuthProviderCode | ''>('')

const formData = reactive({
  username: '',
  password: ''
})

const copy = {
  subtitle: '\u667a\u80fd\u5ba2\u6237\u5173\u7cfb\u7ba1\u7406\u7cfb\u7edf',
  login: '\u767b\u5f55',
  loggingIn: '\u767b\u5f55\u4e2d...',
  username: '\u7528\u6237\u540d',
  usernamePlaceholder: '\u8bf7\u8f93\u5165\u7528\u6237\u540d',
  password: '\u5bc6\u7801',
  passwordPlaceholder: '\u8bf7\u8f93\u5165\u5bc6\u7801',
  passwordMin: '\u5bc6\u7801\u81f3\u5c116\u4f4d',
  loginSuccess: '\u767b\u5f55\u6210\u529f',
  externalLogin: '\u5916\u90e8\u767b\u5f55',
  testAccount: '\u6d4b\u8bd5\u8d26\u53f7: admin / 123456a'
}

const rules: FormRules = {
  username: [{ required: true, message: copy.usernamePlaceholder, trigger: 'blur' }],
  password: [
    { required: true, message: copy.passwordPlaceholder, trigger: 'blur' },
    { min: 6, message: copy.passwordMin, trigger: 'blur' }
  ]
}

const enabledExternalProviders = computed(() => externalProviders.value.filter((provider) => provider.enabled))

onMounted(async () => {
  await handleExternalAuthQuery()
  await loadExternalProviders()
})

async function handleLogin() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    showCaptchaDialog.value = true
  } catch (error) {
    console.error('Login validation error:', error)
  }
}

async function handleCaptchaVerified(captchaVerification: string) {
  try {
    loading.value = true
    await userStore.login({
      username: formData.username,
      password: formData.password,
      captchaVerification
    })

    ElMessage.success(copy.loginSuccess)
    await completeLoginRedirect()
  } catch (error) {
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}

function providerMark(provider: ExternalAuthProviderCode): string {
  if (provider === 'google') return 'G'
  if (provider === 'wechat') return 'W'
  return '?'
}

async function loadExternalProviders() {
  try {
    externalProviders.value = await getExternalAuthProviders()
  } catch (error) {
    console.error('Load external auth providers failed:', error)
    externalProviders.value = []
  }
}

function buildExternalAuthRedirect(): string {
  const query: Record<string, string> = {}
  if (typeof route.query.redirect === 'string') {
    query.redirect = route.query.redirect
  }
  const resolved = router.resolve({ name: 'Login', query })
  return `${window.location.origin}${window.location.pathname}${window.location.search}${resolved.href}`
}

async function startExternalLogin(provider: ExternalAuthProviderCode) {
  externalLoadingProvider.value = provider
  try {
    const { authorizeUrl } = await getExternalAuthAuthorizeUrl(provider, buildExternalAuthRedirect())
    window.location.href = authorizeUrl
  } catch (error) {
    console.error('Start external login failed:', error)
  } finally {
    externalLoadingProvider.value = ''
  }
}

async function handleExternalAuthQuery() {
  const externalError = typeof route.query.externalAuthError === 'string' ? route.query.externalAuthError : ''
  if (externalError) {
    const messageMap: Record<string, string> = {
      unbound: 'No local account is bound to this external account',
      invalid_state: 'External login state is expired',
      denied: 'External login was cancelled',
      failed: 'External login failed'
    }
    ElMessage.error(messageMap[externalError] || 'External login failed')
    await clearExternalAuthQuery()
    return
  }

  const loginTicket = typeof route.query.externalLoginTicket === 'string' ? route.query.externalLoginTicket : ''
  if (!loginTicket) return

  loading.value = true
  try {
    const result = await exchangeExternalLoginTicket({ ticket: loginTicket })
    await userStore.applyLoginResult(result)
    await clearExternalAuthQuery()
    ElMessage.success(copy.loginSuccess)
    await completeLoginRedirect()
  } catch (error) {
    console.error('External login ticket exchange failed:', error)
  } finally {
    loading.value = false
  }
}

async function clearExternalAuthQuery() {
  const query = { ...route.query }
  delete query.externalAuthError
  delete query.externalLoginTicket
  delete query.provider
  await router.replace({ path: route.path, query })
}

async function completeLoginRedirect() {
  let redirect = (route.query.redirect as string) || '/'
  if (redirect.includes('/oauth2/authorize')) {
    try {
      const { sessionToken } = await getOidcSessionToken()
      const url = new URL(redirect)
      url.searchParams.set('session_token', sessionToken)
      redirect = url.toString()
    } catch (e) {
      console.error('Failed to get OIDC session token:', e)
    }
  }

  if (redirect.startsWith('http://') || redirect.startsWith('https://')) {
    window.location.href = redirect
  } else {
    await router.push(redirect)
  }
}
</script>

<style scoped>
.el-card {
  border-radius: 12px;
}
</style>
