"""Portable handover commands. Run with Python 3.13 from any directory."""
from __future__ import annotations

import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import secrets
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
API = ROOT / 'services/api'
OFFLINE = ROOT.parent / 'work/submission-local'
PYTHON = API / '.venv/Scripts/python.exe'


def run(command, *, cwd=ROOT, env=None):
    subprocess.run([str(value) for value in command], cwd=cwd, env=env, check=True)


def api_python():
    if not PYTHON.is_file():
        raise SystemExit('Run: py -3.13 handover/manage.py setup')
    return PYTHON


def initialize_config(args):
    target = API / '.env'
    if not target.exists():
        content = (API / '.env.example').read_text(encoding='utf-8-sig')
        values = {name: secrets.token_urlsafe(48) for name in (
            'JIANGHU_JWT_SECRET', 'JIANGHU_PHONE_LOOKUP_KEY',
            'JIANGHU_VERIFICATION_CODE_KEY', 'JIANGHU_INTERNAL_WORKER_TOKEN',
        )}
        values['JIANGHU_PHONE_ENCRYPTION_KEY'] = base64.urlsafe_b64encode(secrets.token_bytes(32)).decode()
        values['JIANGHU_CONVERSATION_COACH_PROVIDER'] = 'disabled'
        lines = [line.split('=', 1)[0] + '=' + values[line.split('=', 1)[0]]
                 if '=' in line and line.split('=', 1)[0] in values else line
                 for line in content.splitlines()]
        with target.open('x', encoding='utf-8', newline='\n') as stream:
            stream.write('\n'.join(lines) + '\n')
        print('Created backend local configuration with independent secrets; values are not displayed.')
    else:
        print('Existing backend .env preserved.')
    sdk = args.sdk or os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT')
    if not sdk and os.environ.get('LOCALAPPDATA'):
        candidate = Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk'
        if candidate.is_dir():
            sdk = str(candidate)
    if not sdk or not Path(sdk).is_dir():
        print('Android SDK not found. Re-run configure --sdk PATH after installing SDK 35.')
        return
    sdk_value = str(Path(sdk).resolve()).replace('\\', '/').replace(':', '\\:')
    for android in (ROOT / 'android', OFFLINE / 'android'):
        if not android.is_dir():
            continue
        local = android / 'local.properties'
        if local.exists():
            print('Preserved:', os.path.relpath(local, ROOT))
        else:
            with local.open('x', encoding='utf-8', newline='\n') as stream:
                stream.write('sdk.dir=' + sdk_value + '\nAUTH_BASE_URL=http://10.0.2.2:8010/\n'
                             'TERMS_VERSION=2026-09-r2\nPRIVACY_VERSION=2026-08\n')
            print('Created:', os.path.relpath(local, ROOT))


def setup(args):
    if os.name != 'nt' or sys.version_info[:2] != (3, 13):
        raise SystemExit('This reproducible setup targets Windows x64 / Python 3.13. Use py -3.13.')
    if not PYTHON.exists():
        run([sys.executable, '-m', 'venv', API / '.venv'])
    install = [PYTHON, '-m', 'pip', 'install', '-r', ROOT / 'handover/requirements-win-py313.txt']
    wheels = ROOT / 'handover/wheels-win-py313'
    if not wheels.is_dir():
        wheels = ROOT.parent / 'handover/wheels-win-py313'
    if args.online:
        print('Installing from configured Python package index.')
    elif wheels.is_dir():
        install += ['--no-index', '--find-links', wheels]
    else:
        raise SystemExit('Bundled wheels missing. Supply them or explicitly add --online.')
    run(install)
    # Editable installation with all dependencies already installed from the lock/wheelhouse.
    run([PYTHON, '-m', 'pip', 'install', '--no-deps', '--no-build-isolation', '-e', API])
    initialize_config(args)
    print('Setup complete. Next: py -3.13 handover/manage.py test')


def doctor(_args):
    checks = {
        'python': sys.version.split()[0], 'windows': os.name == 'nt',
        'api_venv': PYTHON.is_file(), 'backend_config_exists': (API / '.env').is_file(),
        'android_local_config_exists': (ROOT / 'android/local.properties').is_file(),
        'java_on_path': bool(shutil.which('java')), 'java_home_set': bool(os.environ.get('JAVA_HOME')),
        'docker_on_path': bool(shutil.which('docker')),
        'offline_executable': (ROOT.parent / '体验运行/启动本地演示.exe').is_file(),
        'android_wrapper': (ROOT / 'android/gradle/wrapper/gradle-wrapper.jar').is_file(),
    }
    print(json.dumps(checks, ensure_ascii=False, indent=2))
    print('This reports prerequisites only; it does not prove API, Docker or Android is running.')


def test(_args):
    # app.main also creates the default app during import. Keep that import away
    # from a developer's .env; each formal test supplies its own isolated settings.
    env = {key: value for key, value in os.environ.items()
           if not key.upper().startswith('JIANGHU_')}
    env.update(PYTHONDONTWRITEBYTECODE='1', PYTHONIOENCODING='utf-8',
               PYTHONPATH=str(API))
    with tempfile.TemporaryDirectory(prefix='jiqiao-tests-') as temporary:
        run([api_python(), '-m', 'pytest', '-c', API / 'pyproject.toml',
             API / 'tests', '-p', 'no:cacheprovider'], cwd=temporary, env=env)


def build_android(args):
    android = OFFLINE / 'android' if args.variant == 'offline' else ROOT / 'android'
    if not android.is_dir():
        raise SystemExit('Offline Android source is supplied in the complete handover archive under work/submission-local.')
    env = dict(os.environ)
    if args.java_home:
        env['JAVA_HOME'] = str(Path(args.java_home).resolve())
    if not env.get('JAVA_HOME') or not (Path(env['JAVA_HOME']) / 'bin/java.exe').is_file():
        raise SystemExit('Supply --java-home pointing to JDK/JBR 21, or set JAVA_HOME.')
    suffix = args.variant.capitalize()
    command = [android / 'gradlew.bat', ':app:assemble' + suffix, ':app:test' + suffix + 'UnitTest', '--console=plain', '--no-daemon']
    if args.variant == 'debug':
        command.insert(3, ':app:lintDebug')
    if args.offline:
        command.append('--offline')
    run(command, cwd=android, env=env)


def run_offline(args):
    if not (OFFLINE / 'backend/offline_server.py').is_file():
        raise SystemExit('Offline source is supplied in the complete handover archive under work/submission-local.')
    data = (args.data_dir or ROOT / 'runtime-data/offline').resolve()
    env = dict(os.environ, PYTHONDONTWRITEBYTECODE='1', PYTHONIOENCODING='utf-8')
    # The offline adapter intentionally imports its own retained app/ modules.
    env['PYTHONPATH'] = str(OFFLINE / 'backend')
    run([api_python(), OFFLINE / 'backend/offline_server.py', '--headless', '--port', args.port,
         '--data-dir', data], cwd=OFFLINE / 'backend', env=env)


def verify_package(_args):
    path = ROOT / 'handover/package-manifest.json'
    if not path.exists():
        raise SystemExit('Package manifest is generated in the final handover package, not the development workspace.')
    records = json.loads(path.read_text(encoding='utf-8'))['files']
    failures = []
    for row in records:
        relative = Path(row['path'])
        candidate = (ROOT / relative).resolve()
        if relative.is_absolute() or not candidate.is_relative_to(ROOT):
            raise SystemExit('Unsafe manifest path')
        if not candidate.is_file():
            failures.append({'path': row['path'], 'reason': 'missing'})
            continue
        with candidate.open('rb') as stream:
            digest = hashlib.file_digest(stream, 'sha256').hexdigest()
        if digest != row['sha256']:
            failures.append({'path': row['path'], 'reason': 'changed'})
    print(json.dumps({'checked': len(records), 'failures': failures}, ensure_ascii=False, indent=2))
    if failures:
        raise SystemExit(1)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    for name, handler in (('doctor', doctor), ('test', test), ('verify-package', verify_package)):
        commands.add_parser(name).set_defaults(handler=handler)
    for name, handler in (('setup', setup), ('configure', initialize_config)):
        command = commands.add_parser(name)
        command.add_argument('--sdk', type=Path)
        if name == 'setup':
            command.add_argument('--online', action='store_true')
        command.set_defaults(handler=handler)
    command = commands.add_parser('build-android')
    command.add_argument('--variant', choices=('debug', 'demo', 'acceptance', 'offline'), default='debug')
    command.add_argument('--java-home', type=Path)
    command.add_argument('--offline', action='store_true', help='Use only already cached Gradle dependencies')
    command.set_defaults(handler=build_android)
    command = commands.add_parser('run-offline')
    command.add_argument('--port', type=int, default=8011)
    command.add_argument('--data-dir', type=Path)
    command.set_defaults(handler=run_offline)
    args = parser.parse_args()
    args.handler(args)


if __name__ == '__main__':
    main()
