import { lazy, Suspense } from 'react'
import { SceneCanvas } from './SceneCanvas'
import { HeroSceneFallback } from './HeroSceneFallback'
import { cn } from '@/lib/utils'

const HeroWalletScene = lazy(() =>
  import('./HeroWalletScene').then((m) => ({ default: m.HeroWalletScene })),
)

export function HeroScene3D({ className }: { className?: string }) {
  const sizeClass = cn('h-[320px] w-full sm:h-[420px] lg:h-[520px]', className)

  return (
    <SceneCanvas
      className={sizeClass}
      fallback={<HeroSceneFallback className={sizeClass} />}
    >
      <Suspense fallback={null}>
        <HeroWalletScene />
      </Suspense>
    </SceneCanvas>
  )
}