package com.llew.method.time.printer.plugin

import com.android.build.gradle.AppExtension
import com.llew.method.time.printer.extension.MethodTimePrintExtension
import com.llew.method.time.printer.transform.MethodTimePrintTransform
import com.llew.method.time.printer.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * MethodTimePrinter核心插件
 * <br/><br/>
 *
 * @author llew
 * @date 2017/12/15
 */

public class MethodTimePrintPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        def android = project.extensions.findByType(AppExtension.class)

        project.extensions.create("methodTimePrinterConfig", MethodTimePrintExtension)
        MethodTimePrintExtension extension = project.methodTimePrinterConfig

        Logger.enable = extension.logEnable
        android.registerTransform(new MethodTimePrintTransform(android, project, extension))
    }
}
