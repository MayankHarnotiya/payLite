import { Link } from 'react-router-dom'
import { ArrowRight, Shield, Sparkles, Zap } from 'lucide-react'
import { Button } from '@/components/ui/Button'

const features = [
  {
    icon: Zap,
    title: 'Instant transfers',
    description: 'Send money to any PayLite user in seconds with idempotent, retry-safe APIs.',
  },
  {
    icon: Shield,
    title: 'Bank-grade security',
    description: 'JWT authentication, BCrypt passwords, and optimistic locking on every wallet.',
  },
  {
    icon: Sparkles,
    title: 'Built for demos',
    description: 'Production Spring Boot backend with a modern React client — portfolio ready.',
  },
]

const stats = [
  { value: '< 1s', label: 'Transfer latency' },
  { value: '100%', label: 'Idempotent APIs' },
  { value: '24/7', label: 'Real-time updates' },
]

export function LandingPage() {
  return (
    <div className="gradient-mesh min-h-screen">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-4 py-6 sm:px-6">
        <div className="flex items-center gap-2 font-bold text-slate-900 dark:text-white">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">P</span>
          PayLite
        </div>
        <div className="flex items-center gap-3">
          <Link to="/login">
            <Button variant="ghost">Sign in</Button>
          </Link>
          <Link to="/register">
            <Button>Get started</Button>
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 pb-20 pt-8 sm:px-6 sm:pt-16">
        <div className="mx-auto max-w-3xl text-center">
          <p className="mb-4 inline-flex items-center gap-2 rounded-full border border-brand-200 bg-brand-50 px-4 py-1.5 text-sm font-medium text-brand-800 dark:border-brand-800 dark:bg-brand-950/50 dark:text-brand-300">
            <Sparkles className="h-4 w-4" />
            Digital wallet for the modern web
          </p>
          <h1 className="text-4xl font-bold tracking-tight text-slate-900 sm:text-6xl dark:text-white">
            Send money.
            <span className="block text-brand-600">Without the friction.</span>
          </h1>
          <p className="mt-6 text-lg text-slate-600 dark:text-slate-400">
            PayLite is a full-stack fintech demo — Spring Boot API, JWT auth, P2P transfers, and
            transaction history in a responsive React experience.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link to="/register">
              <Button size="lg" className="min-w-[180px]">
                Create free account
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
            <Link to="/login">
              <Button variant="secondary" size="lg" className="min-w-[180px]">
                I have an account
              </Button>
            </Link>
          </div>
        </div>

        <div className="mt-16 grid grid-cols-3 gap-4">
          {stats.map((s) => (
            <div key={s.label} className="text-center">
              <p className="text-2xl font-bold text-slate-900 sm:text-3xl dark:text-white">
                {s.value}
              </p>
              <p className="mt-1 text-xs text-slate-500 sm:text-sm">{s.label}</p>
            </div>
          ))}
        </div>

        <div className="mt-16 grid gap-6 sm:grid-cols-3">
          {features.map(({ icon: Icon, title, description }) => (
            <div key={title} className="glass rounded-2xl p-6">
              <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-brand-100 text-brand-700 dark:bg-brand-900/40 dark:text-brand-300">
                <Icon className="h-5 w-5" />
              </div>
              <h3 className="font-semibold text-slate-900 dark:text-white">{title}</h3>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">{description}</p>
            </div>
          ))}
        </div>
      </main>

      <footer className="mx-auto max-w-6xl px-4 py-8 sm:px-6">
        <div className="flex flex-col items-center justify-between gap-3 border-t border-slate-200/70 pt-6 text-sm text-slate-500 sm:flex-row dark:border-slate-800">
          <p>© {new Date().getFullYear()} PayLite — a full-stack fintech demo.</p>
          <p>Spring Boot · Kafka · React · TypeScript</p>
        </div>
      </footer>
    </div>
  )
}
