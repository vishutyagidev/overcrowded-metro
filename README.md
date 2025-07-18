# Omnislash

> [ Team : overcrowded-metro ]

Welcome to Omnislash - a project born with a simple vision: we're not the climate heroes, but you can be one of them.

Accurate 3D models of landscapes are often locked behind corporate paywalls or simply not available at all. We're changing that. Omnislash gives everyone - from scientists and civil planners to educators and indie hackers - the power to easily generate, refine, and use high-quality 3D terrain models, all for free, and all locally.

## ✨ Why This Matters

While we don't directly combat climate change ourselves, our project enables those who do. Accurate, accessible 3D models are vital for:

- Simulations of real-world scenarios like flood risk, erosion, or urban heat mapping.

- Sustainable infrastructure planning - visualize roads, bridges, and green spaces before they're built.

- Education & research - make complex geospatial data approachable and visual.

- We believe that giving people the right tools empowers them to build the future. 🌱

## Key Features

### 🗺️ 1. Terrain Data Aggregation

- Fetches topographical data from multiple open-source geospatial databases.

- Automatically generates base 3D models for any zone of interest.

### 📷 2. Multi-Modal Terrain Enrichment

- Use our custom-built camera app to:

  - Capture photos with rich metadata (GPS coordinates, orientation, etc.).

  - Optionally apply a masking layer for granular focus.

- Feed images and chat prompts into our platform to enhance terrain models.

- Behind the scenes:

  - Images and prompts are processed by state-of-the-art multi-modal LLMs.

  - The resulting 3D models are superimposed and refined automatically.

### 🧊 3. Real-Time Interactive 3D Viewer

- Continuously updated and rendered directly in your browser.

- All processing is done locally - no data leaves your machine. 🔒

- When ready, export your model with one click to `.obj` and `.glb` format - fully open and portable.

### 💡 4. Simulate Real or Hypothetical Scenarios

- Easily modify landscapes with hypothetical structures (roads, bridges, etc.) via chat prompts.

- Run your own simulations on current or future terrains to promote sustainable design.

## Our Camera App - FourthEye

Our mobile app doesn't just take pictures - it captures:

- 📍 Latitude & Longitude

- 🧭 Camera Orientation

- 🖌️ Custom Masking Layers

- 📝 Optional Annotations

This rich metadata gives our system the precision needed to generate accurate and meaningful 3D models.

## Our model generator workflow - RoadToTI

The generator workflow functions using the following steps:

- In the first step, we extract the topographical data of a region based on user input using Open Street Maps API which is then converted into a 3D plot and then exported into an object file that can be opened in any 3D modelling program like Blender.

- The second step is where the AI magic happens. We have used [Hunyuan 3D 2.0](https://huggingface.co/tencent/Hunyuan3D-2), a scaling diffusion model for high resolution 3D asset generation along with [ComfyUI](https://github.com/comfyanonymous/ComfyUI), a highly customizable open source platform for generating images, videos, 3D and other varied content. With these tools we have designed an efficient pipeline that takes the image as input and outputs a high quality 3D glb model file.

- In the third step the glb model file obtained from the previous step is converted into an obj file and is fused with the obj file obtained in step 1 using 3D projection algorithms with the help of the directional metadata captured from the picture. This greatly enhances the detail of the model being prepared. The final combined model is then sent to the Visualization Dashboard for further minute tweaking.

## Visualization Dashboard - MeshProphet

// TODO @twinbladeRoG

## Join Us on This Mission

We're not trying to sell you anything. We're a small group of passionate devs, but we believe in community and collaboration. Contributions, ideas, and feedback are always welcome!

Let's empower climate heroes together. 🌎
