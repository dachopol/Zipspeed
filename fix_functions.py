import os

def fix_share(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f: content = f.read()
    content = content.replace("stringResource(R.string.str_share_speed_test_details_32)", "context.getString(R.string.str_share_speed_test_details_32)")
    with open(filepath, 'w') as f: f.write(content)

def fix_history(filepath):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f: content = f.read()
    content = content.replace("stringResource(R.string.str_export_zipspeed_test_history_84)", "context.getString(R.string.str_export_zipspeed_test_history_84)")
    with open(filepath, 'w') as f: f.write(content)

fix_share("app/src/main/java/com/example/ui/components/ShareDetailModal.kt")
fix_history("app/src/main/java/com/example/ui/screens/HistoryScreen.kt")

print("Fixed")
