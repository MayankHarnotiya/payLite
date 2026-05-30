import { Toaster as SonnerToaster } from 'sonner'
import { useTheme } from '@/context/ThemeContext'

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
