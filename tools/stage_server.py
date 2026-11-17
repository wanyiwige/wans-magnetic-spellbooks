from pathlib import Path
from zipfile import ZipFile, BadZipFile
from collections import deque
import tomllib, shutil, json

project=Path(__file__).resolve().parents[1]
instance=project.parent
server=project/'verification/server'
server.mkdir(parents=True,exist_ok=True)
template=instance/'wans-iron-expansion/development-checks/pure-darkness-dev.13/isolated-server'
for name in ['forge_args.txt','eula.txt']:
    shutil.copy2(template/name,server/name)
(server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25679\nonline-mode=false\nlevel-name=verification-world\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\nmax-tick-time=120000\nview-distance=3\nsimulation-distance=3\n',encoding='utf-8')
byid={}; meta={}
for p in (instance/'mods').glob('*.jar'):
    try:
        with ZipFile(p) as z: d=tomllib.loads(z.read('META-INF/mods.toml').decode('utf-8-sig'))
    except (KeyError,ValueError,BadZipFile): continue
    meta[p]=d
    for mod in d.get('mods',[]):byid.setdefault(mod['modId'],p)
queue=deque(['irons_spellbooks','alexscaves','citadel','geckolib','curios','traveloptics'])
selected=set(); missing=set()
while queue:
    mid=queue.popleft()
    if mid in selected or mid in {'minecraft','forge','java','mixinextras'}:continue
    if mid not in byid:missing.add(mid);continue
    selected.add(mid)
    for d in meta[byid[mid]].get('dependencies',{}).get(mid,[]):
        if d.get('mandatory') and d.get('side','BOTH')!='CLIENT':queue.append(d['modId'])
assert not missing,missing
mods=server/'mods';mods.mkdir(exist_ok=True)
for p in {byid[k] for k in selected}:shutil.copy2(p,mods/p.name)
jar=project/'build/libs/wans_magnetic_spellbooks-1.0.1.jar'
shutil.copy2(jar,mods/jar.name)
assert 'alex_caves_spellsbook' not in selected
(project/'verification/server-dependencies.json').write_text(json.dumps(sorted(selected),indent=2),encoding='utf-8')
print('Staged:',', '.join(sorted(selected)))
