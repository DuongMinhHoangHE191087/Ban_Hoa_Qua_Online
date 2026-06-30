import os

def clean_tailwind_plugins():
    jsp_dir = r"d:\DMHoang\Project_GitHub\Ban_Hoa_Qua_Online\web\WEB-INF\jsp"
    
    replacements = [
        ('tailwind.js?plugins=forms,container-queries', 'tailwind.js'),
        ('tailwind.js?plugins=forms', 'tailwind.js'),
        ('tailwind.js?plugins=container-queries', 'tailwind.js')
    ]
    
    count = 0
    for root, dirs, files in os.walk(jsp_dir):
        for f in files:
            if f.endswith('.jsp'):
                path = os.path.join(root, f)
                with open(path, 'r', encoding='utf-8') as file:
                    content = file.read()
                
                modified = False
                for target, replacement in replacements:
                    if target in content:
                        content = content.replace(target, replacement)
                        modified = True
                
                if modified:
                    with open(path, 'w', encoding='utf-8') as file:
                        file.write(content)
                    print(f"Cleaned tailwind plugins in {f}")
                    count += 1
                    
    print(f"Completed! Cleaned {count} files.")

if __name__ == '__main__':
    clean_tailwind_plugins()
