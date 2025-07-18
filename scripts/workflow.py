import websocket
import uuid
import json
import urllib.request
import urllib.parse
from pathlib import Path

def queue_prompt(prompt, server_address, client_id):
    p = {"prompt": prompt, "client_id": client_id}
    data = json.dumps(p).encode('utf-8')
    req =  urllib.request.Request("http://{}/prompt".format(server_address), data=data)
    return json.loads(urllib.request.urlopen(req).read())

def start_workflow(ws, prompt, server_address, client_id):
    prompt_id = queue_prompt(prompt, server_address, client_id)['prompt_id']
    total_nodes = len(list(prompt.keys()))
    finished_nodes = []
    while True:
        out = ws.recv()
        if isinstance(out, str):
            message = json.loads(out)
            if message['type'] == 'progress':
                data = message['data']
                current_step = data['value']
            if message['type'] == 'execution_cached':
                data = message['data']
                for itm in data['nodes']:
                    if itm not in finished_nodes:
                        finished_nodes.append(itm)
            if message['type'] == 'executing':
                data = message['data']
                if data['node'] is None and data['prompt_id'] == prompt_id:
                    break
                if data['node'] not in finished_nodes:
                    finished_nodes.append(data['node'])
        else:
            continue

def run_workflow(prompt, server_address = "127.0.0.1:8188", client_id = str(1)):
    ws = websocket.WebSocket()
    ws.connect("ws://{}/ws?clientId={}".format(server_address, client_id))
    start_workflow(ws, prompt, server_address, client_id)
    ws.close()
