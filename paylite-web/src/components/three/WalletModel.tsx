import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { Float, RoundedBox } from '@react-three/drei'
import type { Group } from 'three'

function Coin({ position, scale = 1 }: { position: [number, number, number]; scale?: number }) {
  const ref = useRef<Group>(null)

  useFrame((state) => {
    if (!ref.current) return
    ref.current.rotation.y = state.clock.elapsedTime * 1.2
    ref.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.8) * 0.2
  })

  return (
    <group ref={ref} position={position} scale={scale}>
      <mesh castShadow>
        <cylinderGeometry args={[0.35, 0.35, 0.08, 32]} />
        <meshStandardMaterial
          color="#fbbf24"
          emissive="#f59e0b"
          emissiveIntensity={0.4}
          metalness={0.9}
          roughness={0.2}
        />
      </mesh>
      <mesh position={[0, 0, 0.045]}>
        <circleGeometry args={[0.22, 32]} />
        <meshStandardMaterial color="#fde68a" emissive="#fcd34d" emissiveIntensity={0.3} />
      </mesh>
    </group>
  )
}

function Chip({ position }: { position: [number, number, number] }) {
  return (
    <RoundedBox args={[0.45, 0.35, 0.02]} radius={0.03} position={position}>
      <meshStandardMaterial color="#d4af37" metalness={0.95} roughness={0.15} />
    </RoundedBox>
  )
}

export function WalletModel() {
  const walletRef = useRef<Group>(null)

  useFrame((state) => {
    if (!walletRef.current) return
    walletRef.current.rotation.y = Math.sin(state.clock.elapsedTime * 0.35) * 0.15
    walletRef.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.25) * 0.05
  })

  return (
    <group>
      <ambientLight intensity={0.7} />
      <hemisphereLight args={['#34d399', '#0f172a', 0.6]} />
      <directionalLight position={[5, 8, 5]} intensity={1.5} />
      <pointLight position={[-4, 2, 4]} intensity={1.2} color="#34d399" />
      <pointLight position={[4, -2, 2]} intensity={0.8} color="#22d3ee" />

      <Float speed={1.5} rotationIntensity={0.2} floatIntensity={0.6}>
        <group ref={walletRef}>
          <RoundedBox args={[2.8, 1.7, 0.12]} radius={0.08} smoothness={4} castShadow>
            <meshStandardMaterial
              color="#10b981"
              emissive="#059669"
              emissiveIntensity={0.35}
              metalness={0.5}
              roughness={0.25}
            />
          </RoundedBox>

          <RoundedBox args={[2.4, 0.35, 0.02]} radius={0.04} position={[0, 0.45, 0.07]}>
            <meshStandardMaterial
              color="#6ee7b7"
              emissive="#34d399"
              emissiveIntensity={0.8}
              metalness={0.3}
              roughness={0.2}
            />
          </RoundedBox>

          <Chip position={[-0.95, 0.05, 0.08]} />

          {[0, 1, 2].map((i) => (
            <mesh key={i} position={[-0.3 + i * 0.22, -0.15, 0.08]}>
              <boxGeometry args={[0.14, 0.04 + i * 0.06, 0.01]} />
              <meshStandardMaterial color="#ecfdf5" emissive="#6ee7b7" emissiveIntensity={0.5} />
            </mesh>
          ))}
        </group>
      </Float>

      <Coin position={[-2.2, 1.4, 0.5]} scale={0.9} />
      <Coin position={[2.3, 0.8, -0.3]} scale={0.75} />
      <Coin position={[1.8, -1.5, 0.6]} scale={0.65} />
    </group>
  )
}
