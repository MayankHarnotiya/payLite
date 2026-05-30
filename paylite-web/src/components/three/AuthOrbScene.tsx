import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { Float, MeshDistortMaterial, Sphere } from '@react-three/drei'
import type { Mesh } from 'three'

export function AuthOrbScene() {
  const coreRef = useRef<Mesh>(null)
  const ringRef = useRef<Mesh>(null)

  useFrame((state) => {
    const t = state.clock.elapsedTime
    if (coreRef.current) coreRef.current.rotation.y = t * 0.3
    if (ringRef.current) {
      ringRef.current.rotation.x = t * 0.4
      ringRef.current.rotation.z = t * 0.2
    }
  })

  return (
    <>
      <ambientLight intensity={0.35} />
      <pointLight position={[4, 4, 4]} intensity={1.2} color="#34d399" />
      <pointLight position={[-3, -2, 2]} intensity={0.6} color="#22d3ee" />

      <Float speed={2} floatIntensity={0.8}>
        <Sphere ref={coreRef} args={[1.2, 64, 64]}>
          <MeshDistortMaterial
            color="#059669"
            emissive="#10b981"
            emissiveIntensity={0.35}
            distort={0.35}
            speed={2}
            roughness={0.2}
            metalness={0.8}
          />
        </Sphere>
      </Float>

      <mesh ref={ringRef}>
        <torusGeometry args={[2, 0.04, 16, 100]} />
        <meshStandardMaterial color="#34d399" emissive="#6ee7b7" emissiveIntensity={0.6} />
      </mesh>

      <mesh rotation={[Math.PI / 2, 0, 0]}>
        <torusGeometry args={[2.4, 0.025, 16, 100]} />
        <meshStandardMaterial color="#22d3ee" emissive="#67e8f9" emissiveIntensity={0.4} transparent opacity={0.7} />
      </mesh>
    </>
  )
}
