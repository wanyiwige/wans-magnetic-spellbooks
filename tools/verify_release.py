from pathlib import Path
from zipfile import ZipFile
import re, json, hashlib, tomllib

P=Path(__file__).resolve().parents[1]
NS='wans_magnetic_spellbooks'
R=P/'src/main/resources'
J=P/'src/main/java'
manifest=json.loads((P/'extraction-manifest.json').read_text(encoding='utf-8'))
def source(name):return (J/'cn/wanyiwige/magneticspellbooks/registry'/f'{name}.java').read_text(encoding='utf-8')
items=re.findall(r'(?:ITEMS\.register|= register)\(\s*"([^"]+)"',source('ModItems'))
spells=re.findall(r'(?:SPELLS\.register|= register)\(\s*"([^"]+)"',source('ModSpells'))
assert len(items)==10,items
assert len(spells)==16,spells
for item in items:assert (R/f'assets/{NS}/models/item/{item}.json').is_file(),item
for spell in spells:assert (R/f'assets/{NS}/textures/gui/spell_icons/{spell}.png').is_file(),spell
for lang in ['zh_cn','en_us']:
    d=json.loads((R/f'assets/{NS}/lang/{lang}.json').read_text(encoding='utf-8'))
    for item in items:assert f'item.{NS}.{item}' in d,(lang,item)
    for spell in spells:assert f'spell.{NS}.{spell}' in d,(lang,spell)
for p in R.rglob('*.json'):
    s=p.read_text(encoding='utf-8');d=json.loads(s)
    assert 'alex_caves_spellsbook' not in s,p
    if '/models/' in p.as_posix():
        for ref in d.get('textures',{}).values():
            if ref.startswith(NS+':'):
                target=R/f'assets/{NS}/textures/{ref.split(":",1)[1]}.png'
                assert target.is_file(),(p,target)
        parent=d.get('parent','')
        if parent.startswith(NS+':'):assert (R/f'assets/{NS}/models/{parent.split(":",1)[1]}.json').is_file(),p
for p in J.rglob('*.java'):
    s=p.read_text(encoding='utf-8')
    assert 'cn.wanyiwige.alexcavesspellsbook' not in s,p
    assert 'alex_caves_spellsbook' not in s,p
    for path in re.findall(r'"((?:textures/|geo/|animations/)[^" ]+\.(?:png|json))"',s):
        if 'alexscaves' in s and path in {'textures/entity/boundroid.png','textures/entity/tesla_bulb.png'}:continue
        if path=='textures/misc/white.png':continue
        assert (R/f'assets/{NS}/{path}').is_file(),(p,path)
cfg=json.loads((R/f'{NS}.mixins.json').read_text())
for cls in cfg['mixins']:
    assert (J/(cfg['package']+'.'+cls).replace('.','/')).with_suffix('.java').is_file(),cls
focus=json.loads((R/'data/irons_spellbooks/tags/items/school_focus.json').read_text())
assert '#'+NS+':magnetic_focus' in focus['values']
jar=P/f'build/libs/{NS}-1.0.1.jar'
with ZipFile(jar) as z:
    metadata=tomllib.loads(z.read('META-INF/mods.toml').decode())
    deps=metadata['dependencies'][NS]
    assert metadata['mods'][0]['modId']==NS
    assert not {'alex_caves_spellsbook','foolish','roacw','fantasy_ending','twilightforest'} & {d['modId'] for d in deps}
    assert NS+'.mixins.json' in z.read('META-INF/MANIFEST.MF').decode()
    for p in R.rglob('*'):
        if p.is_file():assert p.relative_to(R).as_posix() in z.namelist(),p
report={'jar':str(jar),'size':jar.stat().st_size,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'spells':spells,'items':items,'java_files':len(list(J.rglob('*.java'))),'resource_files':len([p for p in R.rglob('*') if p.is_file()]),'checks':'passed'}
(P/'verification').mkdir(exist_ok=True)
(P/'verification/static-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
