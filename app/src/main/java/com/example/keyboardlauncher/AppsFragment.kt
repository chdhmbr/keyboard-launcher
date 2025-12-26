package com.example.keyboardlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class AppsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private var apps = emptyList<AppInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_apps, container, false)
        recyclerView = view.findViewById(R.id.appsRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apps = loadApps()
        adapter = AppAdapter(apps) { app ->
            launchApp(app.packageName)
        }

        recyclerView.layoutManager = NoScrollGridLayoutManager(
            requireContext(),
            calculateSpanCount(apps.size)
        )

        recyclerView.adapter = adapter
    }

    private fun loadApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                AppInfo(
                    name = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    private fun launchApp(packageName: String) {
        val pm = requireContext().packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) startActivity(intent)
    }

    private fun calculateSpanCount(appCount: Int): Int {
        return when {
            appCount <= 20 -> 4
            appCount <= 40 -> 5
            appCount <= 60 -> 6
            else -> 7
        }
    }
}
