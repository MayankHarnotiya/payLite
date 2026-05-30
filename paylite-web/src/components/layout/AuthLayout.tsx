import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Wallet } from 'lucide-react'
import { AuthScene3D } from '@/components/three/AuthScene3D'
import { cn } from '@/lib/utils'

interface AuthLayoutProps {
  children: ReactNode
  title: string
  subtitle: string
}

export function AuthLayout({ children, title, subtitle }: AuthLayoutProps) {
  return (
    <div className="relative min-h-screen overflow-hidden bg-surface-950 text-slate-100">
      <div className="pointer-events-none absolute inset-0 aurora-bg" />
      <div className="pointer-events-none absolute inset-0 noise-overlay opacity-[0.04]" />

      <div className="relative mx-auto grid min-h-screen max-w-7xl lg:grid-cols-2">
        <div className="relative hidden flex-col justify-between p-10 lg:flex xl:p-14">
          <Link to="/" className="flex items-center gap-3">
            <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-400 to-teal-600 shadow-lg shadow-emerald-500/30">
              <Wallet className="h-5 w-5 text-white" />
            </span>
            <span className="text-xl font-bold tracking-tight">PayLite</span>
          </Link>

          <div className="flex flex-1 flex-col justify-center py-8">
            <AuthScene3D className="mx-auto max-h-[420px] max-w-lg" />
            <div className="mt-10 max-w-md">
              <h2 className="font-display text-3xl font-bold leading-tight xl:text-4xl">
                Your money,{' '}
                <span className="bg-gradient-to-r from-emerald-300 to-cyan-300 bg-clip-text text-transparent">
                  reimagined
                </span>
              </h2>
              <p className="mt-4 text-slate-400">
                Full-stack fintech portfolio — Spring Boot, Kafka, JWT, and a React experience
                built for 2026 hiring standards.
              </p>
            </div>
          </div>

          <p className="text-xs text-slate-500">Secured with BCrypt · JWT · Idempotent transfers</p>
        </div>

        <div className="flex items-center justify-center p-4 sm:p-8">
          <div className={cn('glass-dark w-full max-w-md rounded-3xl p-8 sm:p-10')}>
            <div className="mb-8 lg:hidden">
              <Link to="/" className="mb-6 flex items-center gap-2 font-bold">
                <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-600">
                  <Wallet className="h-4 w-4" />
                </span>
                PayLite
              </Link>
            </div>
            <div className="mb-8">
              <h1 className="font-display text-2xl font-bold text-white">{title}</h1>
              <p className="mt-2 text-sm text-slate-400">{subtitle}</p>
            </div>
            {children}
          </div>
        </div>
      </div>
    </div>
  )
}
