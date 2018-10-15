package com.llew.method.time.printer.injector

import com.android.build.gradle.AppExtension
import com.llew.method.time.printer.extension.MethodTimePrintExtension
import com.llew.method.time.printer.helper.MethodTimePrintHelper
import com.llew.method.time.printer.utils.Logger
import com.llew.method.time.printer.utils.TextUtil
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.Descriptor
import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * MethodTimePrint字节码注入类
 * <br/><br/>
 *
 * @author llew
 * @date 2017/12/15
 */

public class MethodTimePrintInjector {

    private static final String JAVA      = ".java"
    private static final String CLASS     = ".class"

    private static final String TOP_LEFT_CORNER    = '┌'
    private static final String MIDDLE_CORNER      = '├'
    private static final String BOTTOM_LEFT_CORNER = '└'

    private static final String HORIZONTAL_LINE    = '│'
    private static final String DOUBLE_DIVIDER     = "────────────────────────────────────────"
    private static final String SINGLE_DIVIDER     = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private static final String TOP_BORDER         = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
    private static final String BOTTOM_BORDER      = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER
    private static final String MIDDLE_BORDER      = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER


    private static final String PACKAGE_PATH = MethodTimePrintHelper.class.package.name.replaceAll("\\.", File.separator)

    private static final String PATTERN = ".+(\\\$[0-9]+)\$"
    private static Pattern sPattern = Pattern.compile(PATTERN)

    private static ClassPool sClassPool
    private static MethodTimePrintInjector sInject

    private MethodTimePrintExtension mExtension
    private Project mProject
    private AppExtension mAndroid


    private MethodTimePrintInjector(AppExtension android, Project project, MethodTimePrintExtension extension) {
        mAndroid = android
        mProject = project
        mExtension = extension
        sClassPool = ClassPool.default
        appendDefaultClassPath()
    }

    public static void init(AppExtension android, Project project, MethodTimePrintExtension extension) {
        sInject = new MethodTimePrintInjector(android, project, extension)
    }

    public static MethodTimePrintInjector getInjector() {
        if (null == sInject) {
            throw new RuntimeException("you must call init() before injector() called !!!")
        }
        return sInject
    }

    public void inject(File srcFile) {
        if (srcFile) {
            injectFile(srcFile, srcFile.absolutePath, srcFile.absolutePath.length())
        }
    }

    private void injectFile(File file, String classPath, int length) {
        if (file && file.exists()) {
            if (file.isDirectory()) {
                file.eachFile { f ->
                    injectFile(f, classPath, length)
                }
            } else {

                if (isValid(file)) {

                    final String filePath = file.absolutePath

                    final String className = filePath.substring(length + 1, filePath.length() - CLASS.length()).replaceAll("/", ".")

                    CtClass ctClass = sClassPool.getCtClass(className)

                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    if (!MethodTimePrintHelper.class.name.equals(className)) {

                        copyMethodTimePrintHelperToBuildDir(classPath, ctClass.classFile.majorVersion)

                        if (!ctClass.interface && canInjectCode(ctClass)) {

                            ctClass.declaredMethods.each { method ->

                                if (null != method && null != method.methodInfo2 && null != method.methodInfo2.codeAttribute) {
                                    String start = MethodTimePrintHelper.class.name + ".start();"

                                    /**
                                     * ┌─────────────────────────────────────────────────────────────
                                     * │ Time：49
                                     * ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
                                     * │ com.llew.method.time.printer.helper.MethodTimePrintHelper
                                     * │     +onStop()
                                     * ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
                                     * │ Thread：main
                                     * └─────────────────────────────────────────────────────────────
                                     */

                                    StringBuffer log = new StringBuffer()
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(TOP_BORDER).append("\");")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(HORIZONTAL_LINE).append(" Time：\"").append("+(java.lang.System.currentTimeMillis()-").append(MethodTimePrintHelper.class.name).append(".stop()").append(")").append(");")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(MIDDLE_BORDER).append("\");")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(HORIZONTAL_LINE).append(" ").append(ctClass.name).append("\");")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(HORIZONTAL_LINE).append("     +").append(method.name).append(Descriptor.toString(method.signature)).append("\");")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(MIDDLE_BORDER).append("\");")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(HORIZONTAL_LINE).append(" Thread：\"").append(" + java.lang.Thread.currentThread().getName());")
                                    log.append("android.util.Log.").append(getLogType(mExtension.level)).append("(\"").append(mExtension.tag).append("\",\"").append(BOTTOM_BORDER).append("\");")

                                    method.insertBefore(start)
                                    method.insertAfter(log.toString())
                                } else {
                                    Logger.e(ctClass.name + "." + method.name + "() can't inject code")
                                }
                            }
                            try {
                                ctClass.writeFile(classPath)
                            } catch (Throwable ignore) {
                            }
                        } else {
                            Logger.e(ctClass.name + " is interface and can't inject code !!!")
                        }
                    }
                    ctClass.detach()
                }
            }
        }
    }

    public void appendClassPath(File path) {
        if (null != path) {
            if (path.isDirectory()) {
                sClassPool.appendPathList(path.absolutePath)
            } else {
                sClassPool.appendClassPath(path.absolutePath)
            }
        }
    }

    private void appendDefaultClassPath() {
        if (null == mProject) return
        def androidJar = new StringBuffer().append(mProject.android.getSdkDirectory())
                .append(File.separator).append("platforms")
                .append(File.separator).append(mProject.android.compileSdkVersion)
                .append(File.separator).append("android.jar").toString()

        File file = new File(androidJar);
        if (!file.exists()) {
            androidJar = new StringBuffer().append(mProject.rootDir.absolutePath)
                    .append(File.separator).append("local.properties").toString()

            Properties properties = new Properties()
            properties.load(new File(androidJar).newDataInputStream())

            def sdkDir = properties.getProperty("sdk.dir")

            androidJar = new StringBuffer().append(sdkDir)
                    .append(File.separator).append("platforms")
                    .append(File.separator).append(mProject.android.compileSdkVersion)
                    .append(File.separator).append("android.jar").toString()

            file = new File(androidJar)
        }

        if (file.exists()) {
            sClassPool.appendClassPath(androidJar)
        } else {
            Logger.e("couldn't find android.jar file !!!")
        }
    }

    private boolean canInjectCode(CtClass ctClass) {
        for (CtClass impl : ctClass.interfaces) {
            if (mExtension.excludes.contains(impl.name)) {
                return false
            }
        }
        return true
    }

    private boolean isValid(File file) {
        return isFilePathValid(file.absolutePath) && isFileNameValid(file.name)
    }

    private boolean isFilePathValid(String filePath) {
        return (!TextUtil.isEmpty(filePath)
                && filePath.endsWith(".class")
                && !filePath.contains("R\$")
                && !filePath.contains("R.class")
                && !filePath.contains("BuildConfig.class"))
    }

    private boolean isFileNameValid(String fileName) {

        fileName = fileName.substring(0, fileName.length() - CLASS.length())

        final Matcher matcher = sPattern.matcher(fileName)

        return !matcher.matches()
    }

    private String getLogType(int level) {
        if (2 == level) {
            return "v"
        } else if (3 == level) {
            return "d"
        } else if (4 == level) {
            return "i"
        } else if (5 == level) {
            return "w"
        } else if (6 == level) {
            return "e"
        } else {
            return "i"
        }
    }

    private void copyMethodTimePrintHelperToBuildDir(String classPath, int majorVersion) {

        File dst = new File(classPath, PACKAGE_PATH + File.separator + MethodTimePrintHelper.class.simpleName + CLASS)

        if (!dst.parentFile.exists()) {
            dst.parentFile.mkdirs()
        }
        if (!dst.exists()) {
            InputStream is = MethodTimePrintHelper.class.getResourceAsStream(MethodTimePrintHelper.class.simpleName + CLASS)
            OutputStream os = new FileOutputStream(dst)
            os << is
            os.flush()
            os.close()
            is.close()
            Logger.e("MethodTimePrintHelper has copied successfully !!!")
        }

        CtClass MethodTimePrintHelper = sClassPool.getCtClass(MethodTimePrintHelper.class.name)
        if (majorVersion != MethodTimePrintHelper.classFile.majorVersion) {
            int version = MethodTimePrintHelper.classFile.majorVersion
            MethodTimePrintHelper.classFile.majorVersion = majorVersion
            MethodTimePrintHelper.writeFile(classPath)
            MethodTimePrintHelper.defrost()
            Logger.e("MethodTimePrintHelper has changed version from JAVA_" + (version - 44) + " to JAVA_" +(majorVersion - 44) + " successfully !!!")
        }
    }
}
