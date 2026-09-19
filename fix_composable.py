import os
import re

def fix_file(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f: content = f.read()
    
    # replace stringResource inside curly braces where context is available.
    # Actually, if we just use context.getString() it's safer.
    content = content.replace("stringResource(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)", "context.getString(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)")
    
    # For ShareDetailModal
    content = content.replace('stringResource(R.string.str_detailed_report_copied_to_clip_33)', 'context.getString(R.string.str_detailed_report_copied_to_clip_33)')
    
    # For HistoryScreen.kt
    content = content.replace('stringResource(R.string.str_export_zipspeed_test_history_84)', 'context.getString(R.string.str_export_zipspeed_test_history_84)')
    
    # Fix import conflicts in LoginScreen and SplashScreen
    # find lines with import R and remove one of them if it's duplicated, or just remove com.example.R if it conflicts with another R
    content = re.sub(r'import com\.example\.R\n.*import com\.example\.R', 'import com.example.R', content)
    
    # check if import com.example.R and another R is imported
    # We will just remove import com.example.R if it conflicts, but wait, usually R is in the same package so we don't even need to import it.
    content = re.sub(r'import com\.example\.R\n', '', content)
    
    with open(filepath, 'w') as f: f.write(content)

fix_file("app/src/main/java/com/example/ui/components/IpAddressCard.kt")
fix_file("app/src/main/java/com/example/ui/components/ShareDetailModal.kt")
fix_file("app/src/main/java/com/example/ui/screens/HistoryScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/SplashScreen.kt")

print("Fixed composable and import issues.")
