export type SettingsMainTab = 'team' | 'role' | 'system'

export type SystemSettingsTab =
  | 'enterprise'
  | 'api'
  | 'auth'
  | 'agent'
  | 'storage'
  | 'customField'

export interface SettingsTabItem<T extends string> {
  value: T
  label: string
}
