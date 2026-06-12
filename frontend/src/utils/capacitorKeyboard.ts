import { Capacitor, type PluginListenerHandle } from '@capacitor/core'
import { Keyboard, KeyboardResize } from '@capacitor/keyboard'
import type { KeyboardInfo } from '@capacitor/keyboard'

type NativeKeyboardInsetCallbacks = {
  onShow: (keyboardHeight: number) => void
  onHide: () => void
}

export function shouldUseCapacitorKeyboardHide(
  isNativePlatform: boolean,
  isKeyboardPluginAvailable: boolean
): boolean {
  return isNativePlatform && isKeyboardPluginAvailable
}

export function shouldConfigureNativeKeyboard(
  isNativePlatform: boolean,
  isKeyboardPluginAvailable: boolean,
  platform: string
): boolean {
  return isNativePlatform && isKeyboardPluginAvailable && platform === 'ios'
}

export function shouldUseNativeKeyboardEvents(
  isNativePlatform: boolean,
  isKeyboardPluginAvailable: boolean
): boolean {
  return isNativePlatform && isKeyboardPluginAvailable
}

export function configureNativeKeyboard(): void {
  if (!shouldConfigureNativeKeyboard(
    Capacitor.isNativePlatform(),
    Capacitor.isPluginAvailable('Keyboard'),
    Capacitor.getPlatform()
  )) {
    return
  }

  void Keyboard.setResizeMode({ mode: KeyboardResize.None }).catch((error) => {
    console.warn('设置原生键盘 resize 模式失败:', error)
  })
  void Keyboard.setAccessoryBarVisible({ isVisible: false }).catch((error) => {
    console.warn('设置原生键盘 accessory bar 失败:', error)
  })
}

export function hideCapacitorKeyboard(): void {
  if (!shouldUseCapacitorKeyboardHide(
    Capacitor.isNativePlatform(),
    Capacitor.isPluginAvailable('Keyboard')
  )) {
    return
  }

  void Keyboard.hide().catch((error) => {
    console.warn('关闭原生键盘失败:', error)
  })
}

export function registerNativeKeyboardInsetListeners(
  callbacks: NativeKeyboardInsetCallbacks
): () => void {
  if (!shouldUseNativeKeyboardEvents(
    Capacitor.isNativePlatform(),
    Capacitor.isPluginAvailable('Keyboard')
  )) {
    return () => undefined
  }

  let disposed = false
  const handles: PluginListenerHandle[] = []

  const addHandle = (listenerPromise: Promise<PluginListenerHandle>) => {
    void listenerPromise
      .then((handle) => {
        if (disposed) {
          void handle.remove()
          return
        }
        handles.push(handle)
      })
      .catch((error) => {
        console.warn('注册原生键盘监听失败:', error)
      })
  }

  const handleShow = (info: KeyboardInfo) => {
    callbacks.onShow(Math.max(0, Math.round(info.keyboardHeight || 0)))
  }
  const handleHide = () => {
    callbacks.onHide()
  }

  addHandle(Keyboard.addListener('keyboardWillShow', handleShow))
  addHandle(Keyboard.addListener('keyboardDidShow', handleShow))
  addHandle(Keyboard.addListener('keyboardWillHide', handleHide))
  addHandle(Keyboard.addListener('keyboardDidHide', handleHide))

  return () => {
    disposed = true
    handles.splice(0).forEach((handle) => {
      void handle.remove()
    })
  }
}
