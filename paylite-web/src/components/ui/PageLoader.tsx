import { Spinner } from '@/components/ui/Spinner'

/** Full-height fallback shown while a lazily-loaded route chunk downloads. */
export function PageLoader() {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <Spinner className="h-8 w-8" />
    </div>
  )
}
