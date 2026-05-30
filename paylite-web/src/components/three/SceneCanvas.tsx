import { Suspense, type ReactNode } from 'react'
import { Canvas } from '@react-three/fiber'
import { cn } from '@/lib/utils'
import { useIsMobile, usePrefersReducedMotion } from '@/hooks/useMediaQuery'
import { HeroSceneFallback } from './HeroSceneFallback'

interface SceneCanvasProps {
  children: ReactNode
  className?: string
  fallback?: ReactNode
  enable3d?: boolean
}

function CanvasFallback({ className }: { className?: string }) {
  return <HeroSceneFallback className={className} />
}

export function SceneCanvas({
  children,
  className,
  fallback,
  enable3d = true,
}: SceneCanvasProps) {
  const reducedMotion = usePrefersReducedMotion()
  const isMobile = useIsMobile()

  if (!enable3d || reducedMotion) {
    return fallback ?? <CanvasFallback className={className} />
  }

  return (
    <div className={cn('relative overflow-hidden rounded-3xl', className)}>
      <Canvas
        dpr={isMobile ? [1, 1.5] : [1, 2]}
        gl={{ antialias: true, alpha: false, powerPreference: 'high-performance' }}
        camera={{ position: [0, 0, 6], fov: 45 }}
        className="!absolute inset-0 bg-gradient-to-br from-emerald-950 via-slate-900 to-cyan-950"
      >
        <Suspense fallback={null}>{children}</Suspense>
      </Canvas>
    </div>
  )
}
