import { Suspense, useEffect } from "react";
import { Canvas } from "@react-three/fiber";
import { Environment, OrbitControls } from "@react-three/drei";
import ModelLoader from "./ModelLoader";

const ThreeDRender = () => {
  return (
    <section className="bg-cross-mesh rounded-3xl overflow-hidden">
      <Canvas camera={{ position: [3, 3, 4] }}>
        <ambientLight intensity={1.2} />
        <directionalLight position={[5, 5, 5]} intensity={1.5} castShadow />
        {/* <pointLight position={[-5, -5, -5]} intensity={1} /> */}
        <Suspense fallback={null}>
          <Environment preset="sunset" />
          <ModelLoader />
        </Suspense>
        <OrbitControls />
      </Canvas>
    </section>
  );
};

export default ThreeDRender;
