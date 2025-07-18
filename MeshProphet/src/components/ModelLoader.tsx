import { useGLTF } from "@react-three/drei";
import { useFrame } from "@react-three/fiber";
import { useRef } from "react";
import { Group } from "three";

const ModelLoader = () => {
  const gltf = useGLTF(
    "https://d2uyg86r3fxv8z.cloudfront.net/Hy3D_textured_00001_.glb"
  );

  const ref = useRef<Group>(null);

  useFrame(() => {
    if (ref.current) {
      ref.current.rotation.y += 0.0025;
    }
  });

  return <primitive object={gltf.scene} ref={ref} scale={5} />;
};

export default ModelLoader;
