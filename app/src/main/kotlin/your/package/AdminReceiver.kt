package your.package

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device Admin включён", Toast.LENGTH_SHORT).show()
    }
    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device Admin отключён", Toast.LENGTH_SHORT).show()
    }
}
