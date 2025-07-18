#!/usr/bin/env python
from workflow import run_workflow
from pathlib import Path
import json, os, shutil
from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import Optional
import boto3
from botocore.exceptions import BotoCoreError, ClientError

LOCAL_S3DATA_DIR = os.path.abspath("./s3data")
comfyui_dir = Path('/home/rohan/repos/ComfyUI/')

def remove_files(directory, extensions):
    dir_path = Path(directory)
    for extension in extensions:
        for file in dir_path.glob(f'*.{extension}'):
            try:
                file.unlink()
            except Exception:
                 pass

def clean_cache(comfyui_dir = '/home/rohan/repos/ComfyUI/'):
    remove_files(Path(comfyui_dir) / Path('input'), ['jpg', 'png'])
    remove_files(Path(comfyui_dir) / Path('output') / Path('3D'), ['glb'])


def ensure_local_s3data():
    """Ensure the local 's3data' directory exists."""
    if not os.path.isdir(LOCAL_S3DATA_DIR):
        print(f"Creating local dump directory: {LOCAL_S3DATA_DIR}")
        os.makedirs(LOCAL_S3DATA_DIR, exist_ok=True)

def get_s3_client():
    return boto3.client(
        "s3",
        aws_access_key_id=os.environ['AWS_ACCESS_KEY_ID'],
        aws_secret_access_key=os.environ['AWS_SECRET_ACCESS_KEY'],
        region_name=os.environ['AWS_REGION']
    )

def get_hackathon_s3_client():
    return boto3.client(
        "s3",
        aws_access_key_id=os.environ['HACKATHON_AWS_ACCESS_KEY_ID'],
        aws_secret_access_key=os.environ['HACKATHON_AWS_SECRET_ACCESS_KEY'],
        region_name=os.environ['HACKATHON_AWS_REGION']
    )

def upload_file(src_path: str):
    if not os.path.isfile(src_path):
        print(f"Source file not found: {src_path}")

    ensure_local_s3data()
    filename = os.path.basename(src_path)
    local_dest = os.path.join(LOCAL_S3DATA_DIR, filename)
    shutil.copy2(src_path, local_dest)
    print(f"Copied to local dump: {local_dest}")

    s3 = get_hackathon_s3_client()
    print(f"Uploading to S3: s3://{os.environ['HACKATHON_BUCKET_NAME']}/public/{filename}")
    s3.upload_file(local_dest, os.environ['HACKATHON_BUCKET_NAME'], f"public/{filename}")
    print("Upload succeeded")

def download_file(filename: str):
    ensure_local_s3data()
    local_dest = os.path.join(LOCAL_S3DATA_DIR, filename)

    s3 = get_s3_client()
    print(f"Downloading from S3: s3://{os.environ['BUCKET_NAME']}/{filename}")
    s3.download_file(os.environ['BUCKET_NAME'], filename, local_dest)
    print(f"Downloaded to local dump: {local_dest}")

def generate_glb(image_name: str):
    with open(Path('road_to_ti.json'), 'r', encoding='utf-8') as file:
        ti_workflow = json.load(file)
        ti_workflow["13"]["inputs"]["image"] = image_name
        run_workflow(ti_workflow)



app = FastAPI()

class GenerateRequest(BaseModel):
    image: str
    mask: str
    metadata: str

class GenerateResponse(BaseModel):
    result: str

@app.post("/generate", response_model=GenerateResponse)
async def generate(data: GenerateRequest, background_tasks: BackgroundTasks):
    image = data.image.strip()
    mask = data.mask.strip()
    metadata = data.metadata.strip()

    if not image or not mask or not metadata:
        raise HTTPException(status_code=400, detail="All fields (image, mask, metadata) are required.")

    # Simulated async processing
    #result = await process_fields(image, mask, metadata)
    background_tasks.add_task(process_fields, image, mask, metadata)
    #return GenerateResponse(result=result)
    return GenerateResponse(result="Job accepted. Processing starts.")

async def process_fields(image: str, mask: str, metadata: str) -> str:
    import asyncio
    download_file(image)
    download_file(mask)
    download_file(metadata)
    clean_cache()
    shutil.copy2(Path(LOCAL_S3DATA_DIR) / Path(image), comfyui_dir / Path('input') / Path(image))
    generate_glb(image)
    upload_file(str(comfyui_dir / Path('output') / Path('3D') / Path('Hy3D_textured_00001_.glb')))
    return f"Processed: {image[::-1]} | {mask[::-1]} | {metadata[::-1]}"
