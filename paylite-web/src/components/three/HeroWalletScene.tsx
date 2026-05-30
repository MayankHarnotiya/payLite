import { Sparkles, Stars } from '@react-three/drei'
import { WalletModel } from './WalletModel'

export function HeroWalletScene() {
  return (
    <>
      <color attach="background" args={['#0a1628']} />
      <fog attach="fog" args={['#0a1628', 6, 14]} />
      <Stars radius={50} count={1200} factor={2} saturation={0} fade speed={0.5} />
      <Sparkles count={100} scale={8} size={3} speed={0.4} color="#34d399" />
      <Sparkles count={50} scale={6} size={2} speed={0.2} color="#22d3ee" />
      <WalletModel />
    </>
  )
}
