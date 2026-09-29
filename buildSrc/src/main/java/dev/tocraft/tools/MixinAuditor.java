package dev.tocraft.tools;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class MixinAuditor {

    public static class ClassRepo {
        private final List<ZipFile> zipFiles = new ArrayList<>();
        private final Map<String, ClassNode> classCache = new HashMap<>();

        public ClassRepo(List<File> jars) {
            for (File f : jars) {
                if (f.exists()) {
                    try {
                        zipFiles.add(new ZipFile(f));
                    } catch (IOException ignored) {}
                }
            }
        }

        public ClassNode getClass(String internalName) {
            if (internalName == null) return null;
            String name = internalName.replace('.', '/');
            if (classCache.containsKey(name)) {
                return classCache.get(name);
            }
            String path = name + ".class";
            for (ZipFile zip : zipFiles) {
                ZipEntry entry = zip.getEntry(path);
                if (entry != null) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        ClassReader cr = new ClassReader(is);
                        ClassNode cn = new ClassNode();
                        cr.accept(cn, 0);
                        classCache.put(name, cn);
                        return cn;
                    } catch (Exception ignored) {}
                }
            }
            classCache.put(name, null);
            return null;
        }

        public boolean isAssignable(String parent, String child) {
            if (parent.equals(child)) return true;
            String curr = child;
            Set<String> visited = new HashSet<>();
            while (curr != null && !curr.isEmpty() && !"java/lang/Object".equals(curr) && visited.add(curr)) {
                ClassNode cn = getClass(curr);
                if (cn == null) break;
                if (parent.equals(cn.superName)) return true;
                if (cn.interfaces != null) {
                    for (String iface : cn.interfaces) {
                        if (parent.equals(iface) || isAssignable(parent, iface)) return true;
                    }
                }
                curr = cn.superName;
            }
            return false;
        }

        public List<MethodNode> findMethods(String className, String methodName, String methodDesc) {
            List<MethodNode> result = new ArrayList<>();
            String curr = className;
            Set<String> visited = new HashSet<>();
            while (curr != null && !curr.isEmpty() && !"java/lang/Object".equals(curr) && visited.add(curr)) {
                ClassNode cn = getClass(curr);
                if (cn == null) break;
                if (cn.methods != null) {
                    for (MethodNode mn : cn.methods) {
                        if (mn.name.equals(methodName)) {
                            if (methodDesc == null || mn.desc.equals(methodDesc)) {
                                result.add(mn);
                            }
                        }
                    }
                }
                if (cn.interfaces != null) {
                    for (String iface : cn.interfaces) {
                        result.addAll(findMethods(iface, methodName, methodDesc));
                    }
                }
                curr = cn.superName;
            }
            return result;
        }

        public List<FieldNode> findFields(String className, String fieldName, String fieldDesc) {
            List<FieldNode> result = new ArrayList<>();
            String curr = className;
            Set<String> visited = new HashSet<>();
            while (curr != null && !curr.isEmpty() && !"java/lang/Object".equals(curr) && visited.add(curr)) {
                ClassNode cn = getClass(curr);
                if (cn == null) break;
                if (cn.fields != null) {
                    for (FieldNode fn : cn.fields) {
                        if (fn.name.equals(fieldName)) {
                            if (fieldDesc == null || fn.desc.equals(fieldDesc)) {
                                result.add(fn);
                            }
                        }
                    }
                }
                if (cn.interfaces != null) {
                    for (String iface : cn.interfaces) {
                        result.addAll(findFields(iface, fieldName, fieldDesc));
                    }
                }
                curr = cn.superName;
            }
            return result;
        }

        public boolean methodInvokes(MethodNode caller, String targetOwner, String targetName, String targetDesc) {
            if (caller.instructions == null) return false;
            for (AbstractInsnNode insn : caller.instructions) {
                if (insn instanceof MethodInsnNode minsn) {
                    if (minsn.name.equals(targetName) && (targetDesc == null || minsn.desc.equals(targetDesc))) {
                        if (targetOwner == null || minsn.owner.equals(targetOwner) || isAssignable(targetOwner, minsn.owner) || isAssignable(minsn.owner, targetOwner)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean methodAccessesField(MethodNode caller, String targetOwner, String targetName, String targetDesc) {
            if (caller.instructions == null) return false;
            for (AbstractInsnNode insn : caller.instructions) {
                if (insn instanceof FieldInsnNode finsn) {
                    if (finsn.name.equals(targetName) && (targetDesc == null || finsn.desc.equals(targetDesc))) {
                        if (targetOwner == null || finsn.owner.equals(targetOwner) || isAssignable(targetOwner, finsn.owner) || isAssignable(finsn.owner, targetOwner)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public static void audit(File projectDir, String projectName) throws Exception {
        String home = System.getProperty("user.home");
        List<File> jars = new ArrayList<>();

        // 1. Minecraft Merged Jars
        Path loomCache = Paths.get(home, ".gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged");
        if (Files.exists(loomCache)) {
            try (var stream = Files.walk(loomCache)) {
                stream.filter(p -> p.toString().endsWith(".jar") && !p.toString().contains("intermediary") && p.toString().contains("loom.mappings") && p.toString().contains("1.21.11"))
                      .map(Path::toFile)
                      .forEach(jars::add);
            }
        }

        // 2. Dependency jars in .gradle/caches/modules-2
        Path modulesCache = Paths.get(home, ".gradle/caches/modules-2/files-2.1");
        if (Files.exists(modulesCache)) {
            try (var stream = Files.walk(modulesCache)) {
                stream.filter(p -> p.toString().endsWith(".jar") && !p.toString().endsWith("-sources.jar") && !p.toString().endsWith("-javadoc.jar"))
                      .map(Path::toFile)
                      .forEach(jars::add);
            }
        }

        // 3. Maven Local jars
        Path m2Cache = Paths.get(home, ".m2/repository");
        // 5. Loom remapped mod cache in project and parent directories
        Path loomRemapped = projectDir.toPath().resolve(".gradle/loom-cache/remapped_mods");
        if (Files.exists(loomRemapped)) {
            try (var stream = Files.walk(loomRemapped)) {
                stream.filter(p -> p.toString().endsWith(".jar"))
                      .map(Path::toFile)
                      .forEach(jars::add);
            }
        }
        File parentLoom = new File(projectDir.getParentFile(), "non_identity2_bridge/.gradle/loom-cache/remapped_mods");
        if (parentLoom.exists()) {
            try (var stream = Files.walk(parentLoom.toPath())) {
                stream.filter(p -> p.toString().endsWith(".jar"))
                      .map(Path::toFile)
                      .forEach(jars::add);
            }
        }

        // 4. Local libs
        File libsDir = new File(projectDir, "libs");
        if (libsDir.exists() && libsDir.isDirectory()) {
            File[] libFiles = libsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (libFiles != null) {
                Collections.addAll(jars, libFiles);
            }
        }

        ClassRepo repo = new ClassRepo(jars);
        int totalFiles = 0;
        int totalIssues = 0;

        // Locate compiled class files
        Path classesDir = projectDir.toPath().resolve("build/classes/java/main");
        if (!Files.exists(classesDir)) {
            classesDir = projectDir.toPath();
        }

        List<Path> classFiles = new ArrayList<>();
        try (var stream = Files.walk(classesDir)) {
            stream.filter(p -> p.toString().endsWith(".class")).forEach(classFiles::add);
        }

        for (Path cp : classFiles) {
            byte[] bytes = Files.readAllBytes(cp);
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            AnnotationNode mixinAnn = findAnnotation(cn.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Mixin;");
            if (mixinAnn == null) {
                mixinAnn = findAnnotation(cn.visibleAnnotations, "Lorg/spongepowered/asm/mixin/Mixin;");
            }
            if (mixinAnn == null) continue;

            totalFiles++;
            List<String> targetClasses = new ArrayList<>();

            // Parse Mixin targets
            if (mixinAnn.values != null) {
                for (int i = 0; i < mixinAnn.values.size(); i += 2) {
                    String key = (String) mixinAnn.values.get(i);
                    Object val = mixinAnn.values.get(i + 1);
                    if ("value".equals(key) && val instanceof List<?> list) {
                        for (Object o : list) {
                            if (o instanceof Type t) {
                                targetClasses.add(t.getInternalName());
                            }
                        }
                    } else if ("targets".equals(key) && val instanceof List<?> list) {
                        for (Object o : list) {
                            if (o instanceof String s) {
                                targetClasses.add(s.replace('.', '/'));
                            }
                        }
                    }
                }
            }

            // Verify target classes exist
            for (String target : targetClasses) {
                if (repo.getClass(target) == null && !target.contains("bettercombat")) {
                    System.err.println("  [!] ERROR " + cn.name + ": Target class not found: " + target);
                    totalIssues++;
                }
            }

            // Audit Fields (@Shadow, @Accessor)
            if (cn.fields != null) {
                for (FieldNode fn : cn.fields) {
                    if (hasAnnotation(fn.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;") ||
                        hasAnnotation(fn.visibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;")) {
                        boolean found = false;
                        for (String target : targetClasses) {
                            if (!repo.findFields(target, fn.name, null).isEmpty()) {
                                found = true;
                                break;
                            }
                        }
                        if (!targetClasses.isEmpty() && !found && repo.getClass(targetClasses.get(0)) != null) {
                            System.err.println("  [!] ERROR " + cn.name + ": @Shadow field '" + fn.name + "' not found in target " + targetClasses.get(0));
                            totalIssues++;
                        }
                    }
                }
            }

            // Audit Methods (@Inject, @Redirect, @WrapOperation, @Shadow, @Accessor, @Invoker)
            if (cn.methods != null) {
                for (MethodNode mn : cn.methods) {
                    // Check @Shadow
                    if (hasAnnotation(mn.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;") ||
                        hasAnnotation(mn.visibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;")) {
                        boolean found = false;
                        for (String target : targetClasses) {
                            if (!repo.findMethods(target, mn.name, null).isEmpty()) {
                                found = true;
                                break;
                            }
                        }
                        if (!targetClasses.isEmpty() && !found && repo.getClass(targetClasses.get(0)) != null) {
                            System.err.println("  [!] ERROR " + cn.name + ": @Shadow method '" + mn.name + "' not found in target " + targetClasses.get(0));
                            totalIssues++;
                        }
                    }

                    // Check @Accessor / @Invoker
                    AnnotationNode accAnn = getAnnotation(mn, "Lorg/spongepowered/asm/mixin/gen/Accessor;");
                    AnnotationNode invAnn = getAnnotation(mn, "Lorg/spongepowered/asm/mixin/gen/Invoker;");
                    if (accAnn != null) {
                        String fTarget = getAnnotationValue(accAnn, "value", "");
                        if (fTarget.isEmpty()) fTarget = inferFieldName(mn.name);
                        boolean found = false;
                        for (String target : targetClasses) {
                            if (!repo.findFields(target, fTarget, null).isEmpty()) {
                                found = true;
                                break;
                            }
                        }
                        if (!targetClasses.isEmpty() && !found && repo.getClass(targetClasses.get(0)) != null) {
                            System.err.println("  [!] ERROR " + cn.name + ": @Accessor field '" + fTarget + "' not found in target " + targetClasses.get(0));
                            totalIssues++;
                        }
                    }
                    if (invAnn != null) {
                        String mTarget = getAnnotationValue(invAnn, "value", "");
                        if (mTarget.isEmpty()) mTarget = inferMethodName(mn.name);
                        boolean found = false;
                        for (String target : targetClasses) {
                            if (!repo.findMethods(target, mTarget, null).isEmpty()) {
                                found = true;
                                break;
                            }
                        }
                        if (!targetClasses.isEmpty() && !found && repo.getClass(targetClasses.get(0)) != null) {
                            System.err.println("  [!] ERROR " + cn.name + ": @Invoker method '" + mTarget + "' not found in target " + targetClasses.get(0));
                            totalIssues++;
                        }
                    }

                    // Check Injections (@Inject, @Redirect, @WrapOperation, @ModifyArg, etc.)
                    AnnotationNode injAnn = getInjectionAnnotation(mn);
                    if (injAnn != null) {
                        List<String> targetMethods = getAnnotationList(injAnn, "method");
                        List<AnnotationNode> ats = getAtAnnotations(injAnn);

                        for (String targetM : targetMethods) {
                            String mName = targetM.contains("(") ? targetM.substring(0, targetM.indexOf('(')) : targetM;
                            String mDesc = targetM.contains("(") ? targetM.substring(targetM.indexOf('(')) : null;

                            List<MethodNode> matchedTargets = new ArrayList<>();
                            for (String target : targetClasses) {
                                matchedTargets.addAll(repo.findMethods(target, mName, mDesc));
                            }

                            if (!targetClasses.isEmpty() && matchedTargets.isEmpty() && repo.getClass(targetClasses.get(0)) != null) {
                                System.err.println("  [!] ERROR " + cn.name + ": Target method '" + targetM + "' not found in " + targetClasses.get(0));
                                totalIssues++;
                                continue;
                            }

                            // Verify Callback Parameters Signature
                            if (injAnn.desc.endsWith("Inject;")) {
                                Type[] cbArgs = Type.getArgumentTypes(mn.desc);
                                List<Type> leadingArgs = new ArrayList<>();
                                for (int aIdx = 0; aIdx < cbArgs.length; aIdx++) {
                                    Type argType = cbArgs[aIdx];
                                    if (argType.getClassName().equals("org.spongepowered.asm.mixin.injection.callback.CallbackInfo") ||
                                        argType.getClassName().equals("org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable")) {
                                        break;
                                    }
                                    if (!hasParameterAnnotation(mn, aIdx, "Lcom/llamalad7/mixinextras/sugar/Local;") &&
                                        !hasParameterAnnotation(mn, aIdx, "Lcom/llamalad7/mixinextras/sugar/Share;")) {
                                        leadingArgs.add(argType);
                                    }
                                }

                                if (!leadingArgs.isEmpty()) {
                                    boolean signatureMatch = false;
                                    for (MethodNode tmn : matchedTargets) {
                                        Type[] targetArgs = Type.getArgumentTypes(tmn.desc);
                                        if (targetArgs.length == leadingArgs.size()) {
                                            boolean typesMatch = true;
                                            for (int i = 0; i < targetArgs.length; i++) {
                                                if (!targetArgs[i].equals(leadingArgs.get(i)) &&
                                                    !repo.isAssignable(leadingArgs.get(i).getInternalName(), targetArgs[i].getInternalName()) &&
                                                    !repo.isAssignable(targetArgs[i].getInternalName(), leadingArgs.get(i).getInternalName())) {
                                                    typesMatch = false;
                                                    break;
                                                }
                                            }
                                            if (typesMatch) {
                                                signatureMatch = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!signatureMatch && !matchedTargets.isEmpty()) {
                                        System.err.println("  [!] ERROR " + cn.name + "." + mn.name + ": Callback parameters " + leadingArgs + " do not match target method " + targetM + " parameters");
                                        totalIssues++;
                                    }
                                }
                            }

                            // Verify @At references
                            for (AnnotationNode at : ats) {
                                String atValue = getAnnotationValue(at, "value", "");
                                String atTarget = getAnnotationValue(at, "target", "");
                                if (!atTarget.isEmpty() && !atTarget.contains("bettercombat")) {
                                    ParsedMember parsed = parseMemberTarget(atTarget);
                                    boolean atFound = false;
                                    for (MethodNode tmn : matchedTargets) {
                                        if ("INVOKE".equals(atValue) && repo.methodInvokes(tmn, parsed.owner, parsed.name, parsed.desc)) {
                                            atFound = true;
                                            break;
                                        } else if ("FIELD".equals(atValue) && repo.methodAccessesField(tmn, parsed.owner, parsed.name, parsed.desc)) {
                                            atFound = true;
                                            break;
                                        }
                                    }
                                    if (!atFound && ("INVOKE".equals(atValue) || "FIELD".equals(atValue))) {
                                        System.err.println("  [!] WARNING " + cn.name + "." + mn.name + ": Injected method '" + mName + "' does not reference @At(" + atValue + ") target: " + atTarget);
                                        totalIssues++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("[checkMixins] Audited " + totalFiles + " compiled mixin classes in " + projectName + ".");
        if (totalIssues > 0) {
            System.err.println(">>> TOTAL MIXIN ISSUES DETECTED: " + totalIssues + " in " + projectName + " <<<");
            throw new RuntimeException("Found " + totalIssues + " broken mixin targets in " + projectName + "!");
        } else {
            System.out.println(">>> ALL " + totalFiles + " MIXINS VERIFIED 100% OK! <<<");
        }
    }

    private static boolean hasAnnotation(List<AnnotationNode> list, String desc) {
        return findAnnotation(list, desc) != null;
    }

    private static AnnotationNode findAnnotation(List<AnnotationNode> list, String desc) {
        if (list == null) return null;
        for (AnnotationNode an : list) {
            if (desc.equals(an.desc)) return an;
        }
        return null;
    }

    private static AnnotationNode getAnnotation(MethodNode mn, String desc) {
        AnnotationNode an = findAnnotation(mn.invisibleAnnotations, desc);
        return an != null ? an : findAnnotation(mn.visibleAnnotations, desc);
    }

    private static AnnotationNode getInjectionAnnotation(MethodNode mn) {
        String[] injs = {
            "Lorg/spongepowered/asm/mixin/injection/Inject;",
            "Lorg/spongepowered/asm/mixin/injection/Redirect;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyArg;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyVariable;",
            "Lorg/spongepowered/asm/mixin/injection/ModifyConstant;",
            "Lcom/llamalad7/mixinextras/injector/wrapoperation/WrapOperation;",
            "Lcom/llamalad7/mixinextras/injector/v2/WrapWithCondition;",
            "Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;",
            "Lcom/llamalad7/mixinextras/injector/ModifyExpressionValue;"
        };
        for (String desc : injs) {
            AnnotationNode an = getAnnotation(mn, desc);
            if (an != null) return an;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAnnotationValue(AnnotationNode an, String key, T defaultVal) {
        if (an == null || an.values == null) return defaultVal;
        for (int i = 0; i < an.values.size(); i += 2) {
            if (key.equals(an.values.get(i))) {
                return (T) an.values.get(i + 1);
            }
        }
        return defaultVal;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getAnnotationList(AnnotationNode an, String key) {
        if (an == null || an.values == null) return Collections.emptyList();
        for (int i = 0; i < an.values.size(); i += 2) {
            if (key.equals(an.values.get(i))) {
                Object val = an.values.get(i + 1);
                if (val instanceof List<?> list) {
                    List<String> res = new ArrayList<>();
                    for (Object o : list) res.add(o.toString());
                    return res;
                } else if (val instanceof String s) {
                    return List.of(s);
                }
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static List<AnnotationNode> getAtAnnotations(AnnotationNode an) {
        if (an == null || an.values == null) return Collections.emptyList();
        List<AnnotationNode> result = new ArrayList<>();
        for (int i = 0; i < an.values.size(); i += 2) {
            if ("at".equals(an.values.get(i))) {
                Object val = an.values.get(i + 1);
                if (val instanceof AnnotationNode atNode) {
                    result.add(atNode);
                } else if (val instanceof List<?> list) {
                    for (Object o : list) {
                        if (o instanceof AnnotationNode item) result.add(item);
                    }
                }
            }
        }
        return result;
    }

    private static boolean hasParameterAnnotation(MethodNode mn, int paramIdx, String desc) {
        if (mn.invisibleParameterAnnotations != null && paramIdx < mn.invisibleParameterAnnotations.length) {
            List<AnnotationNode> list = mn.invisibleParameterAnnotations[paramIdx];
            if (hasAnnotation(list, desc)) return true;
        }
        if (mn.visibleParameterAnnotations != null && paramIdx < mn.visibleParameterAnnotations.length) {
            List<AnnotationNode> list = mn.visibleParameterAnnotations[paramIdx];
            if (hasAnnotation(list, desc)) return true;
        }
        return false;
    }

    private static String inferFieldName(String accessorMethod) {
        if (accessorMethod.startsWith("get") || accessorMethod.startsWith("set")) {
            String sub = accessorMethod.substring(3);
            return Character.toLowerCase(sub.charAt(0)) + sub.substring(1);
        } else if (accessorMethod.startsWith("is")) {
            String sub = accessorMethod.substring(2);
            return Character.toLowerCase(sub.charAt(0)) + sub.substring(1);
        }
        return accessorMethod;
    }

    private static String inferMethodName(String invokerMethod) {
        if (invokerMethod.startsWith("call")) {
            String sub = invokerMethod.substring(4);
            return Character.toLowerCase(sub.charAt(0)) + sub.substring(1);
        }
        return invokerMethod;
    }

    private static record ParsedMember(String owner, String name, String desc) {}

    private static ParsedMember parseMemberTarget(String target) {
        // e.g. Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z
        // e.g. Lnet/minecraft/CrashReport;details:Ljava/util/List;
        String s = target;
        String owner = null;
        if (s.startsWith("L") && s.contains(";")) {
            int semi = s.indexOf(';');
            owner = s.substring(1, semi);
            s = s.substring(semi + 1);
        }
        String name = s;
        String desc = null;
        if (s.contains("(")) {
            int paren = s.indexOf('(');
            name = s.substring(0, paren);
            desc = s.substring(paren);
        } else if (s.contains(":")) {
            int col = s.indexOf(':');
            name = s.substring(0, col);
            desc = s.substring(col + 1);
        }
        return new ParsedMember(owner, name, desc);
    }
}
