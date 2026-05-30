import { Link } from 'react-router-dom'
import { ArrowRight, Cpu, Layers, Shield, Sparkles, Wallet, Zap } from 'lucide-react'
import { lazy, Suspense } from 'react'
import { motion } from 'framer-motion'
import { Button } from '@/components/ui/Button'
import { GradientText } from '@/components/ui/PageHeader'
import { StaggerContainer, StaggerItem } from '@/components/motion/AnimatedPage'

const HeroScene3D = lazy(() =>
  import('@/components/three/HeroScene3D').then((m) => ({ default: m.HeroScene3D })),
)

const techStack = ['React 19', 'TypeScript', 'Three.js', 'Spring Boot', 'Kafka', 'TanStack Query']

const features = [
  {
    icon: Zap,
    title: 'Instant P2P transfers',
    description: 'Sub-second transfers with Stripe-style idempotency keys — safe to retry on network failure.',
    gradient: 'from-amber-500/20 to-orange-500/5',
  },
  {
    icon: Shield,
    title: 'Production-grade security',
    description: 'JWT auth, BCrypt hashing, optimistic locking, and Redis-backed deduplication.',
    gradient: 'from-emerald-500/20 to-teal-500/5',
  },
  {
    icon: Layers,
    title: 'Event-driven architecture',
    description: 'Kafka notifications decouple wallet API from side effects — microservices ready.',
    gradient: 'from-cyan-500/20 to-blue-500/5',
  },
  {
    icon: Cpu,
    title: 'Full-stack portfolio',
    description: 'AWS-deployed backend + this React client — built to stand out in 2026 job interviews.',
    gradient: 'from-violet-500/20 to-purple-500/5',
  },
]

export function LandingPage() {
  return (
    <div className="relative min-h-screen overflow-x-hidden bg-surface-950 text-slate-100">
      <div className="pointer-events-none absolute inset-0 aurora-bg" />
      <div className="pointer-events-none absolute inset-0 noise-overlay opacity-[0.035]" />

      <header className="relative z-10 mx-auto flex max-w-7xl items-center justify-between px-4 py-6 sm:px-6">
        <Link to="/" className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-400 to-teal-600 shadow-lg shadow-emerald-500/30">
            <Wallet className="h-5 w-5" />
          </span>
          <span className="font-display text-xl font-bold">PayLite</span>
        </Link>
        <div className="flex items-center gap-2 sm:gap-3">
          <Link to="/login">
            <Button variant="ghost" className="text-slate-300 hover:text-white">
              Sign in
            </Button>
          </Link>
          <Link to="/register">
            <Button className="btn-glow hidden sm:inline-flex">Get started</Button>
            <Button className="btn-glow sm:hidden" size="sm">
              Start
            </Button>
          </Link>
        </div>
      </header>

      <main className="relative z-10 mx-auto max-w-7xl px-4 pb-24 pt-4 sm:px-6 sm:pt-8">
        <div className="grid items-center gap-12 lg:grid-cols-2 lg:gap-8">
          <motion.div
            initial={{ opacity: 0, x: -24 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
          >
            <p className="mb-6 inline-flex items-center gap-2 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-4 py-1.5 text-sm font-medium text-emerald-300">
              <Sparkles className="h-4 w-4" />
              Full-stack fintech · Portfolio 2026
            </p>

            <h1 className="font-display text-4xl font-extrabold leading-[1.08] tracking-tight sm:text-5xl xl:text-6xl">
              Move money
              <span className="block">
                at the <GradientText>speed of now</GradientText>
              </span>
            </h1>

            <p className="mt-6 max-w-lg text-lg leading-relaxed text-slate-400">
              A premium digital wallet experience powered by Spring Boot, Kafka, and React Three Fiber —
              designed to impress recruiters and users alike.
            </p>

            <div className="mt-8 flex flex-wrap gap-2">
              {techStack.map((t) => (
                <span
                  key={t}
                  className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-medium text-slate-400"
                >
                  {t}
                </span>
              ))}
            </div>

            <div className="mt-10 flex flex-col gap-4 sm:flex-row">
              <Link to="/register">
                <Button size="lg" className="btn-glow min-w-[200px]">
                  Open your wallet
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="secondary" size="lg" className="min-w-[200px] border-white/10 bg-white/5 text-white hover:bg-white/10">
                  Sign in
                </Button>
              </Link>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.15, ease: [0.22, 1, 0.36, 1] }}
            className="relative"
          >
            <div className="glow-border animate-float overflow-hidden rounded-3xl">
              <Suspense
                fallback={
                  <div className="flex h-[320px] items-center justify-center rounded-3xl bg-emerald-500/10 sm:h-[420px] lg:h-[520px]">
                    <div className="h-10 w-10 animate-spin rounded-full border-2 border-emerald-400 border-t-transparent" />
                  </div>
                }
              >
                <HeroScene3D />
              </Suspense>
            </div>
          </motion.div>
        </div>

        <StaggerContainer className="mt-24 grid gap-5 sm:grid-cols-2">
          {features.map(({ icon: Icon, title, description, gradient }) => (
            <StaggerItem key={title}>
              <div
                className={`group glow-border rounded-2xl bg-gradient-to-br ${gradient} p-6 transition-transform hover:scale-[1.02]`}
              >
                <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-white/10 text-emerald-300 ring-1 ring-white/10 transition group-hover:bg-emerald-500/20">
                  <Icon className="h-6 w-6" />
                </div>
                <h3 className="font-display text-lg font-bold text-white">{title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-slate-400">{description}</p>
              </div>
            </StaggerItem>
          ))}
        </StaggerContainer>
      </main>

      <footer className="relative z-10 border-t border-white/5 py-8">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-3 px-4 text-sm text-slate-500 sm:flex-row sm:px-6">
          <p>© {new Date().getFullYear()} PayLite — Mayank Harnotiya</p>
          <p>Spring Boot · Kafka · React · Three.js · AWS</p>
        </div>
      </footer>
    </div>
  )
}
