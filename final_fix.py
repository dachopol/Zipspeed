import os
import re

def fix_imports(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f: content = f.read()
    if 'import com.example.R' not in content:
        content = content.replace('import androidx.compose.ui.res.stringResource\n', 'import androidx.compose.ui.res.stringResource\nimport com.example.R\n')
    
    # Fix composable issues in onClick
    content = content.replace("val msg = stringResource(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)", "val msg = context.getString(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)")
    content = content.replace("val msg = stringResource(R.string.str_ip_copied_ipinfo_publicip_6)", "val msg = context.getString(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)")
    
    with open(filepath, 'w') as f: f.write(content)

fix_imports("app/src/main/java/com/example/ui/components/IpAddressCard.kt")
fix_imports("app/src/main/java/com/example/ui/components/ShareDetailModal.kt")
fix_imports("app/src/main/java/com/example/ui/screens/HistoryScreen.kt")
fix_imports("app/src/main/java/com/example/ui/screens/LoginScreen.kt")
fix_imports("app/src/main/java/com/example/ui/screens/SplashScreen.kt")

print("Done")
