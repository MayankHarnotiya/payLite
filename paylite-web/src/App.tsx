import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from '@/context/AuthContext'
import { ThemeProvider } from '@/context/ThemeContext'
import { NotificationProvider } from '@/context/NotificationContext'
import { ErrorBoundary } from '@/components/ErrorBoundary'
import { Toaster } from '@/components/ui/Toaster'
import { AppRoutes } from '@/routes/AppRoutes'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: true,
    },
  },
})

export function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <Toaster />
          <AuthProvider>
            <NotificationProvider>
              <AppRoutes />
            </NotificationProvider>
          </AuthProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  )
}
