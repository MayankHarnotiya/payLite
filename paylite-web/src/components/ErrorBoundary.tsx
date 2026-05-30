import { Component, type ErrorInfo, type ReactNode } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Button } from '@/components/ui/Button'

interface Props {
  children: ReactNode
}

interface State {
  error: Error | null
}

/**
 * Catches render-time errors so a single broken screen never blanks the app.
 * In production this is where you'd report to Sentry/your error pipeline.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Uncaught render error:', error, info.componentStack)
  }

  handleReset = () => {
    this.setState({ error: null })
    window.location.reload()
  }

  render() {
    if (!this.state.error) return this.props.children

    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 p-6 text-center">
        <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400">
          <AlertTriangle className="h-7 w-7" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-white">
            Something went wrong
          </h1>
          <p className="mt-1 max-w-sm text-sm text-slate-500">
            An unexpected error broke this screen. Reloading usually fixes it.
          </p>
        </div>
        <Button onClick={this.handleReset}>Reload app</Button>
      </div>
    )
  }
}
