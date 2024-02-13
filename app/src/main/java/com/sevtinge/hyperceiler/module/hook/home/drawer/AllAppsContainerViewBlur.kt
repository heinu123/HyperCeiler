/*
  * This file is part of HyperCeiler.

  * HyperCeiler is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

  * Copyright (C) 2023-2024 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.module.hook.home.drawer

import android.app.Application
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.ViewSwitcher
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.sevtinge.hyperceiler.module.base.BaseHook
import com.sevtinge.hyperceiler.utils.devicesdk.getCornerRadiusTop
import com.sevtinge.hyperceiler.utils.getObjectField
import com.sevtinge.hyperceiler.utils.hookBeforeMethod
import com.sevtinge.hyperceiler.utils.blur.zhenxiang.BlurFrameLayout
import com.sevtinge.hyperceiler.utils.blur.zhenxiang.model.CornersRadius


object AllAppsContainerViewBlur : BaseHook() {
    override fun init() {
        Application::class.java.hookBeforeMethod("attach", Context::class.java) {
            EzXHelper.initHandleLoadPackage(lpparam)
            EzXHelper.setLogTag(TAG)
            EzXHelper.setToastTag(TAG)
            EzXHelper.initAppContext(it.args[0] as Context)

            loadClass("com.miui.home.launcher.allapps.BaseAllAppsContainerView").methodFinder().filter {
                name == "onFinishInflate"
            }.toList().createHooks {
                after { hookParam ->
                    val mCategoryContainer = hookParam.thisObject.getObjectField("mCategoryContainer") as ViewSwitcher
                    val appsView = mCategoryContainer.parent as RelativeLayout
                    val blur = BlurFrameLayout(mCategoryContainer.context)
                    val radius = getCornerRadiusTop().toFloat()
                    if (Build.VERSION.SDK_INT >= 31) {
                        blur.blurController.apply {
                            cornerRadius = CornersRadius.custom(radius, radius, 0f, 0f)
                        }
                    }
                    val view = View(mCategoryContainer.context)
                    blur.addView(view)
                    (view.layoutParams as FrameLayout.LayoutParams).apply {
                        width = FrameLayout.LayoutParams.MATCH_PARENT
                        height = FrameLayout.LayoutParams.MATCH_PARENT
                    }
                    appsView.addView(blur, 0)

                    loadClass("com.miui.home.launcher.allapps.BaseAllAppsContainerView").methodFinder().filter {
                        name == "onResume"
                    }.toList().createHooks {
                        after {
                            blur.refreshDrawableState()
                        }
                    }
                }
            }
        }
    }
}
