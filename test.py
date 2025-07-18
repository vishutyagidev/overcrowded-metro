import os
import gzip
import shutil
import requests
import numpy as np
import rasterio
from rasterio.merge import merge
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import osmnx as ox
import trimesh  # <-- added


# 1. Define location
place = "Manali, Himachal Pradesh, India"

# 2. Get bounding box from OSM
area = ox.geocode_to_gdf(place)
minx, miny, maxx, maxy = area.total_bounds
print(f"Bounds: {minx}, {miny}, {maxx}, {maxy}")

# 3. Compute SRTM tiles (1° x 1°)
def get_tile_coords(minx, miny, maxx, maxy):
    lons = range(int(np.floor(minx)), int(np.ceil(maxx)))
    lats = range(int(np.floor(miny)), int(np.ceil(maxy)))
    return [(lat, lon) for lat in lats for lon in lons]

tiles = get_tile_coords(minx, miny, maxx, maxy)
print(f"Tiles needed: {tiles}")

# 4. Download and unzip tiles
os.makedirs("tiles", exist_ok=True)
tif_files = []
for lat, lon in tiles:
    ns = 'N' if lat >= 0 else 'S'
    ew = 'E' if lon >= 0 else 'W'
    tile_name = f"{ns}{abs(lat):02d}{ew}{abs(lon):03d}"
    url = f"https://s3.amazonaws.com/elevation-tiles-prod/skadi/{ns}{abs(lat):02d}/{tile_name}.hgt.gz"
    gz_path = f"tiles/{tile_name}.hgt.gz"
    hgt_path = gz_path.replace('.gz', '')

    if not os.path.exists(hgt_path):
        print(f"Downloading {tile_name}...")
        r = requests.get(url, stream=True)
        if r.status_code == 200:
            with open(gz_path, 'wb') as f:
                shutil.copyfileobj(r.raw, f)
            with gzip.open(gz_path, 'rb') as f_in:
                with open(hgt_path, 'wb') as f_out:
                    shutil.copyfileobj(f_in, f_out)
        else:
            print(f"Tile {tile_name} not available")
            continue

    tif_files.append(hgt_path)

# 5. Merge and clip
datasets = [rasterio.open(fp) for fp in tif_files]
mosaic, out_trans = merge(datasets, bounds=(minx, miny, maxx, maxy))

# 6. Plot 3D
elevation = mosaic[0]
elevation = np.where(elevation < 0, np.nan, elevation)

fig = plt.figure(figsize=(12, 8))
ax = fig.add_subplot(111, projection='3d')

rows, cols = elevation.shape
X, Y = np.meshgrid(np.linspace(minx, maxx, cols), np.linspace(miny, maxy, rows))

ax.plot_surface(X, Y, elevation, cmap='terrain', linewidth=0, antialiased=False)
ax.set_xlabel('Longitude')
ax.set_ylabel('Latitude')
ax.set_zlabel('Elevation (m)')
plt.title(f"3D Terrain of {place}")
plt.tight_layout()
plt.savefig("manali_test_fig.jpg")
plt.show()



from pyproj import Transformer
from scipy.spatial import Delaunay
import trimesh

# Convert lat/lon to meters (Web Mercator or UTM)
transformer = Transformer.from_crs("EPSG:4326", "EPSG:3857", always_xy=True)
x_m, y_m = transformer.transform(X.flatten(), Y.flatten())
z_m = elevation.flatten()

# Mask NaNs
valid = ~np.isnan(z_m)
vertices = np.vstack([x_m[valid], y_m[valid], z_m[valid] * 3.0]).T  # Z scaled by 3×

# Triangulate based on X, Y
tri = Delaunay(vertices[:, :2])
faces = tri.simplices

# Build mesh
mesh = trimesh.Trimesh(vertices=vertices, faces=faces)
mesh.export("manali_terrain_scaled.obj")

# # 7. Export to OBJ
# def new_func(elevation, rows, cols, X, Y):
#     print("Preparing 3D mesh for OBJ export...")

# # Clean grid to remove NaNs
#     elevation_clean = np.nan_to_num(elevation, nan=0.0)

# # Create vertices: Z from elevation, X and Y from meshgrid
#     vertices = np.column_stack([
#     X.ravel(),
#     Y.ravel(),
#     elevation_clean.ravel()
# ])

# # Create faces from regular grid topology
#     faces = []
#     for i in range(rows - 1):
#         for j in range(cols - 1):
#             v0 = i * cols + j
#             v1 = (i + 1) * cols + j
#             v2 = i * cols + (j + 1)
#             v3 = (i + 1) * cols + (j + 1)

#         # Two triangles per square
#             faces.append([v0, v1, v2])
#             faces.append([v1, v3, v2])

#     faces = np.array(faces)

# # Build and export the mesh
#     terrain_mesh = trimesh.Trimesh(vertices=vertices, faces=faces, process=False)

#     obj_file = "manali_terrain.obj"
#     terrain_mesh.export(obj_file)
#     print(f"✅ Exported mesh to {obj_file}")

# # Optional: view in trimesh viewer
#     print("Launching viewer...")
#     terrain_mesh.show()

# new_func(elevation, rows, cols, X, Y)
