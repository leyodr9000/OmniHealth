package com.example.omnihealth

import android.app.Application

/**
 * 应用入口。M1 起承载 Room / Repository / DataStore 的手动依赖注入容器。
 */
class OmniHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
