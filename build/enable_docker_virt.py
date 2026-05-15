import subprocess, sys, os
sys.stdout.reconfigure(encoding='utf-8')

# Create batch
bat_content = '''@echo off
dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
dism /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
echo ALL_DONE
'''

bat_path = r'D:\Agent工作区\Claude Code工作区\综合实训项目\build\enable_features.bat'
with open(bat_path, 'w', encoding='ascii') as f:
    f.write(bat_content)
print(f"Created: {bat_path}")

# Run with elevation via PowerShell
ps_cmd = f'Start-Process -FilePath cmd.exe -ArgumentList "/c {bat_path}" -Verb RunAs -Wait'
result = subprocess.run(
    ["powershell", "-NoProfile", "-Command", ps_cmd],
    capture_output=True, text=True, timeout=120
)
print("STDOUT:", result.stdout[:500])
if result.stderr:
    print("STDERR:", result.stderr[:500])
print("Exit:", result.returncode)

# Check if features are now enabled
result2 = subprocess.run(["wsl", "--status"], capture_output=True)
text2 = result2.stdout.decode('utf-16-le', errors='replace')
print("\nWSL Status:", text2.strip())
