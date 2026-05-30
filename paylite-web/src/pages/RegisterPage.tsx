import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState } from 'react'
import { useAuth } from '@/context/AuthContext'
import { getErrorMessage, getFieldErrors } from '@/lib/errors'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Card } from '@/components/ui/Card'
import { Alert } from '@/components/ui/Alert'

const schema = z.object({
  fullName: z.string().min(1, 'Full name is required').max(100),
  email: z.string().email('Enter a valid email').max(100),
  password: z
    .string()
    .min(8, 'At least 8 characters')
    .max(72, 'Maximum 72 characters'),
  phone: z
    .string()
    .optional()
    .refine((v) => !v || /^[6-9]\d{9}$/.test(v), '10-digit Indian mobile number'),
})

type FormData = z.infer<typeof schema>

export function RegisterPage() {
  const { signup } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const {
    register,
    handleSubmit,
    setError: setFieldError,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const onSubmit = handleSubmit(async (data) => {
    setError(null)
    setLoading(true)
    try {
      await signup({
        email: data.email,
        password: data.password,
        fullName: data.fullName,
        phone: data.phone || undefined,
      })
      navigate('/dashboard', { replace: true })
    } catch (e) {
      const fields = getFieldErrors(e)
      if (fields) {
        Object.entries(fields).forEach(([key, message]) => {
          if (key in schema.shape) {
            setFieldError(key as keyof FormData, { message })
          }
        })
      }
      setError(getErrorMessage(e))
    } finally {
      setLoading(false)
    }
  })

  return (
    <div className="gradient-mesh flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Create account</h1>
          <p className="mt-2 text-sm text-slate-500">Start sending money in under a minute</p>
        </div>

        {error && (
          <Alert variant="error" className="mb-4">
            {error}
          </Alert>
        )}

        <form onSubmit={onSubmit} className="space-y-4">
          <Input label="Full name" error={errors.fullName?.message} {...register('fullName')} />
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
            autoComplete="new-password"
            hint="Minimum 8 characters"
            error={errors.password?.message}
            {...register('password')}
          />
          <Input
            label="Phone (optional)"
            type="tel"
            placeholder="9876543210"
            error={errors.phone?.message}
            {...register('phone')}
          />
          <Button type="submit" className="w-full" loading={loading}>
            Create account
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-brand-600 hover:text-brand-500">
            Sign in
          </Link>
        </p>
      </Card>
    </div>
  )
}
