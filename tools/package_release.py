from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil, hashlib, json

P=Path(__file__).resolve().parents[1]
O=P/'dist'
O.mkdir(exist_ok=True)
jar=P/'build/libs/wans_magnetic_spellbooks-1.0.1.jar'
shutil.copy2(jar,O/jar.name)
staged=P/'verification/server/mods'/jar.name
if staged.is_file():
    with ZipFile(jar) as final,ZipFile(staged) as tested:
        classes=[s for s in final.namelist() if s.endswith('.class')]
        assert all(final.read(c)==tested.read(c) for c in classes),'Tested Java bytecode changed'
source=O/'wans_magnetic_spellbooks-1.0.1-source.zip'
include=['src','gradle','tools']
files=[p for name in include for p in (P/name).rglob('*') if p.is_file() and '__pycache__' not in p.parts]
files += [P/name for name in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','README.md','LICENSE.txt','THIRD_PARTY_NOTICES.md','.gitignore','extraction-manifest.json']]
files += [P/'verification'/name for name in ['static-report.json','runtime-report.md','server-dependencies.json']]
with ZipFile(source,'w',ZIP_DEFLATED) as z:
    for p in sorted(files):z.write(p,'wans-magnetic-spellbooks/'+p.relative_to(P).as_posix())
shutil.copy2(P/'README.md',O/'README.md')
shutil.copy2(P/'verification/runtime-report.md',O/'runtime-report.md')
receipt={p.name:{'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in [O/jar.name,source]}
(O/'release.json').write_text(json.dumps(receipt,indent=2),encoding='utf-8')
print(json.dumps(receipt,indent=2))
