import os
import re

# File paths
top_header = "app/src/main/java/com/example/ui/components/TopHeader.kt"
server_modal = "app/src/main/java/com/example/ui/components/ServerSelectionModal.kt"
home_screen = "app/src/main/java/com/example/ui/screens/HomeScreen.kt"
ad_modal = "app/src/main/java/com/example/ui/components/AdRewardModal.kt"
settings_screen = "app/src/main/java/com/example/ui/screens/SettingsScreen.kt"

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f: content = f.read()
    with open(filepath, 'w') as f: f.write(content.replace(old, new))

# 1
replace_in_file(top_header, 
    "stringResource(R.string.str_ip_copied_ipinfo_publicip_6)",
    "stringResource(R.string.str_ip_copied_ipinfo_publicip_6, ipInfo.publicIp)"
)

# 2
replace_in_file(server_modal, 
    "stringResource(R.string.str_server_nearest_server_distance_13)",
    "stringResource(R.string.str_server_nearest_server_distance_13, server.distanceKm, server.subLocation)"
)

# 3
replace_in_file(home_screen, 
    "stringResource(R.string.str_bangkok_selectedserver_subloca_16)",
    "stringResource(R.string.str_bangkok_selectedserver_subloca_16, selectedServer.subLocation)"
)

# 4
replace_in_file(ad_modal, 
    "stringResource(R.string.str_loading_sponsor_progressanim_1_19)",
    "stringResource(R.string.str_loading_sponsor_progressanim_1_19, (progressAnim * 100).toInt())"
)

# 5
replace_in_file(home_screen, 
    "stringResource(R.string.str_routing_nearest_selectedserver_81)",
    "stringResource(R.string.str_routing_nearest_selectedserver_81, selectedServer.distanceKm, selectedServer.subLocation)"
)

# 6
replace_in_file(settings_screen, 
    "stringResource(R.string.str_version_v_com_example_buildcon_111)",
    "stringResource(R.string.str_version_v_com_example_buildcon_111, com.example.BuildConfig.VERSION_NAME, com.example.BuildConfig.VERSION_CODE)"
)

# Now fix the XMLs
def fix_xml(filepath):
    with open(filepath, 'r') as f: content = f.read()
    
    content = content.replace("IP Copied: ${ipInfo.publicIp}", "IP Copied: %1$s")
    content = content.replace("คัดลอก IP: ${ipInfo.publicIp}", "คัดลอก IP: %1$s")
    
    content = content.replace("Server: Nearest ${server.distanceKm}km - ${server.subLocation}", "Server: Nearest %1$dkm - %2$s")
    content = content.replace("เซิร์ฟเวอร์: ใกล้ที่สุด ${server.distanceKm}กม. - ${server.subLocation}", "เซิร์ฟเวอร์: ใกล้ที่สุด %1$dกม. - %2$s")
    
    content = content.replace("Bangkok → ${selectedServer.subLocation}", "Bangkok → %1$s")
    content = content.replace("กรุงเทพฯ → ${selectedServer.subLocation}", "กรุงเทพฯ → %1$s")
    
    content = content.replace("Loading sponsor... ${(progressAnim * 100).toInt()}%", "Loading sponsor... %1$d%%")
    content = content.replace("กำลังโหลดสปอนเซอร์... ${(progressAnim * 100).toInt()}%", "กำลังโหลดสปอนเซอร์... %1$d%%")
    
    content = content.replace("Routing: Nearest ${selectedServer.distanceKm}km (${selectedServer.subLocation})", "Routing: Nearest %1$dkm (%2$s)")
    content = content.replace("เส้นทาง: ใกล้ที่สุด ${selectedServer.distanceKm}กม. (${selectedServer.subLocation})", "เส้นทาง: ใกล้ที่สุด %1$dกม. (%2$s)")
    
    content = content.replace("Version: v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})", "Version: v%1$s (Build %2$d)")
    content = content.replace("เวอร์ชัน: v${com.example.BuildConfig.VERSION_NAME} (บิลด์ ${com.example.BuildConfig.VERSION_CODE})", "เวอร์ชัน: v%1$s (บิลด์ %2$d)")

    with open(filepath, 'w') as f: f.write(content)

fix_xml("app/src/main/res/values/strings_gen.xml")
fix_xml("app/src/main/res/values-th/strings_gen.xml")

print("Fixed interpolated strings.")
