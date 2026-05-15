
Set UAC = CreateObject("Shell.Application")
UAC.ShellExecute "cmd.exe", "/c dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart && dism /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart && echo ALL_DONE > D:\docker_virt_result.txt", "", "runas", 1
