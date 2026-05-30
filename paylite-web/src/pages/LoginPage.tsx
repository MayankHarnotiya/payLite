import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState } from 'react'
import { useAuth } from '@/context/AuthContext'
import { getErrorMessage } from '@/lib/errors'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Card } from '@/components/ui/Card'
import { Alert } from '@/components/ui/Alert'

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
})

type FormData = z.infer<typeof schema>

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/dashboard'
  const sessionExpired = new URLSearchParams(location.search).get('session') === 'expired'

  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = handleSubmit(async (data) => {
    setError(null)
    setLoading(true)
    try {
      await login(data)
      navigate(from, { replace: true })
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setLoading(false)
    }
  })

  return (
    <div className="gradient-mesh flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Welcome back</h1>
          <p className="mt-2 text-sm text-slate-500">Sign in to your PayLite wallet</p>
        </div>

        {sessionExpired && (
          <Alert variant="info" className="mb-4">
            Your session expired. Please sign in again.
          </Alert>
        )}
        {error && (
          <Alert variant="error" className="mb-4">
            {error}
          </Alert>
        )}

        <form onSubmit={onSubmit} className="space-y-4">
          <Input
            label="Email"
            type="email"
            autoComplete="email"
            error={errors.email?.message}
            {...register('email')}
          />
          <Input
            label="Password"
            type="password"
            autoComplete="current-password"
            error={errors.password?.message}
            {...register('password')}
          />
          <Button type="submit" className="w-full" loading={loading}>
            Sign in
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          New to PayLite?{' '}
          <Link to="/register" className="font-semibold text-brand-600 hover:text-brand-500">
            Create account
          </Link>
        </p>
      </Card>
    </div>
  )
}
