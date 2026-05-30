import { Toaster as SonnerToaster } from 'sonner'
import { useTheme } from '@/context/ThemeContext'

/**
 * App-wide toast host. Mounted once near the root (inside ThemeProvider).
 * Use the re-exported `toast` from 'sonner' anywhere to fire notifications.
 */
export function Toaster() {
  const { theme } = useTheme()

  return (
    <SonnerToaster
      theme={theme}
      position="top-right"
      richColors
      closeButton
      toastOptions={{
        classNames: {
          toast: 'rounded-xl border font-sans',
        },
      }}
    />
  )
}
