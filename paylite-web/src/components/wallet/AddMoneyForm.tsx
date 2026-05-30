import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { addMoney } from '@/api/wallet'
import { walletKeys } from '@/hooks/useWallet'
import { ApiError } from '@/types/api'
import { formatCurrency } from '@/lib/format'
import { getErrorMessage, getFieldErrors } from '@/lib/errors'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Alert } from '@/components/ui/Alert'

const schema = z.object({
  amount: z
    .string()
    .min(1, 'Amount is required')
    .refine((v) => !Number.isNaN(Number(v)) && Number(v) >= 0.01, 'Minimum ₹0.01')
    .refine((v) => Number(v) <= 1_000_000, 'Maximum ₹10,00,000'),
})

type FormData = z.infer<typeof schema>

const quickAmounts = [500, 1000, 2000, 5000]

export function AddMoneyForm({ onSuccess }: { onSuccess?: () => void }) {
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    setValue,
    setError,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const mutation = useMutation({
    mutationFn: (amount: number) => addMoney(amount),
    onSuccess: (wallet) => {
      queryClient.setQueryData(walletKeys.wallet, wallet)
      queryClient.invalidateQueries({ queryKey: walletKeys.wallet })
      queryClient.invalidateQueries({ queryKey: ['transactions'] })
      toast.success('Money added', {
        description: `New balance: ${formatCurrency(wallet.balance, wallet.currency)}`,
      })
      onSuccess?.()
    },
    onError: (error) => {
      const fields = getFieldErrors(error)
      if (fields?.amount) setError('amount', { message: fields.amount })
      else toast.error('Could not add money', { description: getErrorMessage(error) })
    },
  })

  const onSubmit = handleSubmit((data) => {
    mutation.mutate(Number(data.amount))
  })

  return (
    <form onSubmit={onSubmit} className="space-y-4">
      {mutation.isSuccess && (
        <Alert variant="success" title="Money added">
          Your wallet balance has been updated.
        </Alert>
      )}
      {mutation.isError && !(mutation.error instanceof ApiError && mutation.error.fieldErrors) && (
        <Alert variant="error">{getErrorMessage(mutation.error)}</Alert>
      )}

      <div className="flex flex-wrap gap-2">
        {quickAmounts.map((amt) => (
          <button
            key={amt}
            type="button"
            onClick={() => setValue('amount', String(amt), { shouldValidate: true })}
            className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm font-medium text-slate-700 transition hover:border-brand-300 hover:bg-brand-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
          >
            ₹{amt.toLocaleString('en-IN')}
          </button>
        ))}
      </div>

      <Input
        label="Amount (INR)"
        type="number"
        step="0.01"
        min="0.01"
        placeholder="1000.00"
        error={errors.amount?.message}
        {...register('amount')}
      />

      <Button type="submit" className="w-full" loading={mutation.isPending}>
        Add to wallet
      </Button>
    </form>
  )
}
