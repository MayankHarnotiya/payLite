import { useLocation } from 'react-router-dom'
import { Card } from '@/components/ui/Card'
import { TransferForm } from '@/components/wallet/TransferForm'
import { AnimatedPage } from '@/components/motion/AnimatedPage'
import { PageHeader } from '@/components/ui/PageHeader'

export function TransferPage() {
  const location = useLocation()
  const recipient = (location.state as { recipient?: string } | null)?.recipient

  return (
    <AnimatedPage className="mx-auto max-w-lg space-y-6">
      <PageHeader
        title="Send money"
        subtitle="Transfer instantly to any PayLite user by email"
      />

      <Card className="glow-border border-white/10 bg-slate-900/50">
        <TransferForm defaultRecipient={recipient} />
      </Card>
    </AnimatedPage>
  )
}
