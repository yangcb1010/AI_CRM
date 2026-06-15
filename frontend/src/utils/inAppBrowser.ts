import { DefaultWebViewOptions, InAppBrowser, ToolbarPosition } from '@capacitor/inappbrowser'
import { isNativeMobileRuntime, isNativePluginAvailable } from '@/utils/nativeMobileRuntime'

function resolveAbsoluteHttpUrl(href: string): string | null {
  if (typeof window === 'undefined') return null

  try {
    const url = new URL(href, window.location.href)
    return url.protocol === 'http:' || url.protocol === 'https:' ? url.href : null
  } catch {
    return null
  }
}

export async function openLegalDocumentWithInAppBrowser(href: string): Promise<boolean> {
  if (!isNativeMobileRuntime() || !isNativePluginAvailable('InAppBrowser')) {
    return false
  }

  const url = resolveAbsoluteHttpUrl(href)
  if (!url) return false

  try {
    await InAppBrowser.openInWebView({
      url,
      options: {
        ...DefaultWebViewOptions,
        showURL: false,
        showNavigationButtons: false,
        closeButtonText: '关闭',
        toolbarPosition: ToolbarPosition.TOP,
        android: {
          ...DefaultWebViewOptions.android,
          hardwareBack: true
        },
        iOS: {
          ...DefaultWebViewOptions.iOS,
          allowsBackForwardNavigationGestures: true
        }
      }
    })
    return true
  } catch (error) {
    console.warn('Failed to open legal document in InAppBrowser:', error)
    return false
  }
}
