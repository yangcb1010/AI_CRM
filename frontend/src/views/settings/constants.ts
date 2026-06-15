import type { SettingsMainTab, SettingsTabItem, SystemSettingsTab } from './types'

export const SETTINGS_MAIN_TABS: SettingsTabItem<SettingsMainTab>[] = [
  { value: 'team', label: '\u7ec4\u7ec7\u5458\u5de5\u7ba1\u7406' },
  { value: 'role', label: '\u89d2\u8272\u6743\u9650\u7ba1\u7406' },
  { value: 'system', label: '\u7cfb\u7edf\u53c2\u6570\u8bbe\u7f6e' }
]

export const SYSTEM_SETTINGS_TABS: SettingsTabItem<SystemSettingsTab>[] = [
  { value: 'enterprise', label: '\u4f01\u4e1a\u4fe1\u606f' },
  { value: 'api', label: 'AI/API \u914d\u7f6e' },
  { value: 'auth', label: '\u5916\u90e8\u767b\u5f55' },
  { value: 'agent', label: '\u667a\u80fd\u4f53' },
  { value: 'storage', label: '\u5bf9\u8c61\u5b58\u50a8' },
  { value: 'customField', label: '\u81ea\u5b9a\u4e49\u5b57\u6bb5' }
]
