package com.llew.method.time.printer.transform

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.llew.method.time.printer.extension.MethodTimePrintExtension
import com.llew.method.time.printer.helper.MethodTimePrintHelper
import com.llew.method.time.printer.injector.MethodTimePrintInjector
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

/**
 * MethodTimePrint转换类
 * <br/><br/>
 *
 * @author llew
 * @date 2017/12/15
 */

public class MethodTimePrintTransform extends Transform {

    private static final String DEFAULT_NAME = "MethodTimePrintTransform"

    private MethodTimePrintExtension mExtension
    private AppExtension mAndroid
    private Project mProject

    public MethodTimePrintTransform(AppExtension android, Project project, MethodTimePrintExtension extension) {
        mAndroid = android
        mExtension = extension
        mProject = project
        MethodTimePrintInjector.init(android, project, extension)
    }

    @Override
    public String getName() {
        return DEFAULT_NAME
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    public boolean isIncremental() {
        return false
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (null == transformInvocation) {
            throw new IllegalArgumentException("transformInvocation is null !!!")
        }
        Collection<TransformInput> inputs = transformInvocation.inputs
        if (null == inputs) {
            throw new IllegalArgumentException("TransformInput is null !!!")
        }

        TransformOutputProvider outputProvider = transformInvocation.outputProvider;

        if (null == outputProvider) {
            throw new IllegalArgumentException("TransformInput is null !!!")
        }

        deleteMethodTimePrintHelperFromBuildDir()

        inputs.each {
            it.directoryInputs.each {
                appendClassPath(it.file)
            }
            it.jarInputs.each {
                appendClassPath(it.file)
            }
        }


        for (TransformInput input : inputs) {

            if (null == input) continue;

            for (DirectoryInput dirInput : input.directoryInputs) {

                if (dirInput) {

                    if (null != dirInput.file && dirInput.file.exists()) {

                        if (isEnable()) {
                            MethodTimePrintInjector.injector.inject(dirInput.file)
                        }

                        File dest = outputProvider.getContentLocation(dirInput.getName(), dirInput.getContentTypes(), dirInput.getScopes(), Format.DIRECTORY);

                        FileUtils.copyDirectory(dirInput.file, dest)
                    }
                }
            }

            for (JarInput jarInput : input.jarInputs) {
                if (jarInput) {
                    if (jarInput.file && jarInput.file.exists()) {
                        String jarName = jarInput.name;
                        String md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath);

                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }

                        File dest = outputProvider.getContentLocation(DigestUtils.md5Hex(jarName + md5Name), jarInput.contentTypes, jarInput.scopes, Format.JAR)

                        if (dest) {
                            if (dest.parentFile) {
                                if (!dest.parentFile.exists()) {
                                    dest.parentFile.mkdirs()
                                }
                            }

                            if (!dest.exists()) {
                                dest.createNewFile()
                            }

                            FileUtils.copyFile(jarInput.file, dest)
                        }
                    }
                }
            }
        }
    }

    private void deleteMethodTimePrintHelperFromBuildDir() {
        mAndroid.buildTypes.each { buildType ->
            def classPath = new StringBuffer().append(mProject.buildDir.absolutePath)
                    .append(File.separator).append("intermediates")
                    .append(File.separator).append("classes")
                    .append(File.separator).append(buildType.name)
                    .append(File.separator).append(MethodTimePrintHelper.class.package.name.replaceAll("\\.", File.separator))
                    .append(File.separator).append(MethodTimePrintHelper.class.simpleName).append(".class").toString()

            File file = new File(classPath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private void appendClassPath(File file) {
        if (isEnable() && null != file && file.exists()) {
            MethodTimePrintInjector.injector.appendClassPath(file)
        }
    }

    private boolean isEnable() {
        return null != mExtension && mExtension.enable
    }
}
