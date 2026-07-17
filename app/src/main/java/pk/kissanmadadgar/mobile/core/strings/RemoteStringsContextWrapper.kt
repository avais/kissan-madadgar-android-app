package pk.kissanmadadgar.mobile.core.strings

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources

// Wraps a Context so every existing stringResource()/getString()/getText() call site
// transparently resolves through RemoteStringsStore first, falling back to the real bundled
// resource when there's no remote override for that key. This lets the backend-driven string
// catalog take effect without touching any of the existing call sites across the app, so none
// of the current screens/flows need to change (or can be broken by a missed/typo'd call site).
class RemoteStringsContextWrapper private constructor(
    base: Context,
    private val store: RemoteStringsStore
) : ContextWrapper(base) {

    private val overriddenResources: Resources by lazy { buildResources(base) }

    @Suppress("DEPRECATION")
    private fun buildResources(base: Context): Resources {
        val baseResources = base.resources
        return object : Resources(base.assets, baseResources.displayMetrics, baseResources.configuration) {
            override fun getString(id: Int): String = resolve(id) ?: super.getString(id)

            override fun getString(id: Int, vararg formatArgs: Any): String {
                val override = resolve(id) ?: return super.getString(id, *formatArgs)
                return try {
                    String.format(override, *formatArgs)
                } catch (e: Exception) {
                    // Overridden text doesn't match the expected format placeholders (e.g. a
                    // bad value from the backend) — fall back to the known-good bundled string
                    // rather than crashing the caller.
                    super.getString(id, *formatArgs)
                }
            }

            override fun getText(id: Int): CharSequence = resolve(id) ?: super.getText(id)
        }
    }

    private fun resolve(id: Int): String? {
        val name = try {
            baseContext.resources.getResourceEntryName(id)
        } catch (e: Resources.NotFoundException) {
            null
        }
        return name?.let { store.get(it) }
    }

    override fun getResources(): Resources = overriddenResources

    companion object {
        fun wrap(base: Context): ContextWrapper =
            RemoteStringsContextWrapper(base, RemoteStringsStore.getInstance(base))
    }
}
