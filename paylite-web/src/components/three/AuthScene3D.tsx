import { lazy, Suspense } from 'react'
import { SceneCanvas } from './SceneCanvas'
import { cn } from '@/lib/utils'

const AuthOrbScene = lazy(() =>
  import('./AuthOrbScene').then((m) => ({ default: m.AuthOrbScene })),
)

export function AuthScene3D({ className }: { className?: string }) {
  return (
    <SceneCanvas className={cn('h-full min-h-[280px] w-full', className)}>
      <Suspense fallback={null}>
        <AuthOrbScene />
      </Suspense>
    </SceneCanvas>
  )
}
