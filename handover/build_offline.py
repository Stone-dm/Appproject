"""Build the retained offline source into a new portable runtime directory."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OFFLINE = ROOT.parent / 'work/submission-local'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--offline-gradle', action='store_true')
    args = parser.parse_args()
    if not (OFFLINE / 'backend/offline_server.py').is_file():
        parser.error('Extract the complete handover archive first; work/submission-local is not part of the main Git repository.')
    target = args.output.resolve()
    if target.exists():
        parser.error('Output already exists; select a new directory.')
    python = ROOT / 'services/api/.venv/Scripts/python.exe'
    if not python.exists():
        parser.error('Run handover/manage.py setup first.')
    env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1', PYTHONIOENCODING='utf-8')
    if args.java_home:
        env['JAVA_HOME'] = str(args.java_home.resolve())
    java = env.get('JAVA_HOME')
    if not java or not (Path(java) / 'bin/java.exe').is_file():
        parser.error('Set JAVA_HOME or --java-home to JDK/JBR 21.')
    # Probe required packaging dependency before writing any output directory.
    subprocess.run([str(python), '-c', 'import PyInstaller'], check=True, env=env)
    target.mkdir(parents=True)
    build_dir = target / 'build-work'
    runtime = target / '运行文件'
    runtime.mkdir()
    command = [str(OFFLINE / 'android/gradlew.bat'), ':app:assembleOffline', ':app:testOfflineUnitTest', '--console=plain', '--no-daemon']
    if args.offline_gradle:
        command.append('--offline')
    subprocess.run(command, cwd=OFFLINE / 'android', env=env, check=True)
    subprocess.run([str(python), '-m', 'PyInstaller', '--noconfirm', '--clean',
                    '--distpath', str(runtime), '--workpath', str(build_dir),
                    str(OFFLINE / '启动本地演示.spec')], cwd=OFFLINE, env=env, check=True)
    apk = OFFLINE / 'android/app/build/outputs/apk/offline/app-offline.apk'
    shutil.copy2(apk, runtime / '机巧江湖_本地演示.apk')
    # This guide uses paths local to the generated runtime directory.
    guide = ROOT / 'handover/离线运行说明.md'
    if guide.exists():
        shutil.copy2(guide, runtime / '离线运行说明.md')
    files = []
    for path in sorted(runtime.iterdir()):
        if path.is_file():
            with path.open('rb') as stream:
                digest = hashlib.file_digest(stream, 'sha256').hexdigest()
            files.append({'name': path.name, 'bytes': path.stat().st_size, 'sha256': digest})
    (target / 'build-manifest.json').write_text(json.dumps({
        'source': 'work/submission-local current source', 'files': files,
        'notice': 'Built with development Android signing. No production signing keys or online model credentials.',
    }, ensure_ascii=False, indent=2), encoding='utf-8')
    print('Runtime directory:', runtime)
    print('Build intermediates are retained in build-work for diagnosis; they are not required at runtime.')


if __name__ == '__main__':
    main()
